# concurrentpay —— 同一账户高并发扣款 demo

两个并发安全扣款方案的可运行 Java demo，对应之前的设计文档。
纯 Java、零外部依赖（不需要 MySQL / Redis），每个文件都有 `main`，
启动 100 个并发线程验证「不超扣 + 账实平」。

| 文件 | 方案 | 说明 |
| --- | --- | --- |
| `BatchMergeDeductionDemo.java` | 方案一 合并扣减 | 单写者线程 + `BlockingQueue` 批合并 + FIFO 部分结算 |
| `RedisStyleDeductionDemo.java` | 方案三 Redis 内存扣减 | 用 `synchronized` 模拟 Redis 单线程原子段 + opLog 持久化/重建 |
| `deduct.lua` | 方案三 真实脚本 | 生产环境用的 Redis Lua（幂等 + 判断 + DECRBY 原子完成） |

## 放进你的项目

包名为 `com.study.thread.concurrentpay`。两种放法二选一：

1. **保持包名**：把这三个文件放到
   `D:\studyWork\thread-study\src\main\java\com\study\thread\concurrentpay\`
   （Maven/Gradle 标准结构；非 Maven 项目放到对应源码根下的同名目录）。
2. **改包名**：把每个 `.java` 文件首行的 `package com.study.thread.concurrentpay;`
   改成你项目实际的包名，再放进对应目录即可。

> `deduct.lua` 只是方案三上真 Redis 时用的脚本，参与编译，按需放进 resources。

## 运行

每个 demo 都是独立 `main`，直接跑：

```bash
# 标准方式（项目里用 IDE 直接运行 main 即可）
javac BatchMergeDeductionDemo.java
java  com.study.thread.concurrentpay.BatchMergeDeductionDemo

# 或 JDK 11+ 单文件快速运行（不落 class 文件，适合快速看效果）
java BatchMergeDeductionDemo.java
java RedisStyleDeductionDemo.java
```

预期输出（数字每次略有不同，但下面三条恒为 true）：

```
账实校验(初始-成功总额==最终余额) : true
无超扣(最终余额>=0)               : true
opLog 重建余额 == 当前余额        : true   （仅方案三）
```

## 关键设计点（对照真实落地）

- **金额用 long「分」**，规避浮点；生产对应 DB `DECIMAL` / `BigDecimal`。
- **方案一**：余额只被单一 writer 线程改，故无需锁；`BATCH_WINDOW_MS` 是
  「延迟 vs 合并率」的旋钮；`pay()` 带超时兜底防写者卡死。
  分布式多实例时需用一致性哈希 / MQ 分区把同一 accountId 固定到同一写者。
- **方案三**：`synchronized` 仅为模拟 Redis 单 key 的串行原子，真实环境靠
  Lua（`deduct.lua`）保证原子；命门是 **opLog 先落盘再返回成功** + AOF +
  定期对账，demo 里用 `opLog` 和 `rebuildFromLog` 演示了这条链路。
- 两方案都内置了 **幂等**（`dedup` map 模拟 `request_id` 唯一索引 / Redis dedup 键），
  这是与吞吐方案正交、必须都做的底座。
