package com.pikaqiu.demo.learnEnum;

import lombok.Data;
import org.junit.Assert;
import org.junit.Test;

/**
 * @program: java-study
 * @description:
 * @author: xiaoye
 * @create: 2020-05-07 16:10
 **/
@Data
public class Pizza {

  /**
   * 当前使用的枚举类
   */
  private PizzaStatus status;

  public enum PizzaStatus {
    ORDERED (5){
      @Override
      public boolean isOrdered() {
        return true;
      }
    },
    READY (2){
      @Override
      public boolean isReady() {
        return true;
      }
    },
    DELIVERED (0){
      @Override
      public boolean isDelivered() {
        return true;
      }
    };

    private int timeToDelivery;

    public boolean isOrdered() {return false;}

    public boolean isReady() {return false;}

    public boolean isDelivered(){return false;}

    public int getTimeToDelivery() {
      return timeToDelivery;
    }

    /**
     * 构造方法
     * @param timeToDelivery
     */
    PizzaStatus (int timeToDelivery) {
      this.timeToDelivery = timeToDelivery;
    }
  }

  public boolean isDeliverable() {
    return this.status.isReady();
  }

  public void printTimeToDeliver() {
    System.out.println("Time to delivery is " +
        this.getStatus().getTimeToDelivery());
  }
  public void deliver() {
    if (isDeliverable()) {
      //获取策略
      PizzaDeliverySystemConfiguration.getInstance().getDeliveryStrategy()
          //执行指定策略的内容
          .deliver(this);
      this.setStatus(PizzaStatus.DELIVERED);
    }
  }
  @Test
  public void givenPizaOrder_whenReady_thenDeliverable() {
    Pizza testPz = new Pizza();
    testPz.setStatus(Pizza.PizzaStatus.READY);
    Assert.assertTrue(testPz.isDeliverable());
  }

  @Test
  public void givenPizaOrder_whenDelivered_thenPizzaGetsDeliveredAndStatusChanges() {
    Pizza pz = new Pizza();
    pz.setStatus(Pizza.PizzaStatus.READY);
    pz.deliver();
    Assert.assertTrue(pz.getStatus() == Pizza.PizzaStatus.DELIVERED);
  }
}
