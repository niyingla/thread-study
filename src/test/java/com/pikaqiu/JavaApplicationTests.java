package com.pikaqiu;

import com.pikaqiu.statemachine.v2.OrderProcessor;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes = JavaApplication.class)
public class JavaApplicationTests {

    @Autowired
    private OrderProcessor orderProcessor;

    @Test
    public void orderProcessor(){
        orderProcessor.init();
    }
}
