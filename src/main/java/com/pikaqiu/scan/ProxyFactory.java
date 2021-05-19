package com.apec.rpc;

import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.Proxy;

/**
 * @author xiaoye
 */
@Slf4j
public class ProxyFactory {
  /**
   * 获取接口代理
   *
   * @param interfaceClass
   * @param <T>
   * @return
   */
//  public static <T> T getInterfaceInfo(Class<T> interfaceClass) {
//    Class[] interfaceClassArray = new Class[]{interfaceClass};
//    T server = (T) Proxy.newProxyInstance(interfaceClass.getClassLoader(), interfaceClassArray, new RpcFactory());
//    return server;
//  }
}
