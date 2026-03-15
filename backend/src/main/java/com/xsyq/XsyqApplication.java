package com.xsyq;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class XsyqApplication {
    public static void main(String[] args) {
        SpringApplication.run(XsyqApplication.class, args);
        System.out.println("✅ 校食元气(XSYQ) 后端服务启动成功！");
    }
}
