package com.pikaqiu.utils;

/**
 * <p> sss </p>
 *
 * @author xiaoye
 * @version 1.0
 * @date 2025/8/28 17:33
 */
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Main {
    public static void main(String[] args) {
        String content = "我的手机号是+61 498 765 432多谢关心关心";
        String regex = "(\\+?)(?:\\+?61|61)\\s*4(\\s*\\d){8}";
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(content);

        while (matcher.find()) {
            String plusSign = matcher.group(1); // 提取+号
            String phoneNumber = plusSign + matcher.group(2) + matcher.group(3).replaceAll("\\s+", ""); // 组合手机号
            System.out.println("匹配到的手机号: " + phoneNumber);
        }
    }
}
