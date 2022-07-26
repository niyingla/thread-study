package com.pikaqiu.exer;

@FunctionalInterface
public interface MyFunction<K,V> {

	V getValue(String K);

}
