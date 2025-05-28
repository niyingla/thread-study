package com.pikaqiu.mock;

/**
 * <p> StubTestServiceImpl </p>
 *
 * @author xiaoye
 * @version 1.0
 * @date 2025/5/28 11:36
 */
public class StubTestServiceImpl implements StubTestService{

    @Override
    public String stubTestMethodA(String paramA) {
        System.out.println("stubTestMethodA call");
        return "false";
    }

    @Override
    public boolean stubTestMethodB() {
        System.out.println("stubTestMethodB call");
        return false;
    }

    @Override
    public boolean stubTestMethodC() {
        return false;
    }
}
