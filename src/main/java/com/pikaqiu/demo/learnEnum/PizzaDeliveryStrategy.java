package com.pikaqiu.demo.learnEnum;

/**
 * @program: java-study
 * @description:
 * @author: xiaoye
 * @create: 2020-05-07 16:47
 **/
public enum PizzaDeliveryStrategy {
  EXPRESS {
    @Override
    public void deliver(Pizza pz) {
      System.out.println("Pizza will be delivered in express mode");
    }
  },
  NORMAL {
    @Override
    public void deliver(Pizza pz) {
      System.out.println("Pizza will be delivered in normal mode");
    }
  };

  public abstract void deliver(Pizza pz);
}
