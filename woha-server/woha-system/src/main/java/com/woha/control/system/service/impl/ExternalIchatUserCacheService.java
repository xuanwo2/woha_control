package com.woha.control.system.service.impl;

import java.nio.charset.StandardCharsets;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.stereotype.Service;

/**
 * 原业务用户缓存处理服务。
 *
 * @author luowenshen
 */
@Service
public class ExternalIchatUserCacheService
{
    /** 原业务用户资料缓存键。 */
    private static final byte[] USER_CACHE_KEY = "ichat:user".getBytes(StandardCharsets.UTF_8);
    private static final Logger log = LoggerFactory.getLogger(ExternalIchatUserCacheService.class);
    private final RedisConnectionFactory connectionFactory;

    public ExternalIchatUserCacheService(RedisConnectionFactory connectionFactory)
    {
        this.connectionFactory = connectionFactory;
    }

    /** 失效原业务用户资料缓存。 */
    public void evictUserCache(Long uid)
    {
        if (uid == null)
        {
            return;
        }
        try (RedisConnection connection = connectionFactory.getConnection())
        {
            connection.hashCommands().hDel(USER_CACHE_KEY, String.valueOf(uid).getBytes(StandardCharsets.UTF_8));
        }
        catch (Exception exception)
        {
            // 缓存失效失败不影响已提交的靓号分配，保留日志用于后续补偿处理。
            log.error("靓号分配后失效原业务用户缓存失败，uid={}", uid, exception);
        }
    }
}
