package com.tjetc;

import com.tjetc.ui.ClientUI;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import javax.swing.*;

@SpringBootApplication
@MapperScan(basePackages = "com.tjetc.dao")
public class NoiClientApplication implements CommandLineRunner {

    public static void main(String[] args) {
        SpringApplication.run(NoiClientApplication.class, args);
    }

    @Override
    public void run(String... args) throws Exception {
        // 要启动的客户端数量（可改为从配置文件读取）
        int clientCount = 3;

        for (int i = 0; i < clientCount; i++) {
            final int clientIndex = i + 1;
            new Thread(() -> {
                SwingUtilities.invokeLater(() -> {
                    ClientUI client = new ClientUI();
                    client.setTitle("TCP客户端 - " + clientIndex); // 区分窗口标题
                });
            }).start();
        }
    }
}