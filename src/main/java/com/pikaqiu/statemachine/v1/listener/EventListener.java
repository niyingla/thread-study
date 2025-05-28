//package com.pikaqiu.statemachine.v1.listener;
//
//import com.pikaqiu.statemachine.v1.dto.Order;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.messaging.Message;
//import org.springframework.statemachine.annotation.OnTransition;
//import org.springframework.statemachine.annotation.WithStateMachine;
//import org.springframework.stereotype.Component;
//
///**
// * <p> EventListener </p>
// *
// * @author xiaoye
// * @version 1.0
// * @date 2025/5/21 14:50
// */
//@Slf4j
//@Component
//@WithStateMachine(name = "StateMachineConfig")
//public class EventListener {
//
//    @OnTransition(target = "UNPAID")
//    public boolean create(Message<Order> order) {
// // 创建订单逻辑
//        System.out.println("订单创建，待支付");
//        return true;
//    }
//
//    @OnTransition(source = "UNPAID", target = "WAITING_FOR_RECEIVE")
//    public boolean pay(Message<Order> order) {
//        // 支付逻辑 从redis根据order 来进行处理
//        System.out.println("用户完成支付，待收货");
//        return true;
//    }
//
//    @OnTransition(source = "WAITING_FOR_RECEIVE", target = "DONE")
//    public boolean receive(Message<Order> order) {
////从redis中根据传入的order 来查询当前订单 并业务处理
//        System.out.println("用户已收货，订单完成 order:" + order);
//        return true;
//    }
//}
