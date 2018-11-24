package com.example.text.demo.guava;

import com.google.common.cache.*;

import java.util.concurrent.TimeUnit;

/**
 * @program: text
 * @description: guava缓存
 * @author: xiaoye
 * @create: 2018-11-15 11:51
 **/
public class GuavaCacheBuilderTest {

    public static void main(String[] args) throws Exception {
        GuavaCacheBuilderTest cache = new GuavaCacheBuilderTest();
        cache.getNameLoadingCache("bixiao");
    }

    /**
     *
     * loading 是一个加载缓存的获取方法
     * expireAfterWrite是在指定项在一定时间内没有创建/覆盖时，会移除该key，下次取的时候从loading中取
     * expireAfterAccess是指定项在一定时间内没有读写，会移除该key，下次取的时候从loading中取
     * refreshAfterWrite是在指定时间内没有被创建/覆盖，则指定时间过后，再次访问时，会去刷新该缓存，在新值没有到来之前，始终返回旧值
     * 跟expire的区别是，指定时间过后，expire是remove该key，下次访问是同步去获取返回新值
     * 而refresh则是指定时间后，不会remove该key，下次访问会触发刷新，新值没有回来时返回旧值
     * 当Guava Cache中没有key时，将会通过其load方法加载数据，并保存在Cache中。load方法不能返回NULL。
     * @param name
     * @throws Exception
     */
    public void getNameLoadingCache(String name) throws Exception {

        LoadingCache cache = CacheBuilder.newBuilder()
                //设置大小，条目数
                .maximumSize(20)
                .expireAfterWrite(2, TimeUnit.SECONDS)
                .expireAfterAccess(2, TimeUnit.SECONDS)
                .removalListener(new RemovalListener() {
                    @Override
                    public void onRemoval(RemovalNotification notification) {

                        System.out.println(notification.getKey() + "------有缓存数据被移除了");
                    }
                })
                .build(new CacheLoader() {
                    @Override
                    public Object load(Object o) {
                        System.out.println("------load------");
                        return name + "-" + "wo shi pi ka qiu";
                    } //通过回调加载缓存
                });

        System.out.println(cache.get("aaa"));
        Thread.sleep(3000);

        System.out.println(cache.get(name));

        System.out.println(cache.get("aaa"));
    }
}


