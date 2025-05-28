package com.pikaqiu.mock;

/**
 * <p> ArgumentTestService </p>
 *
 * @author xiaoye
 * @version 1.0
 * @date 2025/5/28 10:59
 */
public class ArgumentTestService {
    public String argumentTestMethod(ArgumentTestRequest argumentTestRequest) {
        return argumentTestRequest.getName() + argumentTestRequest.getValue();
    }
}
