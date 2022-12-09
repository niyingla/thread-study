package com.pikaqiu.My;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;

@RestController
public class TestController {

    @Autowired
    private TestService testService;

    @GetMapping("testRetry")
    public void testRetry(){
        System.out.println(LocalDateTime.now());
        testService.testRetry();
    }
}
