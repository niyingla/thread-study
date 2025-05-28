package com.pikaqiu;

import com.pikaqiu.statemachine.v2.OrderProcessor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Configuration;
import org.springframework.retry.annotation.EnableRetry;

@Configuration
@EnableRetry
@SpringBootApplication
public class JavaApplication {

    @Autowired
    private OrderProcessor orderProcessor;



    public static void main(String[] args)throws Exception {
//        SqlTest.test();
        System.out.println("开始启动啦啦啦!!");
        SpringApplication.run(JavaApplication.class, args);
    }
}
