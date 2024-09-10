package com.pikaqiu.cache;


import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;

import java.util.concurrent.TimeUnit;

public class CaffeineCacheExample {
    public static void main(String[] args) {
        // 创建一个缓存，设置写入后过期时间为5秒，并添加移除监听器
        Cache<String, String> cache = Caffeine.newBuilder()
                .expireAfterWrite(5, TimeUnit.SECONDS)
                .removalListener((key, value, cause) ->
                        System.out.println("Key: " + key + ", Value: " + value + ", Cause: " + cause))
                .build();

        // 添加缓存项
        cache.put("key1", "value1");

        // 模拟等待一段时间，超过过期时间
        try {
            TimeUnit.SECONDS.sleep(6);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        cache.put("key2", "value1");
        try {
            TimeUnit.SECONDS.sleep(1);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        cache.put("key3", "value1");

        try {
            TimeUnit.SECONDS.sleep(2);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        cache.put("key4", "value1");
        try {
            TimeUnit.SECONDS.sleep(3);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        // 尝试获取缓存项，触发移除事件
        String value = cache.getIfPresent("key1");
        System.out.println("Cached value after 6 seconds: " + value);
    }
}
