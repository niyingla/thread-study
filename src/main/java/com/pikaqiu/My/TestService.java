package com.pikaqiu.My;

import lombok.extern.slf4j.Slf4j;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Slf4j
@Service
public class TestService {

    /**
     * 等待时间列表
     * 2022-12-10T00:52:08.971   0
     * 2022-12-10T00:52:10.974 + 2*3^0
     * 2022-12-10T00:52:16.980 + 2*3^1
     * 2022-12-10T00:52:34.993 + 2*3^2
     */
    @Retryable(backoff = @Backoff(delay = 2000, multiplier = 3), maxAttempts = 4)
    public void testRetry() {
        log.info("" + LocalDateTime.now());
        if (1 == 1) {
            throw new RuntimeException();
        }
    }
}
