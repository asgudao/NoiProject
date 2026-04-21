package com.tjetc.ui;

import com.tjetc.entity.Information;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class ClientView extends JFrame {

    public void CreateClientView(Information information) {
        JFrame clientView = new JFrame("Client");
        clientView.setLayout(new BorderLayout(10,10));
        Container clientFrame = clientView.getContentPane();
        clientFrame.setBackground(Color.gray);

        clientView.setLocationRelativeTo(null);
        clientView.setExtendedState(JFrame.MAXIMIZED_BOTH);
        clientView.setBounds(0, 0, 800, 600);
        clientView.setVisible(true);
        clientView.setDefaultCloseOperation(EXIT_ON_CLOSE);

        // 1. 顶部面板：显示大标题
        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 20));
        JLabel titleLabel = new JLabel("考试系统客户端", SwingConstants.CENTER);
        titleLabel.setFont(new Font("微软雅黑", Font.BOLD, 24));
        titleLabel.setForeground(Color.BLACK);
        topPanel.setBackground(Color.white);
        topPanel.add(titleLabel);
        clientFrame.add(topPanel, BorderLayout.NORTH);

        // 2. 中间面板：显示核心信息 (姓名、考号、密码)
        JPanel centerPanel = new JPanel(new GridBagLayout());
        centerPanel.setBorder(BorderFactory.createEmptyBorder(50, 100, 50, 100)); // 上下左右留白，让内容不贴边
        centerPanel.setBackground(Color.WHITE); // 给中间内容一个白色背景，看起来更像卡片

        // 使用 GridBagConstraints 来精确控制位置
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL; // 组件横向填满
        gbc.insets = new Insets(10, 10, 10, 10); // 组件间距

        // --- 第一行：姓名 ---
        gbc.gridx = 0; gbc.gridy = 0; // 第0列，第0行
        centerPanel.add(new JLabel("考生姓名:", SwingConstants.RIGHT), gbc);

        gbc.gridx = 1; gbc.weightx = 1.0; // 第1列，允许横向拉伸
        JTextField nameField = new JTextField(information.getStudentName());
        nameField.setEditable(false);
        centerPanel.add(nameField, gbc);

        // --- 第二行：考号 ---
        gbc.gridx = 0; gbc.gridy = 1; // 第0列，第1行
        gbc.weightx = 0; // 重置拉伸，标签不拉伸
        centerPanel.add(new JLabel("考号:", SwingConstants.RIGHT), gbc);

        gbc.gridx = 1; gbc.weightx = 1.0; // 输入框允许拉伸
        JTextField examIdField = new JTextField(information.getExamNum());
        examIdField.setEditable(false);
        centerPanel.add(examIdField, gbc);

        // --- 第三行：解压密码 ---
        gbc.gridx = 0; gbc.gridy = 2;
        gbc.weightx = 0;
        centerPanel.add(new JLabel("解压密码:", SwingConstants.RIGHT), gbc);

        gbc.gridx = 1; gbc.weightx = 1.0;
        JPasswordField pwdField = new JPasswordField(information.getExamPassword());
        pwdField.setEditable(false);
        centerPanel.add(pwdField, gbc);

        clientFrame.add(centerPanel, BorderLayout.CENTER);

        // --- 第四行：身份证登录 ---
        gbc.gridx = 0; gbc.gridy = 3;
        gbc.weightx = 0;
        centerPanel.add(new JLabel("身份证号:", SwingConstants.RIGHT), gbc);

        gbc.gridx = 1; gbc.weightx = 1.0;
        JTextField idField = new JTextField();
        centerPanel.add(idField, gbc);
        clientFrame.add(centerPanel, BorderLayout.CENTER);

        //--- 第五行：压缩包位置 ----
        gbc.gridx = 0; gbc.gridy = 4;
        gbc.weightx = 0;
        centerPanel.add(new JLabel("压缩包地址:", SwingConstants.RIGHT), gbc);

        gbc.gridx = 1; gbc.weightx = 1.0;
        JTextField examFileField = new JTextField(information.getExamFile());
        examFileField.setEditable(false);
        centerPanel.add(examFileField, gbc);




        // 3. 底部面板：进度条和按钮
        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 0));
        bottomPanel.setBorder(BorderFactory.createEmptyBorder(20, 50, 20, 50));
        bottomPanel.setOpaque(false);

        // 按钮
        JButton btnLogin = new JButton("登录系统");
        JPanel btnContainer = new JPanel(new FlowLayout());
        btnContainer.setOpaque(false);
        btnContainer.add(btnLogin);
        bottomPanel.add(btnContainer, BorderLayout.EAST);

        clientFrame.add(bottomPanel, BorderLayout.SOUTH);

        btnLogin.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e){
                String idNumber = idField.getText().trim();
                if(idNumber.length()<=17||idNumber.length()>=20){
                    JOptionPane.showMessageDialog(null, "请输入正确格式的身份证号", "错误", JOptionPane.WARNING_MESSAGE);
                }
                else if(idNumber.equals(information.getStudentNum())){
                    pwdField.setEchoChar((char)0);
                }
            }
        });

        // 最后显示窗口
        clientView.setVisible(true);
    }


    public static void main(String args[]) {


        Information information = new Information(1,"张三","141124200608137890","D:/","123456","78927342","6574","7654");
        //在主方法中调用createJFrame()方法
        new ClientView().CreateClientView(information);


    }
}