package com.pikaqiu.demo;

import java.util.*;
import java.util.regex.*;

public class SqlInsertParser {

    public static String parseAndRebuildInsertSql(String sql, List<String> fieldsToRemove, Map<String, Object> replacements) {
        sql = sql.replaceAll("`", "");
        // 正则表达式匹配插入语句，包括数据库和表名的处理
        Pattern pattern = Pattern.compile("INSERT INTO\\s+(\\w+)\\s*\\((.*?)\\)\\s+VALUES\\s*\\((.*?)\\);", Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(sql);

        if (matcher.find()) {
            String tableName = matcher.group(1);
            String[] fieldNames = matcher.group(2).split(",\\s*");
            String[] values = matcher.group(3).split(",\\s*");

            Map<String, Object> fieldMap = new LinkedHashMap<>();
            for (int i = 0; i < fieldNames.length; i++) {
                String fieldName = fieldNames[i].trim();
                fieldName = fieldName.replaceAll("`", "");
                fieldMap.put(fieldName, values[i].trim());
            }

            // 删除字段
            for (String field : fieldsToRemove) {
                fieldMap.remove(field);
            }

            // 替换字段值
            for (Map.Entry<String, Object> entry : replacements.entrySet()) {
                fieldMap.put(entry.getKey(), entry.getValue());
            }

            // 重新组成SQL，保持表名及结构不变
            String newFields = String.join(", ", fieldMap.keySet());
            String newValues = "";
            for (Object value : fieldMap.values()) {
                newValues += "," + value;
            }
            newValues = newValues.replaceFirst(",", "");
            return String.format("INSERT INTO %s (%s) VALUES (%s);", tableName, newFields, newValues);
        }

        throw new IllegalArgumentException("Invalid SQL Insert Statement");
    }

    public static void main(String[] args) {
        String sql = "INSERT INTO `dy_role`(`role_id`, `name`, `login_auth`, `role_source`, `role_code`, `create_time`, `update_time`, `create_user`, `update_user`, `is_enable`, `is_deleted`, `tenant_id`, `role_type`) VALUES (1, '超级管理员', '1,2', 1, 'super', '2022-04-14 13:38:19', '2024-03-19 14:45:42', NULL, NULL, 1, 0, 0, 1);";
        List<String> fieldsToRemove = Arrays.asList("id");
        Map<String, Object> replacements = new HashMap<>();
        replacements.put("tenant_id", 123);

        String newSql = parseAndRebuildInsertSql(sql, fieldsToRemove, replacements);
        System.out.println(newSql);
    }
}
