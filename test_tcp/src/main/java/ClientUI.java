import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.net.Socket;

public class ClientUI extends JFrame {

    private JTextField ipField = new JTextField("127.0.0.1");
    private JTextField portField = new JTextField("8888");
    private JTextArea msgArea = new JTextArea();

    private Socket socket;
    private DataOutputStream out;

    public ClientUI() {
        setTitle("TCP客户端");
        setSize(550, 420);
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

        JButton connectBtn = new JButton("连接");
        top.add(connectBtn);

        add(top, BorderLayout.NORTH);

        // ===== 中间 =====
        msgArea.setFont(new Font("微软雅黑", Font.PLAIN, 14));
        msgArea.setEditable(false);
        add(new JScrollPane(msgArea), BorderLayout.CENTER);

        // ===== 底部 =====
        JPanel bottom = new JPanel();

        JTextField input = new JTextField(20);
        JButton sendBtn = new JButton("发送消息");
        JButton fileBtn = new JButton("发送文件");

        bottom.add(input);
        bottom.add(sendBtn);
        bottom.add(fileBtn);

        add(bottom, BorderLayout.SOUTH);

        // ===== 事件 =====
        connectBtn.addActionListener(e -> connect());

        sendBtn.addActionListener(e -> {
            sendMsg(input.getText());
            input.setText("");
        });

        fileBtn.addActionListener(e -> sendFile());

        setVisible(true);
    }

    private void connect() {
        try {
            socket = new Socket(ipField.getText(),
                    Integer.parseInt(portField.getText()));

            out = new DataOutputStream(socket.getOutputStream());

            append("已连接：" + socket.getInetAddress());

            // 启动接收线程
            new Thread(this::receive).start();

        } catch (Exception e) {
            append("连接失败");
        }
    }

    private void sendMsg(String msg) {
        if (socket == null || socket.isClosed()) {
            append("请先连接服务器！");
            return;
        }

        try {
            byte[] data = msg.getBytes();

            out.writeInt(data.length + 1);
            out.writeByte(1);
            out.write(data);
            out.flush();

            append("发送：" + msg);

        } catch (Exception e) {
            append("发送失败");
        }
    }

    private void sendFile() {
        if (socket == null || socket.isClosed()) {
            append("请先连接服务器！");
            return;
        }

        try {
            JFileChooser chooser = new JFileChooser();

            if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {

                File file = chooser.getSelectedFile();

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

                out.writeInt(totalLen);
                out.writeByte(2);

                out.writeInt(nameBytes.length);
                out.write(nameBytes);
                out.write(fileData);
                out.flush();

                append("发送文件：" + file.getName() + " (" + fileData.length + " bytes)");
            }

        } catch (Exception e) {
            append("文件发送失败");
        }
    }

    private void receive() {
        try {
            DataInputStream in = new DataInputStream(socket.getInputStream());

            while (true) {
                int len = in.readInt();
                byte type = in.readByte();

                // ===== 文本 =====
                if (type == 1) {
                    byte[] data = new byte[len - 1];
                    in.readFully(data);

                    append("收到：" + new String(data));
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

                    String newName = "client_" + System.currentTimeMillis() + "_" + fileName;

                    FileOutputStream fos = new FileOutputStream(newName);
                    fos.write(fileData);
                    fos.close();

                    append("收到文件：" + newName);
                }
            }

        } catch (Exception e) {
            append("与服务器断开");
        }
    }

    private void append(String msg) {
        SwingUtilities.invokeLater(() -> {
            msgArea.append(msg + "\n");
            msgArea.setCaretPosition(msgArea.getDocument().getLength());
        });
    }

    public static void main(String[] args) {
        new ClientUI();
    }
}