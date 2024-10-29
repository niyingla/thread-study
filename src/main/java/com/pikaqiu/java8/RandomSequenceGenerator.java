package com.pikaqiu.java8;

import java.util.Iterator;
import java.util.Random;

/**
 * <p> RandomSequenceGenerator </p>
 *
 * @author xiaoye
 * @version 1.0
 * @date 2024/9/19 18:01
 */
public class RandomSequenceGenerator implements Iterable<Integer> {
    private final int size;
    private final Random random;

    public RandomSequenceGenerator(int size) {
        this.size = size;
        this.random = new Random();
    }

    @Override
    public Iterator<Integer> iterator() {
        return new Iterator<Integer>() {
            private int count = 0;

            @Override
            public boolean hasNext() {
                return count < size;
            }

            @Override
            public Integer next() {
                count++;
                return random.nextInt();
            }
        };
    }

    public static void main(String[] args) {
        RandomSequenceGenerator generator = new RandomSequenceGenerator(10);
        for (int num : generator) {
            System.out.println(num);
        }

    }
}
