//package com.pikaqiu.statemachine.v1.test;
//
//import com.pikaqiu.statemachine.v1.dto.Order;
//import com.pikaqiu.statemachine.v1.enums.OrderEvents;
//import com.pikaqiu.statemachine.v1.enums.OrderState;
//import lombok.SneakyThrows;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.messaging.Message;
//import org.springframework.messaging.support.MessageBuilder;
//import org.springframework.scheduling.annotation.Async;
//import org.springframework.statemachine.StateMachine;
//
//import javax.annotation.PostConstruct;
//
///**
// * <p> StateTest </p>
// *
// * @author xiaoye
// * @version 1.0
// * @date 2025/5/21 14:55
// */
//
////@Component
//public class StateTest {
//
//    @Autowired
//    private StateMachine<OrderState, OrderEvents> stateMachine;
//
//    @Async
//    @PostConstruct
//    @SneakyThrows
//    public void testState() {
//
//
//        // 发送消息给状态机
//        stateMachine.start();
//        for (int i = 0; i < 100; i++) {
//
//
//            sendMessage(i);
//            Thread.sleep(1000);
//
//        }
//        stateMachine.stop();
//    }
//
//    private void sendMessage(Integer orderId) {
//        Order order = new Order("测试", orderId);
//        // 使用 MessageBuilder 创建消息并设置负载和头信息
//        Message message = MessageBuilder
//                .withPayload(OrderEvents.PAY)
//                .setHeader("order", order)
//                .build();
//        stateMachine.sendEvent(message);
//
//        Message message1 = MessageBuilder
//                .withPayload(OrderEvents.RECEIVE)
//                .setHeader("order", order)
//                .build();
//        stateMachine.sendEvent(message1);
//
//
//
//    }
//}
