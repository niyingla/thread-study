package com.pikaqiu.statemachine.v2;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.statemachine.StateMachine;
import org.springframework.statemachine.persist.StateMachinePersister;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
 * <p> OrderProcessor </p>
 *
 * @author xiaoye
 * @version 1.0
 * @date 2025/5/21 15:37
 */

@Component("orderProcessor")
@Slf4j
public class OrderProcessor {

    @Resource
    private StateMachine<OrderStatus, OrderEvents> orderStateMachine;

    @Resource
    private StateMachinePersister<OrderStatus, OrderEvents, Order> persister;

    public Boolean process(Order order, OrderEvents event) {
        Message<OrderEvents> message = MessageBuilder.withPayload(event)
                .setHeader("order", order).build();
        boolean b = sendEvent(message);
        return b;
    }

    @SneakyThrows
    public void init() {
        for (int i = 0; i < 1; i++) {
            Order order = new Order();
            order.setId(i);
            order.setStatus(OrderStatus.INIT);
            process(order, OrderEvents.PAY);

            Thread.sleep(1000);
            Order order2 = new Order();
            order2.setId(i);
            order2.setStatus(OrderStatus.INIT);
            process(order2, OrderEvents.CANNEL);

            Thread.sleep(1000);
            Order order1 = new Order();
            order1.setId(i);
            order1.setStatus(OrderStatus.PAYED);
            process(order1, OrderEvents.CANNEL);
        }
    }

    @SneakyThrows
    private boolean sendEvent(Message<OrderEvents> message) {
        Order order = (Order) message.getHeaders().get("order");
        //状态机开始
//        orderStateMachine.start();
        //重置状态
        persister.restore(orderStateMachine, order);
        //发送变更事件
        boolean result = orderStateMachine.sendEvent(message);
        //持久化事件
        persister.persist(orderStateMachine, order);
        //状态机停止
//        orderStateMachine.stop();
        return result;
    }
}
