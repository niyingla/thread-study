package com.example.text.demo;

/**
 * @program: text
 * @description:多线程有序打印
 * @author: xiaoye
 * @create: 2018-11-08 09:13
 **/
public class TwoThreadWaitNotify {

    private int start = 1;

    private boolean flag = false;

    public static void main(String[] args) throws Exception{
        join();
        /*TwoThreadWaitNotify twoThread = new TwoThreadWaitNotify();

        //传入引用类
        Thread t1 = new Thread(new OuNum(twoThread));
        t1.setName("A");


        Thread t2 = new Thread(new JiNum(twoThread));
        t2.setName("B");

        t1.start();
        t2.start();*/
    }

    /**
     * 偶数线程
     */
    public static class OuNum implements Runnable {
        private TwoThreadWaitNotify number;

        public OuNum(TwoThreadWaitNotify number) {
            this.number = number;
        }

        @Override
        public void run() {

            while (number.start <= 100) {
                synchronized (TwoThreadWaitNotify.class) {
                    //System.out.println("偶数线程抢到锁了");
                    if (number.flag) {
                        System.out.println(Thread.currentThread().getName() + "+-+偶数" + number.start);
                        number.start++;

                        number.flag = false;
                        //随机唤醒一个线程
                        TwoThreadWaitNotify.class.notify();

                    } else {
                        try {
                            //当前线程等待
                            TwoThreadWaitNotify.class.wait();
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }

            }
        }
    }


    /**
     * 奇数线程
     */
    public static class JiNum implements Runnable {

        private TwoThreadWaitNotify number;

        public JiNum(TwoThreadWaitNotify number) {
            this.number = number;
        }

        @Override
        public void run() {
            while (number.start <= 100) {
                synchronized (TwoThreadWaitNotify.class) {
                    //System.out.println("奇数线程抢到锁了");
                    if (!number.flag) {
                        System.out.println(Thread.currentThread().getName() + "+-+奇数" + number.start);
                        number.start++;

                        number.flag = true;

                        TwoThreadWaitNotify.class.notify();
                    } else {
                        try {
                            TwoThreadWaitNotify.class.wait();
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        }
    }

    private static void join() throws InterruptedException {
        Thread t1 = new Thread(new Runnable() {
            @Override
            public void run() {
                System.out.println("running");
                try {
                    Thread.sleep(3000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });
        Thread t2 = new Thread(new Runnable() {
            @Override
            public void run() {
                System.out.println("running2");
                try {
                    Thread.sleep(4000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });

        //直接创建各个线程时平行的
        t1.start();
        t2.start();

        //抢占当前线程执行
        //等待线程2终止
        t2.join();
        //等待线程1终止
        t1.join();
        //等待了 四秒


        System.out.println("main over");
    }
}
