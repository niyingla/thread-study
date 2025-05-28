package com.pikaqiu.statemachine.v2;

import lombok.Data;

/**
 * <p> Order </p>
 *
 * @author xiaoye
 * @version 1.0
 * @date 2025/5/21 15:34
 */
@Data
public class Order {
    private Integer id;

    private OrderStatus status;
}
