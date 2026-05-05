package com.tjetc.ui;

import com.fasterxml.jackson.annotation.JsonIgnore;  // 实体类中需要加此注解
import com.fasterxml.jackson.databind.ObjectMapper;
import com.tjetc.entity.Information;

import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.List;

public class ServerUI extends JFrame {

    private JTextField ipField = new JTextField("0.0.0.0");
    private JTextField portField = new JTextField("8888");
    private JTextArea logArea = new JTextArea();

    private ServerSocket serverSocket;
    private List<Information> informationList;

    public ServerUI(List<Information> informationList) {
        this.informationList = informationList;
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
            DataOutputStream out = new DataOutputStream(socket.getOutputStream());

            boolean isFirstMessage = true;
            String clientIp = null;

            while (true) {
                int len = in.readInt();
                byte type = in.readByte();

                // ===== 文本消息 =====
                if (type == 1) {
                    byte[] data = new byte[len - 1];
                    in.readFully(data);
                    String msg = new String(data, StandardCharsets.UTF_8);

                    if (isFirstMessage) {
                        clientIp = msg;
                        isFirstMessage = false;
                        log("客户端 IP 已记录：" + clientIp);

                        Information matched = null;
                        for (Information info : informationList) {
                            if (clientIp.equals(info.getComputerIp())) {
                                matched = info;
                                break;
                            }
                        }

                        if (matched != null) {
                            // 1. 发送考生信息（不含文件内容）
                            String infoStr = infoToString(matched);
                            sendToClient(out, infoStr);
                            log("已向 " + clientIp + " 发送考生信息");

                            String examFilePath = matched.getExamFile();  // 现在返回的是路径字符串
                            if (examFilePath != null && !examFilePath.isEmpty()) {
                                try {
                                    File file = new File(examFilePath);
                                    if (file.exists() && file.isFile()) {
                                        byte[] fileData = java.nio.file.Files.readAllBytes(file.toPath());
                                        sendFileToClient(out, fileData, file.getName());
                                        log("已向 " + clientIp + " 发送考试文件：" + file.getName());
                                    } else {
                                        log("考试文件不存在于路径：" + examFilePath);
                                    }
                                } catch (Exception e) {
                                    log("考试文件发送失败：" + e.getMessage());
                                }
                            }}
                        else {
                            sendToClient(out, "未找到匹配的考生信息");
                            log("未找到 IP " + clientIp + " 对应的考生");
                        }
                    } else {
                        log("[" + socket.getInetAddress() + "]：" + msg);
                    }
                }

                // ===== 接收客户端发来的文件 =====
                else if (type == 2) {
                    int nameLen = in.readInt();
                    byte[] nameBytes = new byte[nameLen];
                    in.readFully(nameBytes);
                    String fileName = new String(nameBytes, StandardCharsets.UTF_8);

                    int fileLen = len - 1 - 4 - nameLen;
                    byte[] fileData = new byte[fileLen];
                    in.readFully(fileData);

                    String newName = System.currentTimeMillis() + "_" + fileName;
                    try (FileOutputStream fos = new FileOutputStream(newName)) {
                        fos.write(fileData);
                    }
                    log("[" + socket.getInetAddress() + "] 收到文件：" + newName);
                }
            }

        } catch (EOFException e) {
            log("客户端断开：" + socket.getInetAddress());
        } catch (Exception e) {
            log("客户端异常：" + socket.getInetAddress() + " - " + e.getMessage());
        }
    }

    // 发送文本消息
    private void sendToClient(DataOutputStream out, String msg) throws Exception {
        byte[] data = msg.getBytes(StandardCharsets.UTF_8);
        out.writeInt(data.length + 1);
        out.writeByte(1);
        out.write(data);
        out.flush();
    }

    // 发送文件给客户端（使用 type=2 协议）
    private void sendFileToClient(DataOutputStream out, byte[] fileData, String fileName) throws Exception {
        byte[] nameBytes = fileName.getBytes(StandardCharsets.UTF_8);
        int totalLen = 1 + 4 + nameBytes.length + fileData.length; // type + nameLen + name + file
        out.writeInt(totalLen);
        out.writeByte(2);
        out.writeInt(nameBytes.length);
        out.write(nameBytes);
        out.write(fileData);
        out.flush();
    }

    // 将 Information 转为 JSON 字符串（排除 examFile 字段）
    private String infoToString(Information info) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            // 如果实体中的 examFile 没有加 @JsonIgnore，需要在此处手动排除
            // 更推荐在 Information 类的 examFile 字段上添加 @JsonIgnore 注解
            return mapper.writeValueAsString(info);
        } catch (Exception e) {
            e.printStackTrace();
            return "{}";
        }
    }

    private void log(String msg) {
        SwingUtilities.invokeLater(() -> {
            logArea.append(msg + "\n");
            logArea.setCaretPosition(logArea.getDocument().getLength());
        });
    }
}