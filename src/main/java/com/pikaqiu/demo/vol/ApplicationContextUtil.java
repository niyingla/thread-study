package com.pikaqiu.demo.vol;

import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * @date 2019-09-11
 */
@Component
public class ApplicationContextUtil implements ApplicationContextAware {
    /***
     * spring容器上下文对象
     */
    private static ApplicationContext applicationContext;

    @Override
    public void setApplicationContext(ApplicationContext applicationContext)  {
       ApplicationContextUtil.applicationContext = applicationContext;
    }

    /**
     * 通过bean的名称获取bean
     *
     * @param name bean名称
     * @return bean对象
     */
    public static Object getBean(String name) {
        return applicationContext.getBean(name);
    }

    /**
     * 通过bean类型动态获取bean
     *
     * @param clazz bean类型的class
     * @param <T>   bean的类型
     * @return 返回bean
     */
    public static <T> T getBean(Class<T> clazz) {
        return applicationContext.getBean(clazz);
    }

    public static <T> Map<String, T> getBeans(Class<T> clazz) {
        return applicationContext.getBeansOfType(clazz);
    }

    /**
     * 通过bean名称和类型动态获取bean
     *
     * @param name  bean名称
     * @param clazz bean类型的class
     * @param <T>   bean的类型
     * @return bean
     */
    public static <T> T getBean(String name, Class<T> clazz) {
        return applicationContext.getBean(name, clazz);
    }
}
