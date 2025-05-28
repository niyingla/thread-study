package com.pikaqiu.mock;

import org.junit.Test;
import org.mockito.ArgumentMatcher;
import org.mockito.InOrder;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import static org.mockito.Mockito.*;

/**
 * <p> MockTest </p>
 *
 * @author xiaoye
 * @version 1.0
 * @date 2025/5/28 10:49
 */
public class MockTest {
    /**
     * 我想学习Mockito的一些用法，帮我写n个测试方法，让我了解它的特性和功能
     */
    @Test
    public void test1() {
        //1、创建mock对象（模拟依赖的对象）
        final List mock = mock(List.class);

        //2、使用mock对象（mock对象会对接口或类的方法给出默认实现）
        System.out.println("mock.add result => " + mock.add("first"));  //false
        System.out.println("mock.size result => " + mock.size());       //0

        //3、打桩操作（状态测试：设置该对象指定方法被调用时的返回值）
        when(mock.get(0)).thenReturn("second");
        doReturn(66).when(mock).size();

        //3、使用mock对象的stub（测试打桩结果）
        System.out.println("mock.get result => " + mock.get(0));    //second
        System.out.println("mock.size result => " + mock.size());   //66

        //4、验证交互 verification（行为测试：验证方法调用情况）
        Mockito.verify(mock).get(Mockito.anyInt());
        Mockito.verify(mock, Mockito.times(2)).size();
    }

    @Test
    public void test2() {
        //静态导入，减少代码量：import static org.mockito.Mockito.*;
        final ArrayList mockList = mock(ArrayList.class);

        // 设置方法调用返回值
        when(mockList.add("test2")).thenReturn(true);
        doReturn(true).when(mockList).add("test2");
        System.out.println(mockList.add("test2"));  //true

        // 设置方法调用抛出异常
        when(mockList.get(0)).thenThrow(new RuntimeException());
        doThrow(new RuntimeException()).when(mockList).get(0);
        //System.out.println(mockList.get(0));    //throw RuntimeException

        // 无返回方法打桩
        doNothing().when(mockList).clear();

        // 为回调做测试桩（对方法返回进行拦截处理）
        final Answer<String> answer = new Answer<String>() {
            @Override
            public String answer(InvocationOnMock invocationOnMock) throws Throwable {
                final List mock = (List) invocationOnMock.getMock();
                return "mock.size result => " + mock.size();
            }
        };
        when(mockList.get(1)).thenAnswer(answer);
        doAnswer(answer).when(mockList).get(1);
        System.out.println(mockList.get(1));    //mock.size result => 0

        // 对同一方法多次打桩，以最后一次为准
        when(mockList.get(2)).thenReturn("test2_1");
        when(mockList.get(2)).thenReturn("test2_2");
        System.out.println(mockList.get(2));    //test2_2
        System.out.println(mockList.get(2));    //test2_2

        // 设置多次调用同类型结果
        when(mockList.get(3)).thenReturn("test2_1", "test2_2");
        when(mockList.get(3)).thenReturn("test2_1").thenReturn("test2_2");
        System.out.println(mockList.get(3));    //test2_1
        System.out.println(mockList.get(3));    //test2_2

        // 为连续调用做测试桩（为同一个函数调用的不同的返回值或异常做测试桩）
        when(mockList.get(4)).thenReturn("test2")
                .thenReturn("test3").thenThrow(new RuntimeException());
//        doReturn("test2").doThrow(new RuntimeException()).when(mockList).get(4);
        System.out.println(mockList.get(4));    //test2
        System.out.println(mockList.get(4));    //throw RuntimeException
//        System.out.println(mockList.get(4));    //throw RuntimeException

        // 无打桩方法，返回默认值
        System.out.println(mockList.get(99));    //null
    }


