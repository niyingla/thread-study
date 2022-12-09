package com.pikaqiu;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Configuration;
import org.springframework.retry.annotation.EnableRetry;

@Configuration
@EnableRetry
@SpringBootApplication
public class JavaApplication {

    public static void main(String[] args) {
        System.out.println("开始启动啦。。。");
        SpringApplication.run(JavaApplication.class, args);


    }
}
