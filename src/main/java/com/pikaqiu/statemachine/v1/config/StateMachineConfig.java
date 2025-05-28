package com.pikaqiu.statemachine.v1.config;

import com.pikaqiu.statemachine.v1.enums.OrderEvents;
import com.pikaqiu.statemachine.v1.enums.OrderState;
import org.springframework.context.annotation.Configuration;
import org.springframework.statemachine.config.EnableStateMachine;
import org.springframework.statemachine.config.EnumStateMachineConfigurerAdapter;
import org.springframework.statemachine.config.builders.StateMachineStateConfigurer;
import org.springframework.statemachine.config.builders.StateMachineTransitionConfigurer;

import java.util.EnumSet;

/**
 * <p> StateMachineConfig </p>
 *
 * @author xiaoye
 * @version 1.0
 * @date 2025/5/21 14:46
 */
@Configuration
@EnableStateMachine(name = "StateMachineConfig")
public class StateMachineConfig extends EnumStateMachineConfigurerAdapter<OrderState, OrderEvents> {

    //定义初始状态
    @Override
    public void configure(StateMachineStateConfigurer<OrderState, OrderEvents> states)
            throws Exception {
        states
                .withStates()
                .initial(OrderState.UNPAID)
                .states(EnumSet.allOf(OrderState.class));
    }

    //状态转换过程 触发什么事件就转换为什么状态
    @Override
    public void configure(StateMachineTransitionConfigurer<OrderState, OrderEvents> transitions)
            throws Exception {
        transitions
                .withExternal()
                .source(OrderState.UNPAID).target(OrderState.WAITING_FOR_RECEIVE)
                .event(OrderEvents.PAY)
                .and()
                .withExternal()
                .source(OrderState.WAITING_FOR_RECEIVE).target(OrderState.DONE)
                .event(OrderEvents.RECEIVE);
    }
}
