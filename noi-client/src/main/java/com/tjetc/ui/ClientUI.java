package com.tjetc.ui;

import com.google.gson.Gson;
import com.tjetc.entity.Information;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public class ClientUI extends JFrame {

    // 文件保存目录
    private static final String SAVE_DIR = "D:" + File.separator + "project";

    private JTextField ipField = new JTextField("127.0.0.1");
    private JTextField portField = new JTextField("8888");
    private JTextArea msgArea = new JTextArea();
    private JTextField nameField = new JTextField();
    private JTextField examNumField = new JTextField();
    private JPasswordField passwordField = new JPasswordField();
    private JTextField idField = new JTextField();
    private JTextField examFileAddressField = new JTextField();
    private JButton btnLogin = new JButton("登录系统");
    private JButton connectBtn = new JButton("连接");


    private Socket socket;
    private DataOutputStream out;
    private DataInputStream in;
    private Information information;            // 存储服务端返回的考生信息
    private volatile List<String> zipEntries;

    public ClientUI() {
        setTitle("TCP客户端");
        setSize(750, 420);
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

        top.add(connectBtn);
        add(top, BorderLayout.NORTH);

        // ===== 中间 =====
        JPanel center = new JPanel();
        JPanel component = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        nameField.setEditable(false);
        examNumField.setEditable(false);
        passwordField.setEditable(false);
        examFileAddressField.setEditable(false);
        addFormRow(component, gbc, "考生姓名:", nameField, 0);
        addFormRow(component, gbc, "考号：", examNumField, 1);
        addFormRow(component, gbc, "解压密码：", passwordField, 2);
        addFormRow(component, gbc, "身份证号", idField, 3);
        addFormRow(component, gbc, "压缩包地址：", examFileAddressField, 4);

        gbc.gridx = 0;
        gbc.gridy = 5;
        gbc.weightx = 0;
        component.add(btnLogin, gbc);

        component.setBackground(Color.white);
        center.setBackground(Color.white);
        component.setPreferredSize(new Dimension(400, 250));
        center.add(component, BorderLayout.CENTER);

        // ===== 消息显示区 =====
        msgArea.setEditable(false);
        JScrollPane msgScrollPane = new JScrollPane(msgArea);
        msgScrollPane.setPreferredSize(new Dimension(250, 0)); // 右区宽度 250 像素


        // 将考生信息区（center）和消息区（msgScrollPane）水平分割
        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, center, msgScrollPane);
        splitPane.setDividerLocation(500);   // 初始分割位置（左区宽度500，可根据窗口调整）
        splitPane.setResizeWeight(1.0);      // 调整窗口大小时，左区优先伸缩（0.0则右区优先）

        add(splitPane, BorderLayout.CENTER);   // 分割面板放到中央，取代原来的 center

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
        btnLogin.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String idNumber = idField.getText().trim();
                if (idNumber.length() <= 17 || idNumber.length() >= 20) {
                    JOptionPane.showMessageDialog(null, "请输入正确格式的身份证号", "错误", JOptionPane.WARNING_MESSAGE);
                } else if (information != null && idNumber.equals(information.getStudentNum())) {
                    passwordField.setEchoChar((char) 0);
                } else if (information == null) {
                    JOptionPane.showMessageDialog(null, "尚未获取到考生信息", "错误", JOptionPane.WARNING_MESSAGE);
                }
            }
        });
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        setVisible(true);
    }

    private void addFormRow(JPanel panel, GridBagConstraints gbc,
                            String labelText, JComponent comp, int row) {
        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.weightx = 0;
        panel.add(new JLabel(labelText), gbc);

        gbc.gridx = 1;
        gbc.weightx = 1.0;
        panel.add(comp, gbc);
    }

    private void connect() {
        try {
            socket = new Socket(ipField.getText(),
                    Integer.parseInt(portField.getText()));

            out = new DataOutputStream(socket.getOutputStream());
            in = new DataInputStream(socket.getInputStream());

            appendMsg("已连接：" + socket.getInetAddress());
            connectBtn.setEnabled(false);

            // 发送本机 IP
            String localIp = socket.getLocalAddress().getHostAddress();
            sendMsg(localIp);

            // 启动接收线程
            new Thread(this::receiveMessages).start();

        } catch (Exception e) {
            appendMsg("连接失败：" + e.getMessage());
        }
    }

    private void receiveMessages() {
        try {
            boolean firstInfoReceived = false;
            Gson gson = new Gson();
            while (true) {
                int len = in.readInt();
                byte type = in.readByte();

                if (type == 1) {
                    byte[] data = new byte[len - 1];
                    in.readFully(data);
                    String msg = new String(data, StandardCharsets.UTF_8);

                    if (!firstInfoReceived) {
                        firstInfoReceived = true;
                        try {
                            information = gson.fromJson(msg, Information.class);
                            SwingUtilities.invokeLater(() -> updateInfoFields(information));
                            appendMsg("成功接收考生信息");
                        } catch (Exception e) {
                            appendMsg("解析考生信息失败：" + e.getMessage());
                        }
                    } else {
                        String finalMsg = msg;
                        SwingUtilities.invokeLater(() -> appendMsg("服务器：" + finalMsg));
                    }
                } else if (type == 2) {
                    int nameLen = in.readInt();
                    byte[] nameBytes = new byte[nameLen];
                    in.readFully(nameBytes);
                    String fileName = new String(nameBytes, StandardCharsets.UTF_8);

                    int fileLen = len - 1 - 4 - nameLen;
                    byte[] fileData = new byte[fileLen];
                    in.readFully(fileData);

                    // 决定保存目录：优先使用服务端下发的 examFileAddress
                    String saveDir;
                    if (information != null && information.getExamFileAddress() != null
                            && !information.getExamFileAddress().isEmpty()) {
                        saveDir = information.getExamFileAddress();
                    } else {
                        saveDir = SAVE_DIR;   // 回退到默认目录
                    }

                    // 确保目录路径以分隔符结尾
                    if (!saveDir.endsWith(File.separator)) {
                        saveDir += File.separator;
                    }

                    File dir = new File(saveDir);
                    if (!dir.exists()) {
                        dir.mkdirs();
                    }

                    String savePath = saveDir + fileName;
                    try (FileOutputStream fos = new FileOutputStream(savePath)) {
                        fos.write(fileData);
                    }
                    if (fileName.toLowerCase().endsWith(".zip")) {
                        zipEntries = getZipEntries(savePath);
                        if (zipEntries != null && !zipEntries.isEmpty()) {
                            SwingUtilities.invokeLater(() ->
                                    appendMsg("压缩包内含文件：" + String.join(", ", zipEntries))
                            );
                        }
                    }
                    String finalFileName = fileName;
                    String finalSavePath = savePath;
                    SwingUtilities.invokeLater(() ->
                            appendMsg("收到考试文件：" + finalFileName + "，已保存至 " + finalSavePath));
                    // 如果是 ZIP 文件，读取内容列表并保存
                }
            }
        } catch (EOFException e) {
            SwingUtilities.invokeLater(() -> appendMsg("服务器断开连接"));
        } catch (Exception e) {
            SwingUtilities.invokeLater(() -> appendMsg("接收异常：" + e.getMessage()));
        }
    }

    private void updateInfoFields(Information info) {
        nameField.setText(info.getStudentName());
        examNumField.setText(info.getExamNum());
        passwordField.setText(info.getExamPassword());
        examFileAddressField.setText(info.getExamFileAddress());
    }

    private void sendMsg(String msg) {
        try {
            byte[] data = msg.getBytes(StandardCharsets.UTF_8);
            out.writeInt(data.length + 1);
            out.writeByte(1);
            out.write(data);
            out.flush();
            appendMsg("发送：" + msg);
        } catch (Exception e) {
            appendMsg("发送失败");
        }
    }

    private void sendFile() {
        try {
            JFileChooser chooser = new JFileChooser();
            if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
                File file = chooser.getSelectedFile();
                byte[] fileData = new FileInputStream(file).readAllBytes();
                byte[] nameBytes = file.getName().getBytes(StandardCharsets.UTF_8);

                int len = 1 + 4 + nameBytes.length + fileData.length;
                out.writeInt(len);
                out.writeByte(2);
                out.writeInt(nameBytes.length);
                out.write(nameBytes);
                out.write(fileData);
                out.flush();
                appendMsg("发送文件：" + file.getName());
            }
        } catch (Exception e) {
            appendMsg("文件发送失败");
        }
    }

    private void appendMsg(String msg) {
        SwingUtilities.invokeLater(() -> {
            msgArea.append(msg + "\n");
            msgArea.setCaretPosition(msgArea.getDocument().getLength());
        });
    }

    private List<String> getZipEntries(String zipPath) {
        List<String> entries = new ArrayList<>();
        try (ZipFile zipFile = new ZipFile(zipPath)) {
            zipFile.stream().map(ZipEntry::getName).forEach(entries::add);
        } catch (IOException e) {
            appendMsg("读取压缩包内容失败：" + e.getMessage());
        }
        return entries;
    }

    public static void main(String[] args) {
        new ClientUI();
    }
}