package com.pikaqiu.My;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.util.concurrent.RateLimiter;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Random;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

/**
 * <p> Limite </p>
 *
 * @author xiaoye
 * @version 1.0
 * @date 2020/8/28 17:49
 */
public class Limite {

  private static int totalCount = 0;
  // 根据IP分不同的令牌桶, 每天自动清理缓存
  private static LoadingCache<String, RateLimiter> caches = CacheBuilder.newBuilder()
      .maximumSize(5)
      .expireAfterWrite(1, TimeUnit.DAYS)
      .build(new CacheLoader<String, RateLimiter>() {
        @Override
        public RateLimiter load(String key) throws Exception {
          // 新的IP初始化 (限流每秒两个令牌响应)
          return RateLimiter.create(2);
        }
      });

  private static void login(int i) throws ExecutionException {
    // 模拟IP的key
    String ip = String.valueOf(i).charAt(0) + "";
    RateLimiter limiter = caches.get(ip);

    if (limiter.tryAcquire()) {
      System.out.println(i + " success " + new SimpleDateFormat("HH:mm:ss.sss").format(new Date()));
    } else {
      System.out.println(i + " failed " + new SimpleDateFormat("HH:mm:ss.sss").format(new Date()));
    }
  }

  public static void main(String[] args) throws Exception{
//    limit();
//    redPackage1();
    redPackage2();


  }


  private static void limit() throws InterruptedException, ExecutionException {
    for (int i = 0; i < 1100; i++) {
      // 模拟实际业务请求
      Thread.sleep(100);
      login(i);
    }
  }

  private static  void redPackage1(){
    int total = 1500;
    int num = 8;
    int min = 1;
    for (int i = 1; i < num; i++) {
      int safeTotal = (total - (num - i) * min) / (num - i);
      Random random = new Random();
      int money = random.nextInt(safeTotal) % (safeTotal -  min + 1) +  min;
      total -= (money);
      System.out.println("第" + i + "次 红包数额 " + (double) money / 100 + "元 安全总数 " + (double) safeTotal / 100);
    }
    System.out.println("第" + num + "次 红包数额 " + (double) total / 100 + "元 安全总数 " + (double) total / 100);
  }

  private static void redPackage2() {
    double total = 0.3;
    int num = 8;
    double min = 0.01;
    double max = 200;
    for (int i = 1; i < num; i++) {
      double money = 正态分布(total * 6 / (num - i + 1), total / (num - i + 1), min, max, total, num - i);
      total -= (money);
      System.out.println("第" + i + "次 红包数额 " + money + "元 ");
    }
    System.out.println("第" + num + "次 红包数额 " + (double) Math.round(total * 100) / 100 + "元 共计算 " + totalCount);
  }


  private static double 正态分布(double a, double average, double min, double max, double total,int remain) {
    totalCount++;
    Random random = new Random();
    if ( total / ((remain + 1) * max) >= 0.99) {
      return max;
    }
    if ( total / ((remain + 1) * min) <= 2) {
      return min;
    }
    if ( total / ((remain + 1) * min) == 1) {
      return min;
    }
    double value = Math.sqrt(a) * random.nextGaussian() + average;

    if (value >= max || value <= min || total - value > remain * max || total - value <= 0||total - value < remain * min) {
      return 正态分布(a, average, min, max, total, remain);
    }
    return (double) Math.round(value * 100) / 100;
  }



}
