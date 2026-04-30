package com.tjetc.ui;

import com.tjetc.entity.Information;

import javax.swing.*;
import java.awt.*;

public class ServerView extends JFrame {

    public void CreateServerView() {
        JFrame serverView = new  JFrame("Server");
        serverView.setLayout(new BorderLayout(10,0));
        Container serverFrame = serverView.getContentPane();
        serverFrame.setBackground(Color.gray);

        serverView.setLocationRelativeTo(null);
        serverView.setExtendedState(JFrame.MAXIMIZED_BOTH);
        serverView.setBounds(0, 0, 800, 600);
        serverView.setVisible(true);
        serverView.setDefaultCloseOperation(EXIT_ON_CLOSE);

        // 1. 顶部面板：显示大标题
        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 20));
        JLabel titleLabel = new JLabel("考试系统服务端", SwingConstants.CENTER);
        titleLabel.setFont(new Font("微软雅黑", Font.BOLD, 24));
        titleLabel.setForeground(Color.BLACK);
        topPanel.setBackground(Color.white);
        topPanel.add(titleLabel);
        serverFrame.add(topPanel, BorderLayout.NORTH);

        // 2. 中间面板：显示核心信息 (姓名、考号、密码)
        JPanel centerPanel = new JPanel(new GridBagLayout());
        centerPanel.setLayout(new GridLayout(6,10,5,5));
        centerPanel.setBorder(BorderFactory.createEmptyBorder(50, 100, 50, 100)); // 上下左右留白，让内容不贴边
        centerPanel.setBackground(Color.WHITE); // 给中间内容一个白色背景，看起来更像卡片

        for (int i =1;i<=60;i++){
            JPanel cell = createCell(i,i%2==0?Color.CYAN:Color.ORANGE);
            centerPanel.add(cell);
        }

        JScrollPane scrollPane = new JScrollPane(centerPanel);
        scrollPane.setBorder(BorderFactory.createEmptyBorder()); // 去掉滚动条边框
        serverFrame.add(scrollPane, BorderLayout.CENTER);

        // --- 窗口显示设置 ---
        serverView.setVisible(true);
    }
    /**
     * 辅助方法：创建一个单元格
     * @param text 显示的文字
     * @param color 背景颜色
     */
    private JPanel createCell(int text, Color color) {
        JPanel cell = new JPanel();
        cell.setLayout(new BorderLayout()); // 单元格内部也用布局，方便居中

        // 关键：设置不透明为true，背景色才会显示
        cell.setOpaque(true);
        cell.setBackground(color);
        cell.setBorder(BorderFactory.createLineBorder(Color.GRAY)); // 给每个格子加个边框

        // 添加文字标签
        JLabel label = new JLabel(String.valueOf(text), SwingConstants.CENTER);
        label.setFont(new Font("微软雅黑", Font.BOLD, 16));
        label.setOpaque(false); // 标签透明，显示面板的背景色
        cell.add(label);

        return cell;
    }

    public static void main(String args[]) {


        Information information = new Information(1,"张三","141124200608137890","D:/","123456","78927342","6574","7654");
        //在主方法中调用createJFrame()方法
        new ServerView().CreateServerView();


    }
}
