package com.pikaqiu.syncResult;

import com.google.common.collect.Lists;

import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @program: demo
 * @description:
 * @author: xiaoye
 * @create: 2019-08-12 16:19
 **/
public class FutureResult {

    private final static Integer TIME_OUT = 60;

    private final static ConcurrentHashMap<String, SyncResultDto> concurrentHashMap = new ConcurrentHashMap();

    /**
     * 获取结果
     *
     * @param requestId
     * @return
     */
    public static void waitResult(String requestId, SyncResultDto syncResultDto) {
        //创建结果包装类
        concurrentHashMap.put(requestId, syncResultDto);
    }

    /**
     * 获取结果
     *
     * @param requestId
     * @return
     */
    public static void putResult(String requestId, Object data) {
        //获取结果包装类
        SyncResultDto syncResultDto = concurrentHashMap.remove(requestId);
        if(syncResultDto != null){
            syncResultDto.setData(data);
        }
    }


    /**
     * 删除超时请求
     */
    public static void deleteTimeOut() {
        Calendar instance = Calendar.getInstance();
        instance.add(-TIME_OUT, Calendar.SECOND);
        Date outTime = instance.getTime();
        for (String key : concurrentHashMap.keySet()) {
            SyncResultDto syncResultDto = concurrentHashMap.get(key);
            if (outTime.after(syncResultDto.createTime)) {
                syncResultDto.setData(null);
                concurrentHashMap.remove(key);
            }
        }
    }


}
