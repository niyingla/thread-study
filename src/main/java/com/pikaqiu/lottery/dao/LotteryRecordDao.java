package com.pikaqiu.lottery.dao;

import com.pikaqiu.lottery.common.LotteryConstants;
import com.pikaqiu.lottery.dto.WinRecord;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import javax.annotation.Resource;
import java.sql.Timestamp;
import java.util.List;

/**
 * 中奖记录 DAO。通过 uk_act_request 唯一键 + INSERT IGNORE 实现落库幂等。
 *
 * @author xiaoye
 */
@Repository
public class LotteryRecordDao {

    /** 行映射:t_lottery_record -> WinRecord(补偿场景不含 prizeName,发奖按 prizeId 即可) */
    private static final RowMapper<WinRecord> RECORD_MAPPER = (rs, rowNum) -> {
        WinRecord record = new WinRecord();
        record.setActId(rs.getLong("act_id"));
        record.setUserId(rs.getString("user_id"));
        record.setPrizeId(rs.getLong("prize_id"));
        record.setRequestId(rs.getString("request_id"));
        Timestamp drawTime = rs.getTimestamp("draw_time");
        record.setDrawTime(drawTime == null ? null : drawTime.getTime());
        return record;
    };

    @Resource
    private JdbcTemplate jdbcTemplate;

    /**
     * 幂等插入。返回 1 表示新插入,0 表示 (act_id, request_id) 已存在。
     */
    public int insertIgnore(WinRecord record) {
        String sql = "INSERT IGNORE INTO t_lottery_record "
                + "(act_id, user_id, prize_id, request_id, send_status, draw_time) "
                + "VALUES (?, ?, ?, ?, ?, ?)";
        return jdbcTemplate.update(sql,
                record.getActId(),
                record.getUserId(),
                record.getPrizeId(),
                record.getRequestId(),
                LotteryConstants.SEND_PENDING,
                new Timestamp(record.getDrawTime()));
    }

    /**
     * 是否已发放成功(幂等判断)。
     */
    public boolean isGranted(Long actId, String requestId) {
        String sql = "SELECT count(*) FROM t_lottery_record "
                + "WHERE act_id = ? AND request_id = ? AND send_status = ?";
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class,
                actId, requestId, LotteryConstants.SEND_SUCCESS);
        return count != null && count > 0;
    }

    /**
     * 拉取一批未发放成功、且创建时间早于 createdBefore 的记录,供对账补偿。
     *
     * @param createdBefore 只取该时间点之前创建的记录(避开在途消费)
     * @param limit         批大小
     * @return 待补偿的中奖记录
     */
    public List<WinRecord> listUnfinished(Timestamp createdBefore, int limit) {
        String sql = "SELECT act_id, user_id, prize_id, request_id, draw_time "
                + "FROM t_lottery_record WHERE send_status <> ? AND create_time < ? ORDER BY id LIMIT ?";
        return jdbcTemplate.query(sql, RECORD_MAPPER,
                LotteryConstants.SEND_SUCCESS, createdBefore, limit);
    }

    /**
     * 更新发奖状态。
     */
    public int updateSendStatus(Long actId, String requestId, int sendStatus) {
        String sql = "UPDATE t_lottery_record SET send_status = ? WHERE act_id = ? AND request_id = ?";
        return jdbcTemplate.update(sql, sendStatus, actId, requestId);
    }
}
