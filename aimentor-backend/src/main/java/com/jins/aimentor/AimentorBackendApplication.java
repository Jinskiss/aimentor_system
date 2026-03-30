package com.jins.aimentor;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class AimentorBackendApplication {

    public static void main(String[] args) {
        SpringApplication.run(AimentorBackendApplication.class, args);

        System.out.println("========================================");
        System.out.println("=    学情分析与学习规划系统启动成功     =");
        System.out.println("=    访问地址: http://localhost:8080   =");
        System.out.println("========================================");
    }

}
