package com.pikaqiu.mock;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * <p> ArgumentTestRequest </p>
 *
 * @author xiaoye
 * @version 1.0
 * @date 2025/5/28 10:59
 */
@AllArgsConstructor
@NoArgsConstructor
@Data
public class ArgumentTestRequest {
    private String name;

    private String value;
}
