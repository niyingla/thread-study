package com.pikaqiu.drools.dto;

import com.pikaqiu.drools.enums.CustomerType;
import lombok.Getter;
import lombok.Setter;

/**
 * <p> OrderRequest </p>
 *
 * @author xiaoye
 * @version 1.0
 * @date 2023/5/10 11:21
 */
@Getter
@Setter
public class OrderRequest {
    /**
     * 客户号
     */
    private String customerNumber;
    /**
     * 年龄
     */
    private Integer age;
    /**
     * 订单金额
     */
    private Integer amount;
    /**
     * 客户类型
     */
    private CustomerType customerType;
}
