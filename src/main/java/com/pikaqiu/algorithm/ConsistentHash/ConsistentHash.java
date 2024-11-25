package com.pikaqiu.algorithm.ConsistentHash;

import org.springframework.util.DigestUtils;

import java.util.SortedMap;
import java.util.TreeMap;

public class ConsistentHash {

    private String SALT = ":zxfhzdd:";
    /**
     * 用于存储一致性hash节点 的 排序map
     * key ： 虚拟节点hash值
     * value : 节点
     */
    private final SortedMap<Integer, String> circle = new TreeMap<>();
    /**
     * 虚拟分片数
     */
    private final int numberOfReplicas;

    public ConsistentHash(int numberOfReplicas, String[] nodes) {
        this.numberOfReplicas = numberOfReplicas;
        for (String node : nodes) {
            add(node);
        }
    }

    public void add(String node) {
        //循环虚拟添加到一致性hash圈
        for (int i = 0; i < numberOfReplicas; i++) {
            //对节点和虚拟序号进行Md5然后获取hash
            int hash = getHash(getMd5Key(node, i));
            //添加到hash圈
            circle.put(hash, node);
        }
    }

    public void remove(String node) {
        for (int i = 0; i < numberOfReplicas; i++) {
            int hash = getHash(getMd5Key(node, i));
            circle.remove(hash);
        }
    }

    /**
     * 计算节点和虚拟序号的MD5值
     * @param node
     * @param index
     * @return
     */
    public String getMd5Key(String node, Integer index) {
        byte[] bytes = DigestUtils.md5Digest((node + SALT + index).getBytes());
        return new String(bytes);
    }

    public String get(String key) {
        if (circle.isEmpty()) {
            return null;
        }
        //获取key对应的hash
        int hash = getHash(key);
        //获取大于hash的全部数据
        SortedMap<Integer, String> tailMap = circle.tailMap(hash);
        //后面没有数据，取第一个，否则取节点后第一个
        Integer hashToUse = tailMap.isEmpty() ? circle.firstKey() : tailMap.firstKey();
        //根据前面的hash值获取对应的节点
        return circle.get(hashToUse);
    }

    /**
     * 简单的哈希方法
     * @param key
     * @return
     */
    private int getHash(String key) {
        return key.hashCode();
    }

    public static void main(String[] args) {
        String[] nodes = {"Node1", "Node2", "Node3"};
        ConsistentHash consistentHash = new ConsistentHash(520, nodes); // 使用50个虚拟节点
        String key = "myKey2";
        String assignedNode = consistentHash.get(key);
        System.out.println("Key: " + key + " is assigned to Node: " + assignedNode);
    }
}
