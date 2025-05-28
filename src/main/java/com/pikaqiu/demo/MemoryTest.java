package com.pikaqiu.demo;

import com.google.common.collect.Lists;
import com.pikaqiu.drools.dto.OrderDiscount;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * <p> MemoryTest </p>
 *
 * @author xiaoye
 * @version 1.0
 * @date 2024/10/29 11:07
 */
@Service
@Slf4j
public class MemoryTest {
    List list = Lists.newArrayList();
//    @PostConstruct
    public void createOb(){
        for (int i = 0; i < 1000; i++) {
            for (int j = 0; j < 1000; j++) {
                String[][] strings = new String[100][1000];
                list.add(new OrderDiscount());
                list.add(strings);
            }
        }
    }
}