    @Test
    public void test3() {
        final Map mockMap = mock(Map.class);
        // 正常打桩测试
        when(mockMap.get("key")).thenReturn("value1");
        System.out.println(mockMap.get("key"));     //value1

        // 为灵活起见，可使用参数匹配器
        when(mockMap.get(anyString())).thenReturn("value2");
        System.out.println(mockMap.get(anyString()));   //value2
        System.out.println(mockMap.get("test_key"));    //value2
        System.out.println(mockMap.get(0)); //null

        // 多个入参时，要么都使用参数匹配器，要么都不使用，否则会异常
        when(mockMap.put(anyString(), anyInt())).thenReturn("value3");
        System.out.println(mockMap.put("key3", 3));     //value3
        System.out.println(mockMap.put(anyString(), anyInt()));     //value3
        System.out.println(mockMap.put("key3", anyInt()));    //异常

        // 行为验证时，也支持使用参数匹配器
        verify(mockMap, atLeastOnce()).get(anyString());
        verify(mockMap).put(anyString(), eq(3));

        // 自定义参数匹配器
        final ArgumentMatcher<ArgumentTestRequest> myArgumentMatcher = new ArgumentMatcher<ArgumentTestRequest>() {
            @Override
            public boolean matches(ArgumentTestRequest request) {
                return "name".equals(request.getName()) || "value".equals(request.getValue());
            }
        };
        // 自定义参数匹配器使用
        final ArgumentTestService mock = mock(ArgumentTestService.class);
        when(mock.argumentTestMethod(argThat(myArgumentMatcher))).thenReturn("success");
        doReturn("success").when(mock).argumentTestMethod(argThat(myArgumentMatcher));
        System.out.println(mock.argumentTestMethod(new ArgumentTestRequest("name", "value")));  // success
        System.out.println(mock.argumentTestMethod(new ArgumentTestRequest()));     //null
    }

    @Test
    public void test4() {
        // 验证同一个对象多个方法的执行顺序
        final List mockList = mock(List.class);
        mockList.add("first");
        mockList.add("second");
        final InOrder inOrder = inOrder(mockList);
        inOrder.verify(mockList).add("first");
        inOrder.verify(mockList).add("second");

        // 验证多个对象多个方法的执行顺序
        final List mockList1 = mock(List.class);
        final List mockList2 = mock(List.class);
        mockList1.get(0);
        mockList1.get(1);
        mockList2.get(0);
        mockList1.get(2);
        mockList2.get(1);
        final InOrder inOrder1 = inOrder(mockList1, mockList2);
        inOrder1.verify(mockList1).get(0);
        inOrder1.verify(mockList1).get(2);
        inOrder1.verify(mockList2).get(1);
    }


    @Test
    public void test7() {
        // stub部分mock（stub中使用真实调用）。注意：需要mock实现类，否则会有异常
        final StubTestService stubTestService = mock(StubTestServiceImpl.class);
        when(stubTestService.stubTestMethodA("paramA")).thenCallRealMethod();
        doCallRealMethod().when(stubTestService).stubTestMethodB();
        System.out.println(stubTestService.stubTestMethodA("paramA"));  //stubTestMethodA is called, param = paramA
        System.out.println(stubTestService.stubTestMethodB());  //stubTestMethodB is called
        System.out.println(stubTestService.stubTestMethodC());  //null

        // spy部分mock
        final LinkedList<String> linkedList = new LinkedList();
        final LinkedList spy = spy(linkedList);
        spy.add("one");
        spy.add("two");
        doReturn(100).when(spy).size();
        when(spy.get(0)).thenReturn("one_test");
        System.out.println(spy.size()); //100
        System.out.println(spy.get(0)); //one_test
        System.out.println(spy.get(1)); //two

        // spy可以类比AOP。在spy中，由于默认是调用真实方法，所以第二种写法不等价于第一种写法，不推荐这种写法。
        doReturn("two_test").when(spy).get(2);
        when(spy.get(2)).thenReturn("two_test"); //异常 java.lang.IndexOutOfBoundsException: Index: 2, Size: 2
        System.out.println(spy.get(2));   //two_test

        // spy对象只是真实对象的复制，真实对象的改变不会影响spy对象
        final List<String> arrayList = new ArrayList<>();
        final List<String> spy1 = spy(arrayList);
        spy1.add(0, "one");
        System.out.println(spy1.get(0));    //one
        arrayList.add(0, "list1");
        System.out.println(arrayList.get(0));   //list1
        System.out.println(spy1.get(0));    //one

        // 若对某个方法stub之后，又想调用真实的方法，可以使用reset(spy)
        final ArrayList<String> arrayList1 = new ArrayList<>();
        final ArrayList<String> spy2 = spy(arrayList1);
        doReturn(100).when(spy2).size();
        System.out.println(spy2.size());    //100
        reset(spy2);
        System.out.println(spy2.size());    //0
    }
}
