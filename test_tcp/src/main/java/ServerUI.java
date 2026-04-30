import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.net.*;
import java.util.List;

public class ServerUI extends JFrame {

    private JTextField ipField = new JTextField("0.0.0.0");
    private JTextField portField = new JTextField("8888");
    private JTextArea logArea = new JTextArea();

    private DefaultListModel<Socket> clientModel = new DefaultListModel<>();
    private JList<Socket> clientList = new JList<>(clientModel);

    private ServerSocket serverSocket;

    public ServerUI() {
        setTitle("TCP服务端");
        setSize(650, 450);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        // ===== 顶部 =====
        JPanel top = new JPanel();
        top.add(new JLabel("IP:"));
        ipField.setColumns(10);
        top.add(ipField);

        top.add(new JLabel("端口:"));
        portField.setColumns(5);
        top.add(portField);

        JButton startBtn = new JButton("启动监听");
        top.add(startBtn);

        add(top, BorderLayout.NORTH);

        // ===== 客户端列表 =====
        clientList.setPreferredSize(new Dimension(220, 0));

        // 支持多选（群发）
        clientList.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);

        // 美化显示
        clientList.setCellRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(
                    JList<?> list, Object value,
                    int index, boolean isSelected, boolean cellHasFocus) {

                Socket s = (Socket) value;

                return super.getListCellRendererComponent(
                        list,
                        s.getInetAddress().getHostAddress() + ":" + s.getPort(),
                        index,
                        isSelected,
                        cellHasFocus
                );
            }
        });

        add(new JScrollPane(clientList), BorderLayout.EAST);

        // ===== 日志区 =====
        logArea.setFont(new Font("微软雅黑", Font.PLAIN, 14));
        logArea.setEditable(false);
        add(new JScrollPane(logArea), BorderLayout.CENTER);

        // ===== 底部操作区 =====
        JPanel bottom = new JPanel();

        JTextField input = new JTextField(15);
        JButton sendBtn = new JButton("发送消息");
        JButton fileBtn = new JButton("发送文件");

        bottom.add(input);
        bottom.add(sendBtn);
        bottom.add(fileBtn);

        add(bottom, BorderLayout.SOUTH);

        // ===== 事件 =====
        startBtn.addActionListener(e -> startServer());

        sendBtn.addActionListener(e -> {
            sendToClient(input.getText());
            input.setText("");
        });

        fileBtn.addActionListener(e -> sendFileToClient());

        setVisible(true);
    }

    // ================== 启动服务器 ==================
    private void startServer() {
        new Thread(() -> {
            try {
                String ip = ipField.getText();
                int port = Integer.parseInt(portField.getText());

                InetAddress address = InetAddress.getByName(ip);
                serverSocket = new ServerSocket(port, 50, address);

                log("服务器启动：" + ip + ":" + port);

                while (true) {
                    Socket socket = serverSocket.accept();

                    //  UI线程安全添加
                    SwingUtilities.invokeLater(() -> clientModel.addElement(socket));

                    log("客户端上线：" + socket.getInetAddress());

                    new Thread(() -> handleClient(socket)).start();
                }

            } catch (Exception e) {
                log("启动失败：" + e.getMessage());
            }
        }).start();
    }

    // ================== 处理客户端 ==================
    private void handleClient(Socket socket) {
        try {
            DataInputStream in = new DataInputStream(socket.getInputStream());

            while (true) {
                int len = in.readInt();
                byte type = in.readByte();

                // ===== 文本 =====
                if (type == 1) {
                    byte[] data = new byte[len - 1];
                    in.readFully(data);

                    log("[" + socket.getInetAddress() + "]：" + new String(data));
                }

                // ===== 文件 =====
                else if (type == 2) {
                    int nameLen = in.readInt();
                    byte[] nameBytes = new byte[nameLen];
                    in.readFully(nameBytes);
                    String fileName = new String(nameBytes);

                    int fileLen = len - 1 - 4 - nameLen;
                    byte[] fileData = new byte[fileLen];
                    in.readFully(fileData);

                    String newName = System.currentTimeMillis() + "_" + fileName;

                    FileOutputStream fos = new FileOutputStream(newName);
                    fos.write(fileData);
                    fos.close();

                    log("[" + socket.getInetAddress() + "] 收到文件：" + newName);
                }
            }

        } catch (Exception e) {
            log("客户端断开：" + socket.getInetAddress());

            //  UI线程安全移除
            SwingUtilities.invokeLater(() -> clientModel.removeElement(socket));
        }
    }

    // ================== 发送消息（支持群发） ==================
    private void sendToClient(String msg) {
        List<Socket> sockets = clientList.getSelectedValuesList();

        if (sockets.isEmpty()) {
            log("请选择客户端！");
            return;
        }

        for (Socket socket : sockets) {
            try {
                DataOutputStream out = new DataOutputStream(socket.getOutputStream());

                byte[] data = msg.getBytes();
                out.writeInt(data.length + 1);
                out.writeByte(1);
                out.write(data);
                out.flush();

                log("发送给 [" + socket.getInetAddress() + "]：" + msg);

            } catch (Exception e) {
                log("发送失败：" + socket.getInetAddress());
            }
        }
    }

    // ================== 发送文件（支持群发） ==================
    private void sendFileToClient() {
        List<Socket> sockets = clientList.getSelectedValuesList();

        if (sockets.isEmpty()) {
            log("请选择客户端！");
            return;
        }

        try {
            JFileChooser chooser = new JFileChooser();

            if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
                File file = chooser.getSelectedFile();

                // 安全读取文件
                FileInputStream fis = new FileInputStream(file);
                ByteArrayOutputStream bos = new ByteArrayOutputStream();

                byte[] buffer = new byte[4096];
                int len;

                while ((len = fis.read(buffer)) != -1) {
                    bos.write(buffer, 0, len);
                }

                byte[] fileData = bos.toByteArray();
                byte[] nameBytes = file.getName().getBytes();

                fis.close();
                bos.close();

                int totalLen = 1 + 4 + nameBytes.length + fileData.length;

                for (Socket socket : sockets) {
                    try {
                        DataOutputStream out = new DataOutputStream(socket.getOutputStream());

                        out.writeInt(totalLen);
                        out.writeByte(2);
                        out.writeInt(nameBytes.length);
                        out.write(nameBytes);
                        out.write(fileData);
                        out.flush();

                        log("发送文件给 [" + socket.getInetAddress() + "]：" + file.getName());

                    } catch (Exception e) {
                        log("发送失败：" + socket.getInetAddress());
                    }
                }
            }

        } catch (Exception e) {
            log("文件发送失败");
        }
    }

    // ================== 日志 ==================
    private void log(String msg) {
        SwingUtilities.invokeLater(() -> {
            logArea.append(msg + "\n");
            logArea.setCaretPosition(logArea.getDocument().getLength());
        });
    }

    public static void main(String[] args) {
        new ServerUI();
    }
}