package com.pikaqiu.statemachine.v2;

import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.Message;
import org.springframework.statemachine.annotation.OnTransition;
import org.springframework.statemachine.annotation.WithStateMachine;
import org.springframework.stereotype.Component;

/**
 * <p> OrderStateListener </p>
 *
 * @author xiaoye
 * @version 1.0
 * @date 2025/5/21 15:37
 */
@Component("orderStateListener")
@Slf4j
@WithStateMachine(name = "orderStateMachine")
public class OrderStateListener {

    @OnTransition(source = "INIT", target = "PAYED")
    public boolean pay(Message<OrderEvents> message) {
        Order order = (Order) message.getHeaders().get("order");
        order.setStatus(OrderStatus.PAYED);
        log.info("订单表保存数据");
        log.info("发送站内消息");
        log.info("回调支付接口");
        log.info("同步至数据仓库");
        return true;
    }

    @OnTransition(source = "INIT", target = "WAIT_DELIVERY")
    public boolean delivery(Message<OrderEvents> message) {
        Order order = (Order) message.getHeaders().get("order");
        order.setStatus(OrderStatus.WAIT_DELIVERY);
        log.info("创建物流订单");
        log.info("更新物流订单状态");
        log.info("同步至数据仓库");
        return true;
    }

    @OnTransition(source = "PAYED", target = "CANNEL")
    public boolean cannel(Message<OrderEvents> message) {
        Order order = (Order) message.getHeaders().get("order");
        order.setStatus(OrderStatus.CANNEL);
        log.info("取消订单");
        return true;
    }

    @OnTransition(source = "INIT", target = "CANNEL")
    public boolean i_cannel(Message<OrderEvents> message) {
        Order order = (Order) message.getHeaders().get("order");
        order.setStatus(OrderStatus.CANNEL);
        log.info("初始化取消订单");
        return true;
    }

    @OnTransition(source = "WAIT_DELIVERY", target = "RECEIVED")
    public boolean receive(Message<OrderEvents> message) {
        Order order = (Order) message.getHeaders().get("order");
        order.setStatus(OrderStatus.PAYED);
        log.info("更新订单数据");
        log.info("同步至数据仓库");
        return true;
    }
}
