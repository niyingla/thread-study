package com.pikaqiu.map_struct;
import java.util.Date;

/**
 * <p> test </p>
 *
 * @author xiaoye
 * @version 1.0
 * @date 2020/11/18 17:00
 */
public class Test {
  public static void main(String[] args) {

    PersonDTO personDO = new PersonDTO();
    personDO.setUserName("南京");
    personDO.setBirthday(new Date());
    personDO.setAge(20);
    personDO.setAddress("北京");
    PersonDO personDTO = PersonConvert.INSTANCE.dto2do(personDO);

    System.out.println(personDTO);

  }
}
