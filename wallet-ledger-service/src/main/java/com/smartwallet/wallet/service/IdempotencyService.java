package com.smartwallet.wallet.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class IdempotencyService {

    private final StringRedisTemplate redisTemplate;

    public boolean isProcessed(String idempotencyKey) {
        if (idempotencyKey == null || idempotencyKey.isEmpty()) {
            return false;
        }
        // SET if not exists, 24 hours TTL
        Boolean isNew = redisTemplate.opsForValue().setIfAbsent("idem:" + idempotencyKey, "PROCESSED", 24, TimeUnit.HOURS);
        return Boolean.FALSE.equals(isNew);
    }
}
