package com.pikaqiu.My;

/**
 * @program: concurrency
 * @description: 枚举示例
 * @author: xiaoye
 * @create: 2020-06-15 23:05
 **/
public class Pizza {

    private PizzaStatus status;

    public enum PizzaStatus {
        //构造方法入参
        ORDERED(5) {
            /**
             * 复写方法
             * @return
             */
            @Override
            public boolean isOrdered() {
                return true;
            }
        },
        READY(2) {
            @Override
            public boolean isReady() {
                return true;
            }
        },
        DELIVERED(0) {
            @Override
            public boolean isDelivered() {
                return true;
            }
        };

        private int timeToDelivery;

        public boolean isOrdered() {
            return false;
        }

        public boolean isReady() {
            return false;
        }

        public boolean isDelivered() {
            return false;
        }

        public int getTimeToDelivery() {
            return timeToDelivery;
        }

        /**
         * 构造方法
         * @param timeToDelivery
         */
        PizzaStatus(int timeToDelivery) {
            this.timeToDelivery = timeToDelivery;
        }
    }

    public boolean isDeliverable() {
        return this.status.isReady();
    }

    public boolean isOrdered() {
        return this.status.isOrdered();
    }

    public void printTimeToDeliver() {
        System.out.println("Time to delivery is " +
                this.getStatus().getTimeToDelivery());
    }

    public PizzaStatus getStatus() {
        return status;
    }

    public void setStatus(PizzaStatus status) {
        this.status = status;
    }

    public static void main(String[] args) {
        Pizza testPz = new Pizza();
        testPz.setStatus(PizzaStatus.ORDERED);
        System.out.println(true == testPz.isOrdered());
    }

}
