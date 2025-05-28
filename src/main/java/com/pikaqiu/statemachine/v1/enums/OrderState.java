package com.pikaqiu.statemachine.v1.enums;

/**
 * <p> OrderState </p>
 *
 * @author xiaoye
 * @version 1.0
 * @date 2025/5/21 14:46
 */
public enum OrderState {
    UNPAID,                 // 待支付
    WAITING_FOR_RECEIVE,    // 待收货
    DONE                    // 结束
}
