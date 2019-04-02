package com.pikaqiu.java8;

@FunctionalInterface
public interface MyFun {

	public Integer getValue(Integer num);
	default String getName(){
		return "哈哈哈";
	}
}
