package com.pikaqiu.map_struct;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;
@Mapper
public interface PersonConvert {

  PersonConvert INSTANCE = Mappers.getMapper(PersonConvert.class);

  @Mapping(source = "userName", target = "name")
  @Mapping(target = "birthday",dateFormat = "yyyy-MM-dd HH:mm:ss")
  @Mapping(target = "address",expression = "java(homeAddressToString(dto2do.getAddress()))")
  PersonDO dto2do(PersonDTO dto2do);

  default String homeAddressToString(String address){
    return address + "哈哈";
  }

}
