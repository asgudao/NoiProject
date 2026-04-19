package com.tjetc.ui;

import com.tjetc.entity.Information;

import javax.swing.*;
import java.awt.*;

public class ClientView extends JFrame {

    public void CreateClientView(Information information) {
        JFrame frame = new JFrame("Client");
        frame.setLayout(new BorderLayout());
        Container clientFrame = frame.getContentPane();
        clientFrame.setBackground(Color.gray);

        frame.setVisible(true);
        frame.setDefaultCloseOperation(EXIT_ON_CLOSE);
    }
    public static void main(String args[]) {
        //在主方法中调用createJFrame()方法
        new ClientView().CreateClientView(null);
    }
}