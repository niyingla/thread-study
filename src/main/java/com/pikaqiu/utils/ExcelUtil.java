package com.pikaqiu.utils;

import cn.hutool.core.collection.CollectionUtil;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * <p> ExcelUtil </p>
 *
 * @author xiaoye
 * @version 1.0
 * @date 2023/7/22 15:54
 */
@Slf4j
public class ExcelUtil {

    /**
     * 读取excel
     *
     * @param file
     * @return
     */
    public static List<Map<String, String>> readExcel(MultipartFile file) {
        //开启文件读取流
        HSSFWorkbook sheets;//读取文件
        try {
            sheets = new HSSFWorkbook(file.getInputStream());
        } catch (IOException e) {
            log.error("读取导入文件报错：", e);
            throw new RuntimeException("读取导入文件报错");
        }
        //获取sheet
        HSSFSheet sheet = sheets.getSheetAt(0);
        //列头
        Map<Integer, String> indexHead = new HashMap<>();
        List<Map<String, String>> data = Lists.newArrayList();
        int rows = sheet.getPhysicalNumberOfRows();
        for (int i = 0; i < rows; i++) {
            //行数据
            HashMap<String, String> readData = Maps.newHashMap();
            //获取列数
            HSSFRow row = sheet.getRow(i);
            int columns = row.getPhysicalNumberOfCells();
            for (int j = 0; j < columns; j++) {
                //拿到单元格数据
                HSSFCell cell = row.getCell(j);
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
