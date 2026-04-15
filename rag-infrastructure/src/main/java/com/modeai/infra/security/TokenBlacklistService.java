package com.modeai.infra.security;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class TokenBlacklistService {

    private final RedisTemplate<String, Object> redisTemplate;

    public void addToBlacklist(String token, long ttlMillis) {
        redisTemplate.opsForValue().set("blacklist:" + token, "1", ttlMillis, TimeUnit.MILLISECONDS);
        log.debug("Token added to blacklist, TTL: {}ms", ttlMillis);
    }

    public boolean isBlacklisted(String token) {
        return Boolean.TRUE.equals(redisTemplate.hasKey("blacklist:" + token));
    }

    public void removeFromBlacklist(String token) {
        redisTemplate.delete("blacklist:" + token);
    }
}
