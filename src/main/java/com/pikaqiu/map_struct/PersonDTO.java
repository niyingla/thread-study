package com.pikaqiu.map_struct;

import lombok.Data;

import java.util.Date;

/**
 * <p> PersonDTO </p>
 *
 * @author xiaoye
 * @version 1.0
 * @date 2020/11/18 16:52
 */
@Data
public class PersonDTO {
  private String userName;
  private Date birthday;
  private Integer age;
  private String address;
}
