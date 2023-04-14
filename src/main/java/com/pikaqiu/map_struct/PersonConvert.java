package com.pikaqiu.map_struct;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;
import org.mapstruct.Named;
import org.mapstruct.factory.Mappers;
@Mapper
public interface PersonConvert {

  PersonConvert INSTANCE = Mappers.getMapper(PersonConvert.class);

  @Mappings({
      @Mapping(source = "userName", target = "name"),
      @Mapping(target = "birthday", dateFormat = "yyyy-MM-dd HH:mm:ss"),
      @Mapping(target = "address", expression = "java(homeAddressToString(dto2do.getAddress()))")
  })
  PersonDO dto2do(PersonDTO dto2do);

  /**
   * 自定义方法
   * @param address
   * @return
   * @Named("address") 用于指定使用方法的字段
   */
  @Named("address")
  default String homeAddressToString(String address){
    return address + "哈哈";
  }

}
