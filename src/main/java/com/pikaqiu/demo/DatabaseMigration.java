package com.pikaqiu.demo;

import java.sql.*;

public class DatabaseMigration {
    private static final String SOURCE_DB_URL = "jdbc:mysql://192.168.0.230:3306/test?useUnicode=true&characterEncoding=utf8&serverTimezone=Asia/Shanghai&useSSL=false&autoReconnect=true";
    private static final String SOURCE_DB_USERNAME = "root";
    private static final String SOURCE_DB_PASSWORD = "123456";

    private static final String TARGET_DB_URL = "jdbc:mysql://192.168.0.230:3306/test?useUnicode=true&characterEncoding=utf8&serverTimezone=Asia/Shanghai&useSSL=false&autoReconnect=true";
    private static final String TARGET_DB_USERNAME = "root";
    private static final String TARGET_DB_PASSWORD = "123456";

    public static void test() {
        Connection sourceConn = null;
        Connection targetConn = null;
        Statement sourceStmt = null;
        Statement targetStmt = null;
        ResultSet rs = null;

        try {
            Class.forName(com.mysql.jdbc.Driver.class.getName());
            // Connect to source database
            sourceConn = DriverManager.getConnection(SOURCE_DB_URL, SOURCE_DB_USERNAME, SOURCE_DB_PASSWORD);
            sourceStmt = sourceConn.createStatement();

            // Connect to target database
            targetConn = DriverManager.getConnection(TARGET_DB_URL, TARGET_DB_USERNAME, TARGET_DB_PASSWORD);
            targetStmt = targetConn.createStatement();

            // Query the source database for table names
            String tableQuery = "SHOW TABLES";
            rs = sourceStmt.executeQuery(tableQuery);

            // Iterate over the table names
            while (rs.next()) {
                String tableName = rs.getString(1);

                // Query the source database for data
                String dataQuery = "SELECT * FROM " + tableName;
                ResultSet dataResultSet = sourceStmt.executeQuery(dataQuery);

                // Iterate over the result set and process SQL statements
                Integer tenantId = 19998;
                while (dataResultSet.next()) {
                    // Get the tenant_id value from the result set
                    //String tenantId = dataResultSet.getString("tenant_id");

                    // Process the SQL statement using jsqlparser
                    String insertStatement = createInsertStatement(tableName, dataResultSet, tenantId);

                    // Execute the insert statement on the target database
                    targetStmt.executeUpdate(insertStatement);
                }

                dataResultSet.close();
            }

            System.out.println("Data migration completed successfully!");

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            // Close all database resources
            try {
                if (rs != null) {
                    rs.close();
                }
                if (sourceStmt != null) {
                    sourceStmt.close();
                }
                if (sourceConn != null) {
                    sourceConn.close();
                }
                if (targetStmt != null) {
                    targetStmt.close();
                }
                if (targetConn != null) {
                    targetConn.close();
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    private static String createInsertStatement(String tableName, ResultSet dataResultSet, Integer tenantId) throws SQLException {
        String insertQuery = "INSERT INTO " + tableName + " (";
        String values = " VALUES (";

        // Get the column names from the result set metadata
        ResultSetMetaData rsmd = dataResultSet.getMetaData();
        int columnCount = rsmd.getColumnCount();
        for (int i = 1; i <= columnCount; i++) {
            String columnName = rsmd.getColumnName(i);

            // Exclude the tenant_id column from the insert statement
            if (columnName.equalsIgnoreCase("tenant_id")) {
                continue;
            }
            if (columnName.equalsIgnoreCase("id")) {
                continue;
            }


            insertQuery += columnName + ",";
            values += "'" + dataResultSet.getString(i) + "',";
        }

        insertQuery += "tenant_id) ";
        values += "'" + tenantId + "')";

        return insertQuery + values;
    }
}
