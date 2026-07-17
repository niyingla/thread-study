-- =============================================================
-- 抽奖系统表结构
-- 设计要点:
--   1. Redis 是抽奖过程(库存/限次/概率)的唯一真相源,MySQL 只做配置与结果存储
--   2. t_lottery_record 通过 (act_id, request_id) 唯一键做落库幂等,防止 MQ 重复消费
-- =============================================================

-- 活动表
CREATE TABLE IF NOT EXISTS `t_lottery_activity`
(
    `id`            BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '活动ID',
    `act_name`      VARCHAR(128)    NOT NULL DEFAULT '' COMMENT '活动名称',
    `start_time`    DATETIME        NOT NULL COMMENT '活动开始时间',
    `end_time`      DATETIME        NOT NULL COMMENT '活动结束时间',
    `slice_seconds` INT UNSIGNED    NOT NULL DEFAULT 60 COMMENT '时间片长度(秒),用于奖品均匀发放',
    `user_limit`    INT UNSIGNED    NOT NULL DEFAULT 1 COMMENT '每人限抽次数(总)',
    `status`        TINYINT         NOT NULL DEFAULT 0 COMMENT '状态:0-未上线 1-已上线 2-已下线',
    `create_time`   DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time`   DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 COMMENT '抽奖活动表';

-- 奖品表
CREATE TABLE IF NOT EXISTS `t_lottery_prize`
(
    `id`          BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '奖品ID',
    `act_id`      BIGINT UNSIGNED NOT NULL COMMENT '活动ID',
    `prize_name`  VARCHAR(128)    NOT NULL DEFAULT '' COMMENT '奖品名称',
    `prize_type`  TINYINT         NOT NULL DEFAULT 1 COMMENT '奖品类型:1-积分 2-优惠券(纯虚拟)',
    `prob_ppm`    INT UNSIGNED    NOT NULL DEFAULT 0 COMMENT '独立中奖概率(百万分之),如 5000 表示 0.5%',
    `total_count` INT UNSIGNED    NOT NULL DEFAULT 0 COMMENT '奖品总数量',
    `priority`    INT             NOT NULL DEFAULT 0 COMMENT '优先级,越大越先掷(高价值奖品在前)',
    `create_time` DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    KEY `idx_act_id` (`act_id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 COMMENT '抽奖奖品表';

-- 中奖记录表
CREATE TABLE IF NOT EXISTS `t_lottery_record`
(
    `id`          BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '记录ID',
    `act_id`      BIGINT UNSIGNED NOT NULL COMMENT '活动ID',
    `user_id`     VARCHAR(64)     NOT NULL COMMENT '用户ID',
    `prize_id`    BIGINT UNSIGNED NOT NULL COMMENT '中奖奖品ID',
    `request_id`  VARCHAR(64)     NOT NULL COMMENT '抽奖请求ID,幂等用',
    `send_status` TINYINT         NOT NULL DEFAULT 0 COMMENT '发奖状态:0-待发放 1-已发放 2-发放失败',
    `draw_time`   DATETIME        NOT NULL COMMENT '中奖时间',
    `create_time` DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_act_request` (`act_id`, `request_id`),
    KEY `idx_act_user` (`act_id`, `user_id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 COMMENT '抽奖中奖记录表';
