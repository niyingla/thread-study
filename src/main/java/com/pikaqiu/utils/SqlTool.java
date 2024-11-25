package com.pikaqiu.utils;

import cn.hutool.core.collection.CollectionUtil;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * <p> SqlTool </p>
 *
 * @author xiaoye
 * @version 1.0
 * @date 2024/11/21 17:10
 */
public class SqlTool {
    public static void main(String[] args) throws Exception {
        String fileName = "C:\\Users\\qiyu\\Downloads\\无标题 (2).xlsx";
        //读取本地文件
        InputStream inputStream = new FileInputStream(fileName);
        List<Map<String, String>> readExcel = readExcel(inputStream);
        BufferedWriter writer = new BufferedWriter(new FileWriter("生成订单22222.sql", true));
        int index = 1;
        for (Map<String, String> rowMap : readExcel) {
            //index五位补0
            String indexStr = String.format("%05d", index++);

            //读取rowMap中的 ai_marketing_start_time ai_marketing_end_time ,account_id open_id,tenant_id
            String aiMarketingStartTime = rowMap.get("ai_marketing_start_time");
            String aiMarketingEndTime = rowMap.get("ai_marketing_end_time");
            String accountId = rowMap.get("account_id");
            String openId = rowMap.get("open_id");
            String tenantId = rowMap.get("tenant_id");
            /**
             * 补入sql ai_marketing_start_time ai_marketing_end_time ,account_id open_id,tenant_id
             * INSERT INTO `sys_ai_marketing_order`(`order_no`, `order_type`, `relation_type`,
             *                                      `order_status`, `ai_marketing_start_date`,
             *                                      `ai_marketing_end_date`, `account_id`, `open_id`, `tenant_id`,
             *                                      `platform`, `remark`)
             * VALUES ('YX10000120241100001', 1, 4, 1, '2024-11-21', '2024-11-21', 1, '1', 197, 1, '初始化账号有效期');
             */
            StringBuffer stringBuffer = new StringBuffer();
            stringBuffer.append("INSERT INTO `sys_ai_marketing_order`(`order_no`, `order_type`, `relation_type`,");
            stringBuffer.append("`order_status`, `ai_marketing_start_date`,");
            stringBuffer.append("`ai_marketing_end_date`, `account_id`, `open_id`, `tenant_id`,");
            stringBuffer.append("`platform`, `remark`)");
            stringBuffer.append("VALUES ('YX100001202411" + indexStr + "', 1, 4, 1, '");
            stringBuffer.append(aiMarketingStartTime);
            stringBuffer.append("', '");
            stringBuffer.append(aiMarketingEndTime);
            stringBuffer.append("', '");
            stringBuffer.append(accountId);
            stringBuffer.append("', '");
            stringBuffer.append(openId);
            stringBuffer.append("', '");
            stringBuffer.append(tenantId);
            stringBuffer.append("', 1, '初始化账号有效期');");

            //写入到file文件最后一行
            writer.write(stringBuffer.toString());
            writer.newLine();
        }
        writer.close();
    }

    /**
     * 读取excel
     *
     * @param inputStream
     * @return
     */
    public static List<Map<String, String>> readExcel(InputStream inputStream) {
        //开启文件读取流
        XSSFWorkbook sheets = null;//读取文件
        try {
            sheets = new XSSFWorkbook(inputStream);
        } catch (IOException e) {
        }
        //获取sheet
        XSSFSheet sheet = sheets.getSheetAt(0);
        //列头
        Map<Integer, String> indexHead = new HashMap<>();
        List<Map<String, String>> data = Lists.newArrayList();
        int rows = sheet.getPhysicalNumberOfRows();
        for (int i = 0; i < rows; i++) {
            //行数据
            HashMap<String, String> readData = Maps.newHashMap();
            //获取列数
            XSSFRow row = sheet.getRow(i);
            if(Objects.isNull(row)){
                break;
            }
            int columns = row.getPhysicalNumberOfCells();
            for (int j = 0; j < columns; j++) {
                //拿到单元格数据
                XSSFCell cell = row.getCell(j);
                if (cell == null) {
                    continue;
                }
                String rawValue = new DataFormatter().formatCellValue(cell);
                if (i == 0) {
                    indexHead.put(j, rawValue);
                } else {
                    readData.put(indexHead.get(j), rawValue);
                }
            }
            if (CollectionUtil.isNotEmpty(readData)) {
                data.add(readData);
            }
        }
        return data;
    }
}
