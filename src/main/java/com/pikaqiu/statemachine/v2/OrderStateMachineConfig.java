package com.pikaqiu.statemachine.v2;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.statemachine.StateMachineContext;
import org.springframework.statemachine.StateMachinePersist;
import org.springframework.statemachine.config.EnableStateMachine;
import org.springframework.statemachine.config.StateMachineConfigurerAdapter;
import org.springframework.statemachine.config.builders.StateMachineStateConfigurer;
import org.springframework.statemachine.config.builders.StateMachineTransitionConfigurer;
import org.springframework.statemachine.persist.DefaultStateMachinePersister;
import org.springframework.statemachine.support.DefaultStateMachineContext;

import java.util.EnumSet;

/**
 * 订单状态机配置
 */
@Configuration
@EnableStateMachine(name = "orderStateMachine")
public class OrderStateMachineConfig extends StateMachineConfigurerAdapter<OrderStatus, OrderEvents> {

    /**
     * 配置状态
     *
     * @param states
     * @throws Exception
     */
    public void configure(StateMachineStateConfigurer<OrderStatus, OrderEvents> states) throws Exception {
        states.withStates()
                .initial(OrderStatus.INIT)
                .states(EnumSet.allOf(OrderStatus.class));
    }

    /**
     * 配置状态转换事件关系
     *
     * @param transitions
     * @throws Exception
     */
    public void configure(StateMachineTransitionConfigurer<OrderStatus, OrderEvents> transitions) throws Exception {
        transitions.withExternal().source(OrderStatus.INIT).target(OrderStatus.PAYED)
                .event(OrderEvents.PAY)
                .and()
                .withExternal().source(OrderStatus.PAYED).target(OrderStatus.WAIT_DELIVERY)
                .event(OrderEvents.DELIVERY)
                .and()
                .withExternal().source(OrderStatus.PAYED).target(OrderStatus.CANNEL)
                .event(OrderEvents.CANNEL)
                .and()
                .withExternal().source(OrderStatus.INIT).target(OrderStatus.CANNEL)
                .event(OrderEvents.CANNEL)
                .and()
                .withExternal().source(OrderStatus.WAIT_DELIVERY).target(OrderStatus.RECEIVED)
                .event(OrderEvents.RECEIVE);
    }

    /**
     * 持久化配置
     * 在实际使用中，可以配合Redis等进行持久化操作
     *
     * @return
     */
    @Bean
    public DefaultStateMachinePersister<OrderStatus, OrderEvents, Order> persister() {
        return new DefaultStateMachinePersister<>(new StateMachinePersist<OrderStatus, OrderEvents,Order>() {
            @Override
            public void write(StateMachineContext<OrderStatus, OrderEvents> context, Order order) throws Exception {
                System.out.println("写入持久化");
                //todo 持久化处理
            }

            /**
             * 前序状态从这里读取
             * @param order
             * @return
             * @throws Exception
             */
            @Override
            public StateMachineContext<OrderStatus, OrderEvents> read(Order order) throws Exception {
                //此处直接获取Order中的状态，其实并没有进行持久化读取操作
                return new DefaultStateMachineContext<>(order.getStatus(), null, null, null);
            }
        });
    }
}
