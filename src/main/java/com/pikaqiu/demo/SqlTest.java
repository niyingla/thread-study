package com.pikaqiu.demo;

import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.LongValue;
import net.sf.jsqlparser.expression.operators.relational.ExpressionList;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.statement.insert.Insert;

import java.util.List;

/**
 * <p> SqlTest </p>
 *
 * @author xiaoye
 * @version 1.0
 * @date 2024/10/23 16:21
 */
public class SqlTest {
    public static void test() throws Exception {
        String originalSql = "INSERT INTO `newlink197`.`dy_role`(`role_id`, `name`, `login_auth`, `role_source`, `role_code`, `create_time`, `update_time`, `create_user`, `update_user`, `is_enable`, `is_deleted`, `tenant_id`, `role_type`) VALUES (2, '普通员工', '1,2', 1, 'staff', '2022-04-14 13:38:19', '2023-05-12 17:49:10', NULL, NULL, 1, 1, 0, 1);";

        Insert insert = (Insert) CCJSqlParserUtil.parse(originalSql);
        //找到字段为 tenant_id 的位置
        int tenantIdIndex = 0;
        int idIndex = 0;
        List<Column> columns = insert.getColumns();
        for (int i = 0; i < columns.size(); i++) {
            if ("tenant_id".equals(columns.get(i).getColumnName()) ||
                "`tenant_id`".equals(columns.get(i).getColumnName())) {
                tenantIdIndex = i;
            }
            if ("role_id".equals(columns.get(i).getColumnName()) ||
                "`role_id`".equals(columns.get(i).getColumnName())) {
                idIndex = i;

            }
        }
        Expression expression = ((ExpressionList) insert.getItemsList()).getExpressions().get(tenantIdIndex);
        ((LongValue) expression).setStringValue("1971");

        columns.remove(idIndex);
        //删除idIndex列 让她自增
        ((ExpressionList) insert.getItemsList()).getExpressions().remove(idIndex);


        System.out.println(insert.toString());
    }
}
