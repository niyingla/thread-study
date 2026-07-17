package com.pikaqiu.lottery.dto;

import com.pikaqiu.lottery.enums.DrawStatus;
import lombok.Data;

/**
 * 抽奖结果。
 *
 * @author xiaoye
 */
@Data
public class DrawResult {

    /** 结果状态 */
    private DrawStatus status;

    /** 是否中奖 */
    private boolean win;

    /** 中奖奖品ID,未中奖为 null */
    private Long prizeId;

    /** 中奖奖品名称,未中奖为 null */
    private String prizeName;

    /** 是否为幂等重放(同一 requestId 的重复请求) */
    private boolean replay;

    /** 提示信息 */
    private String message;

    public static DrawResult of(DrawStatus status) {
        DrawResult result = new DrawResult();
        result.setStatus(status);
        result.setWin(status == DrawStatus.WIN);
        result.setMessage(status.getDesc());
        return result;
    }
}
