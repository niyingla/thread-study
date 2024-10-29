package com.pikaqiu.demo;

import java.sql.*;

public class SchemaValidator {
    private static final String DATABASE_URL = "jdbc:mysql://mysql-5632a4182a17-public.rds.volces.com/newlink197?useUnicode=true&characterEncoding=utf8&serverTimezone=Asia/Shanghai&useSSL=false&autoReconnect=true";
    private static final String DATABASE_USERNAME = "newlinktest";
    private static final String DATABASE_PASSWORD = "GRvDoXG#hh338x42";

    public static void test(){
        checkTables();
    }

    public static void checkTables() {
        try (Connection conn = DriverManager.getConnection(DATABASE_URL, DATABASE_USERNAME, DATABASE_PASSWORD);
             Statement stmt = conn.createStatement()) {

            // 查询所有表名
            ResultSet rs = stmt.executeQuery("SHOW TABLES");
            while (rs.next()) {
                String tableName = rs.getString(1);
                validateTable(conn, tableName);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private static void validateTable(Connection conn, String tableName) throws SQLException {
        DatabaseMetaData metaData = conn.getMetaData();
        ResultSet columns = metaData.getColumns(null, null, tableName, null);

        boolean hasTenantId = false;
        boolean hasNonPrimaryUniqueKey = false;

        while (columns.next()) {
            String columnName = columns.getString("COLUMN_NAME");
            if ("tenant_id".equalsIgnoreCase(columnName)) {
                hasTenantId = true;
            }
        }

        ResultSet uniqueKeys = metaData.getIndexInfo(null, null, tableName, true, false);
        while (uniqueKeys.next()) {
            //获取当前表的所有UNIQUE KEY
            String keyName = uniqueKeys.getString("INDEX_NAME");
            String nonUnique = uniqueKeys.getString("NON_UNIQUE");
            //获取唯一键包含的字段

            if ("false".equals(nonUnique)) {
                // 如果不是主键并且是唯一键
                if (!keyName.equalsIgnoreCase("PRIMARY")) {
                    hasNonPrimaryUniqueKey = true;
                }
            }
        }

        if (!hasTenantId) {
            System.out.println("Table " + tableName + " does not have tenant_id column.");
        }
        if (hasNonPrimaryUniqueKey) {
            System.out.println("Table " + tableName + " has unique keys other than primary key.");
        }
    }
}
