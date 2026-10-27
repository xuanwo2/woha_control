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
    /** 原业务用户访问令牌缓存键前缀。 */
    private static final String ACCESS_TOKEN_KEY_PREFIX = "ichat:uid_access_token:";
    /** 原业务用户票据缓存键前缀。 */
    private static final String TICKET_KEY_PREFIX = "ichat:uid_ticket:";
    /** 原业务游戏访问令牌缓存键前缀。 */
    private static final String GAME_ACCESS_TOKEN_KEY_PREFIX = "ichat:game_access_token:";
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

    /** 清理原业务用户已登录会话。 */
    public void invalidateUserSessions(Long uid, Integer regionId)
    {
        if (uid == null || regionId == null)
        {
            return;
        }
        byte[] uidBytes = String.valueOf(uid).getBytes(StandardCharsets.UTF_8);
        byte[] accessTokenKey = getRegionKey(ACCESS_TOKEN_KEY_PREFIX, regionId);
        byte[] gameAccessTokenKey = getRegionKey(GAME_ACCESS_TOKEN_KEY_PREFIX, regionId);
        try (RedisConnection connection = connectionFactory.getConnection())
        {
            byte[] accessToken = connection.hashCommands().hGet(accessTokenKey, uidBytes);
            if (accessToken != null)
            {
                connection.hashCommands().hDel(accessTokenKey, accessToken);
            }
            connection.hashCommands().hDel(accessTokenKey, uidBytes);
            connection.hashCommands().hDel(getRegionKey(TICKET_KEY_PREFIX, regionId), uidBytes);

            byte[] gameToken = connection.hashCommands().hGet(gameAccessTokenKey, uidBytes);
            if (gameToken != null)
            {
                connection.hashCommands().hDel(gameAccessTokenKey, gameToken);
            }
            connection.hashCommands().hDel(gameAccessTokenKey, uidBytes);
        }
        catch (Exception exception)
        {
            log.error("封禁用户后清理原业务登录会话失败，uid={}, regionId={}", uid, regionId, exception);
        }
    }

    /** 获取原业务按大区分片的 Redis 键。 */
    private byte[] getRegionKey(String keyPrefix, Integer regionId)
    {
        return (keyPrefix + regionId).getBytes(StandardCharsets.UTF_8);
    }
}
