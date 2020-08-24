package com.pikaqiu.syncResult;

import lombok.extern.slf4j.Slf4j;

import java.util.Date;

/**
 * @program: demo
 * @description:
 * @author: xiaoye
 * @create: 2019-08-12 16:29
 **/
@Slf4j
public class SyncResultDto {
    volatile boolean isRead = false;
    Object data = null;
    Object lock = new Object();
    final Date createTime = new Date();

    /**
     * 获取数据没有数据时线程等待
     * @return
     */
    public Object getData() {
        if (!isRead) {
            try {
                synchronized (lock) {
                    //释放锁
                    lock.wait();
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        return data;
    }

    /**
     * 设置数据
     * @param data
     */
    public void setData(Object data) {
        this.data = data;
        this.isRead = true;
        synchronized (lock) {
            lock.notifyAll();
        }
    }
}
