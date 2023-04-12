package com.pikaqiu.demo;

import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.google.common.collect.Lists;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.List;
import java.util.stream.Collectors;

/**
 * <p> dbtool </p>
 *
 * @author xiaoye
 * @version 1.0
 * @date 2023/4/12 20:27
 */
public class DbTool {
    public static void main(String[] args) {
        List<Integer> list = Lists.newArrayList(100044, 100089, 100090, 100091, 100093, 100095, 100097, 100098, 100099, 100100, 100101, 100102, 100103, 100104, 100105, 100106, 100107, 100108, 100109, 100111, 100112, 100113, 100114, 100115, 100116, 100117, 100118, 100119, 100120, 100121, 100122, 100123, 100124, 100125, 100126, 100127, 100128, 100129, 100130, 100131, 100132, 100133, 100134, 100135, 100136, 100137, 100138, 100139, 100140, 100141, 100142, 100143, 100144, 100145, 100146, 100147, 100148, 100149, 100150, 100151, 100152, 100153, 100154, 100155, 100156, 100157, 100158, 100159, 100160, 100161, 100162, 100163, 100164, 100165, 100166, 100167, 100168, 100169, 100170, 100171, 100172, 100173, 100174, 100175, 100176, 100177, 100178, 100179, 100180, 100181, 100182, 100183, 100184, 100185, 100186, 100187, 100188, 100189, 100190, 100191, 100192, 100193, 100194, 100195, 100196, 100197, 100198, 100199, 100200, 100201, 100202, 100203, 100204, 100205, 100206, 100207, 100208, 100209, 100210, 100211, 100212, 100213, 100214, 100215, 100216, 100217, 100218, 100219, 100220, 100221, 100222, 100223, 100224);
        List<Object> dsName1 = null;
        JSONObject jsonObject = null;
        try {
            //加载本地文件到string
            FileInputStream inputStream = new FileInputStream("C:\\Users\\qiyu\\Desktop\\dblocal.txt");
            byte[] bytes = new byte[1024];
            int len = 0;
            StringBuilder sb = new StringBuilder();
            while ((len = inputStream.read(bytes)) != -1) {
                sb.append(new String(bytes, 0, len));
            }
            jsonObject = JSONUtil.parseObj(sb.toString());

            JSONArray jsonArray = jsonObject.getJSONArray("druid");
            dsName1 = jsonArray.stream().filter(item -> {
                JSONObject dataSource = (JSONObject) item;
                String dsName = (String) dataSource.get("dsName");
                if (list.contains(Integer.valueOf(dsName.replace("ds", "")))) {
                    return true;
                }
                return false;
            }).map(item -> {
                JSONObject dataSource = (JSONObject) item;
                dataSource.put("database", ((String) dataSource.get("database")).replace("qiyuan", "newlink"));
                return dataSource;
            }).collect(Collectors.toList());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        //文件流把json写入到文件
        try {
            FileOutputStream outputStream = new FileOutputStream("C:\\Users\\qiyu\\Desktop\\newlink.txt");
            jsonObject.put("druid", dsName1);
            outputStream.write(JSONUtil.toJsonStr(jsonObject).getBytes());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
