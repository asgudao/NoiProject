package com.tjetc.ui;

import com.tjetc.entity.Information;
import org.springframework.beans.factory.annotation.Autowired;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.net.Socket;

public class ClientUI extends JFrame {

    private JTextField ipField = new JTextField("127.0.0.1");
    private JTextField portField = new JTextField("8888");
    private JTextArea msgArea = new JTextArea();
    private JTextField nameField = new JTextField();
    private JTextField examNumField = new JTextField();
    private JPasswordField passwordField = new JPasswordField();
    private JTextField idField = new JTextField();
    private JTextField examFileAddressField = new JTextField();
    private JButton btnLogin=new JButton("登录系统");
    private JButton connectBtn=new JButton("连接");

    private Socket socket;
    private DataOutputStream out;

    public  ClientUI(Information information) {
        nameField.setText(information.getStudentName());
        examNumField.setText(information.getExamNum());
        passwordField.setText(information.getExamPassword());
        examFileAddressField.setText(information.getExamFileAddress());


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

        top.add(connectBtn);

        add(top, BorderLayout.NORTH);

        // ===== 中间 =====
        JPanel center = new JPanel();
        JPanel component = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);   // 组件间距
        gbc.fill = GridBagConstraints.HORIZONTAL; // 水平方向拉伸


        nameField.setEditable(false);
        examNumField.setEditable(false);
        passwordField.setEditable(false);
        examFileAddressField.setEditable(false);
        addFormRow(component, gbc, "考生姓名:", nameField, 0);
        addFormRow(component, gbc, "考号：", examNumField, 1);
        addFormRow(component, gbc, "解压密码：", passwordField, 2);
        addFormRow(component, gbc, "身份证号", idField, 3);
        addFormRow(component, gbc, "压缩包地址：", examFileAddressField, 4);

        //按钮
        gbc.gridx = 0;
        gbc.gridy = 5;
        gbc.weightx = 0;
        component.add(btnLogin, gbc);


        component.setBackground(Color.white);
        center.setBackground(Color.white);
        component.setPreferredSize(new Dimension(400, 250));
        center.add(component, BorderLayout.CENTER);
        add(center, BorderLayout.CENTER);

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
            public void actionPerformed(ActionEvent e){
                String idNumber = idField.getText().trim();
                if(idNumber.length()<=17||idNumber.length()>=20){
                    JOptionPane.showMessageDialog(null, "请输入正确格式的身份证号", "错误", JOptionPane.WARNING_MESSAGE);
                }
                else if(idNumber.equals(information.getStudentNum())){
                    passwordField.setEchoChar((char)0);
                }
            }
        });


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

            msgArea.append("已连接：" + socket.getInetAddress() + "\n");

            connectBtn.setEnabled(false);


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

//    public static void main(String[] args) {
//        Information information = new Information(1,"张三","141124200608137890","D:/","123456","78927342","6574","7654");
//        new ClientUI(information);
//    }
}