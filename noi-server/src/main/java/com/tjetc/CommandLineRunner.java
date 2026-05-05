package com.tjetc;

import com.tjetc.entity.Information;
import org.springframework.boot.SpringApplication;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;

public class CommandLineRunner {
    public static void main(String[] args) throws Exception {
        // 手动构建 SqlSessionFactory 或复用 Spring 容器
        // 最简单：直接用 JDBC 执行 UPDATE
        byte[] data = Files.readAllBytes(Paths.get("D:/server/1.zip"));
        Connection conn = DriverManager.getConnection("jdbc:sqlite:E:\\MapleLeaf\\school\\Course\\Computer-Networks\\NoiProject\\identifier.sqlite");
        PreparedStatement pst = conn.prepareStatement("UPDATE information SET exam_file = ? WHERE id = ?");
        pst.setBytes(1, data);
        pst.setInt(2, 1);   // 考生ID
        pst.executeUpdate();
        conn.close();
    }
}
