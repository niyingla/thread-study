package com.pikaqiu.jdk8.java8;

@FunctionalInterface
public interface MyFun {

	public Integer getValue(Integer num);
	default String getName(){
		return "哈哈哈";
	}
}
