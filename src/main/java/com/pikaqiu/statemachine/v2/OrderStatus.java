package com.pikaqiu.statemachine.v2;

/**
 * <p> OrderStatus </p>
 *
 * @author xiaoye
 * @version 1.0
 * @date 2025/5/21 15:34
 */

public enum OrderStatus {
    //待支付，待发货，待收货，订单结束
    INIT , PAYED, WAIT_DELIVERY,CANNEL,I_CANNEL,SHIPPED, RECEIVED;
}
