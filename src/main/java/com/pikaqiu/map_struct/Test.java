package com.pikaqiu.map_struct;
import org.springframework.core.annotation.AnnotationUtils;

import java.util.Date;
import java.util.Map;

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

//    Map<String, AbstractChannelAdd> beans = webApplicationContext.getBeansOfType(AbstractChannelAdd.class);
//    for (Map.Entry<String, AbstractChannelAdd> entry : beans.entrySet()) {
//    AbstractChannelAdd strategy = entry.getValue();
//    StrategyType strategyType = AnnotationUtils.findAnnotation(strategy.getClass(), StrategyType.class);
//    if (strategyType != null) {
//      WelcomeStateDTO.WelcomeStateEnum type = strategyType.type();
//      if (!strategyTypeMap.containsKey(type.getBusinessType())) {
//        strategyTypeMap.put(type.getBusinessType(), strategy);
//      }
//    }
//  }
}
