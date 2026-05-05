package com.tjetc;


import com.tjetc.common.JsonResult;
import com.tjetc.entity.Information;
import com.tjetc.service.InformationService;
import com.tjetc.ui.ServerUI;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import javax.swing.*;
import java.util.List;

@SpringBootApplication
@MapperScan(basePackages = "com.tjetc.dao")
public class NoiServerApplication implements CommandLineRunner {

    @Autowired
    private InformationService informationService;

    public static void main(String[] args) {
        // 启动 Spring Boot
        System.setProperty("java.awt.headless", "false");
        SpringApplication.run(NoiServerApplication.class, args);
    }

    @Override
    public void run(String... args) {
        // 从数据库获取考生信息列表
        List<Information> informationList = informationService.selectAll().getData();

        // 在事件分发线程中创建并显示 GUI
        SwingUtilities.invokeLater(() -> new ServerUI(informationList));
    }
}
