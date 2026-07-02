package com.pikaqiu.utils;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.ArrayList;
import java.util.List;

public class AustralianPhoneNumberExtractor {
    public static void main(String[] args) {
        String text = "联系我：0412 345 678 或 (04)1234-5678，紧急：+61-23456789，座机：(02)9876-5432";
        List<String> phoneNumbers = extractAustralianPhoneNumbers(text);

        System.out.println("提取到的澳大利亚手机号:");
        for (String number : phoneNumbers) {
            System.out.println(number);
        }
    }


    public static List<String> extractAustralianPhoneNumbers(String text) {
        List<String> phoneNumbers = new ArrayList<>();
        // 改进的正则表达式：
        // 支持 (04)XXXX-XXXX 格式和所有常见变体
//        String regex = "\\b(?:\\(?04\\)?[)\\s-]*\\d{4}[\\s-]*\\d{4}|04[\\s-]*\\d{2}[\\s-]*\\d{3}[\\s-]*\\d{3})\\b";
        String regex = "(?:\\+61|0)(?:[\\s-]?\\d){9}$";
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(text);

        while (matcher.find()) {
            // 移除所有非数字字符（保留纯数字格式）
            String cleanNumber = matcher.group().replaceAll("[^0-9]", "");
            // 确保是10位数字（04开头）
            if (cleanNumber.length() == 10 && cleanNumber.startsWith("04")) {
                phoneNumbers.add(cleanNumber);
            }

        }
        return phoneNumbers;
    }
}
