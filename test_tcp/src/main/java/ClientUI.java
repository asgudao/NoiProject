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
        sendBtn.addActionListener(e -> sendMsg(input.getText()));
        fileBtn.addActionListener(e -> sendFile());

        setVisible(true);
    }

    private void connect() {
        try {
            socket = new Socket(ipField.getText(),
                    Integer.parseInt(portField.getText()));

            out = new DataOutputStream(socket.getOutputStream());

            msgArea.append("已连接：" + socket.getInetAddress() + "\n");

        } catch (Exception e) {
            msgArea.append("连接失败\n");
        }
    }

    private void sendMsg(String msg) {
        try {
            byte[] data = msg.getBytes();

            out.writeInt(data.length + 1);
            out.writeByte(1);
            out.write(data);
            out.flush();

            msgArea.append("发送：" + msg + "\n");

        } catch (Exception e) {
            msgArea.append("发送失败\n");
        }
    }

    private void sendFile() {
        try {
            JFileChooser chooser = new JFileChooser();

            if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {

                File file = chooser.getSelectedFile();

                byte[] fileData = new FileInputStream(file).readAllBytes();
                byte[] nameBytes = file.getName().getBytes();

                int len = 1 + 4 + nameBytes.length + fileData.length;

                out.writeInt(len);
                out.writeByte(2);

                out.writeInt(nameBytes.length);
                out.write(nameBytes);
                out.write(fileData);
                out.flush();

                msgArea.append("发送文件：" + file.getName() + "\n");
            }

        } catch (Exception e) {
            msgArea.append("文件发送失败\n");
        }
    }

    public static void main(String[] args) {
        new ClientUI();
    }
}