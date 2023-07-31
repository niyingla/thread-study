package com.pikaqiu.spring.utils;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.core.LocalVariableTableParameterNameDiscoverer;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * TODO
 *处理注解内部的的动态参数
 * 比如 @XXXAnnotation(user:${name}mm${age})
 * 这样的注解，写在方法上,会从方法参数上去拿对应的参数值
 * @author 程昭斌
 * @date 2020/4/3 14:23
 */
@Slf4j
public class DynamicParamParser {
    static LocalVariableTableParameterNameDiscoverer parameterNameDiscoverer=new LocalVariableTableParameterNameDiscoverer();
    private final static String PREFIX="${";
    private final static String SUFFIX="}";
    private final static Pattern regex = Pattern.compile("\\$\\{([^}]*)\\}");
    private static boolean isBaseType(Class className, boolean isBaseStr) {
        if (isBaseStr && className.equals(String.class)) {
            return true;
        }
        return className.equals(Integer.class) ||
                className.equals(int.class) ||
                className.equals(Byte.class) ||
                className.equals(byte.class) ||
                className.equals(Long.class) ||
                className.equals(long.class) ||
                className.equals(Double.class) ||
                className.equals(double.class) ||
                className.equals(Float.class) ||
                className.equals(float.class) ||
                className.equals(Character.class) ||
                className.equals(char.class) ||
                className.equals(Short.class) ||
                className.equals(short.class) ||
                className.equals(Boolean.class) ||
                className.equals(boolean.class);
    }
public static boolean isDynameicParam(String var){
        return var.contains(PREFIX);
}
    /**
     * @xxAnnotation(xx${name}yy${mm})
     * public void test(String age,String name,String mm,User user){}
     *  其中
     *  User{
     *  private String name;
     *  private String age;
     *  }
     *  如上，会直接从第一个参数开始获取${name}的值,如诺不空则直接返回取到的值，否则继续往下找
     * @param  method 目标方法
     * @param args 目标方法的参数集合
     * @param var 注解中获取的变量字符串
     */
    public static String handle(Method method, Object[] args, String var) {
        if(method==null||args==null|| StringUtils.isBlank(var)){
            return "";
        }
        if(!isDynameicParam(var)){
            //不是动态参数,直接原样返回
            return var;
        }
        log.info("处理前的动态参数:{}",var);
        Matcher matcher = regex.matcher(var);
        List<String> varList=new ArrayList<>();
        List<Object> valList=new ArrayList<>();
        while(matcher.find()) {
            varList.add(matcher.group(1));
        }
        for(String v:varList){
            Object o = parserVar(method, args,v);
            if(o==null){
                //若没有对应取值，则用原串替换
                valList.add(v);
            }else{
                valList.add(o);
            }
        }
        String pattern=var.replaceAll(regex.toString(),"%s");
        String resolvedVar = String.format(pattern, valList.toArray());
        log.info("处理后的动态参数:{}",resolvedVar);
        return resolvedVar;
    }
    private static Object parserVar(Method method, Object[] objects, String var){
        Class[] parameterTypes = method.getParameterTypes();
        String[] parameterNames = parameterNameDiscoverer.getParameterNames(method);
        Object parsredVar = null;
        for (int i = 0; i < parameterTypes.length; i++) {
            Class clazz = parameterTypes[i];
            String paraName = parameterNames[i];
            if (isBaseType(clazz, true)) {
                if (paraName.equals(var)) {
                    parsredVar = objects[i];
                    if (parsredVar != null) {
                        break;
                    }
                }
            } else {
                parsredVar = handleCustomerObj(objects[i], paraName, var);
                if (parsredVar != null) {
                    break;
                }
            }
        }
        return parsredVar;
    }
    private static Object handleCustomerObj(Object customerObj, String objectName, String var) {
        //处理自定义类型
        Object val = null;
        Field[] declaredFields = customerObj.getClass().getDeclaredFields();
        for (Field field : declaredFields) {
            if ((objectName +"."+ field.getName()).equals(var)) {
                field.setAccessible(true);
                try {
                    val = field.get(customerObj);
                    break;
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        return val;
    }
}
