package com.IusCloud.messaging.config.redis;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class PermissionRedisService {

    /**
     * Convención compartida con el auth-service:
     *   auth:perms:{tenantId}:{userId}  →  ["CASES:READ", "CASES:WRITE", ...]
     */
    private static final String KEY_PATTERN = "auth:perms:%s:%s";

    private final RedisTemplate<String, List<String>> permissionsRedisTemplate;

    public List<String> getPermissions(UUID tenantId, UUID userId) {
        String key = KEY_PATTERN.formatted(tenantId, userId);
        try {
            List<String> permissions = permissionsRedisTemplate.opsForValue().get(key);
            return permissions != null ? permissions : Collections.emptyList();
        } catch (Exception ex) {
            log.warn("Redis unavailable when reading permissions for user {}: {}", userId, ex.getMessage());
            return Collections.emptyList();
        }
    }
}
