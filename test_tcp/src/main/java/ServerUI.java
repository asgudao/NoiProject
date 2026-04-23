import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.net.*;

public class ServerUI extends JFrame {

    private JTextField ipField = new JTextField("0.0.0.0");
    private JTextField portField = new JTextField("8888");
    private JTextArea logArea = new JTextArea();

    private ServerSocket serverSocket;

    public ServerUI() {
        setTitle("TCP服务端");
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

        JButton startBtn = new JButton("启动监听");
        top.add(startBtn);

        add(top, BorderLayout.NORTH);

        // ===== 日志区 =====
        logArea.setFont(new Font("微软雅黑", Font.PLAIN, 14));
        logArea.setEditable(false);
        add(new JScrollPane(logArea), BorderLayout.CENTER);

        startBtn.addActionListener(e -> startServer());

        setVisible(true);
    }

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
                    log("客户端上线：" + socket.getInetAddress());

                    new Thread(() -> handleClient(socket)).start();
                }

            } catch (Exception e) {
                log("启动失败：" + e.getMessage());
            }
        }).start();
    }

    private void handleClient(Socket socket) {
        try {
            DataInputStream in = new DataInputStream(socket.getInputStream());

            while (true) {
                int len = in.readInt();
                byte type = in.readByte();

                // ===== 文本消息 =====
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
        }
    }

    private void log(String msg) {
        SwingUtilities.invokeLater(() -> {
            logArea.append(msg + "\n");
            logArea.setCaretPosition(logArea.getDocument().getLength()); // 自动滚动
        });
    }

    public static void main(String[] args) {
        new ServerUI();
    }
}