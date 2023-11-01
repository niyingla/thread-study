package com.pikaqiu.log;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * <p> LogCheck </p>
 *
 * @author xiaoye
 * @version 1.0
 * @date 2023/11/1 11:38
 */
public class LogCheck {
    public static void main(String[] args) {
        String filePath = "C:\\Users\\qiyu\\Desktop\\export-ae7a4b1f-3deb-43fc-8f33-8659483a65b4.json";
        String regexPattern = "\"message\":\"(.+)qiyu-douyin-auth \\[ INFO\\] \\[(.+)[a-zA-Z0-9.-],";

        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            String line;
            Pattern pattern = Pattern.compile(regexPattern);

            //创建集合 key trance value 第一次出现时间
            HashMap<String, LocalDateTime> datatime = new HashMap<>();
            HashMap<String, LocalDateTime> endtime = new HashMap<>();

            while ((line = reader.readLine()) != null) {
                Matcher matcher = pattern.matcher(line);

                while (matcher.find()) {
                    String matchedData = matcher.group();
                    //拿到数据 例如 "message":"2023-11-01 10:33:56.853 qiyu-douyin-auth [ INFO] [d70c20d4541c68f9,
                    //获取trance d70c20d4541c68f9 和时间 2023-11-01 10:33:56.853
                    String trace = matchedData.substring(61, 77);
                    String time = matchedData.substring(11, 34);
                    if (datatime.containsKey(trace)) {
                        endtime.put(trace, LocalDateTime.parse(time, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS")));
                    } else {
                        endtime.put(trace, LocalDateTime.parse(time, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS")));
                        datatime.put(trace, LocalDateTime.parse(time, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS")));
                    }
                }
            }

            //计算时间差，并根据时间差倒序排序写入集合
            HashMap<String, Long> timeMap = new HashMap<>();
            endtime.forEach((k, v) -> timeMap.put(k,  datatime.get(k).toEpochSecond(ZoneOffset.of("+8"))-endtime.get(k).toEpochSecond(ZoneOffset.of("+8")) ));

            //根据value倒序
            timeMap.entrySet().stream().sorted(Comparator.comparing(Map.Entry::getValue)).forEach(System.out::println);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
