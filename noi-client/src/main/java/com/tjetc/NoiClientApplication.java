package com.tjetc;


import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan(basePackages = "com.tjetc.dao")
public class NoiClientApplication {
    public static void main(String[] args) {
        SpringApplication.run(NoiClientApplication.class, args);
    }
}
