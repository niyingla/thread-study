package com.pikaqiu.spring.utils;

import com.pikaqiu.java8.Employee;

import java.lang.reflect.Method;
import java.util.Objects;

/**
 * <p> TestService </p>
 *
 * @author xiaoye
 * @version 1.0
 * @date 2023/7/31 17:32
 */
public class TestService {

    public void testEmployee(Employee employee){
    }

    public static void main(String[] args) {
        Method[] methods = TestService.class.getMethods();
        for (Method method : methods) {
            if (Objects.equals(method.getName(), "testEmployee")) {
                Object[] arg = new Object[1];
                Employee employee = new Employee();
                employee.setName("this is name");
                arg[0] = employee;
                String lockKey = DynamicParamParser.handle(method, arg, "sffxzh:" + "${employee.name}");
                System.out.println(lockKey);
            }
        }
    }
}
