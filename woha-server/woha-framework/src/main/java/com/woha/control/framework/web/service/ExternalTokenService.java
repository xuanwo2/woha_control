package com.woha.control.framework.web.service;

import java.util.Collections;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import org.springframework.stereotype.Component;
import com.woha.control.common.core.domain.model.ExternalLoginUser;
import com.woha.control.common.core.redis.RedisCache;

/**
 * 外管登录令牌服务
 *
 * @author luowenshen
 */
@Component
public class ExternalTokenService
{
    private static final int EXPIRE_DAYS = 7;
    private static final String TOKEN_KEY_PREFIX = "external:login:token:";
    private static final String ACCOUNT_TOKEN_KEY_PREFIX = "external:login:account:";
    /** 外管停用账号标记前缀 */
    private static final String DISABLED_ACCOUNT_KEY_PREFIX = "external:login:disabled:";
    private final RedisCache redisCache;

    public ExternalTokenService(RedisCache redisCache)
    {
        this.redisCache = redisCache;
    }

    /** 创建外管登录令牌 */
    public String createToken(ExternalLoginUser loginUser)
    {
        String token = UUID.randomUUID().toString().replace("-", "");
        redisCache.setCacheObject(TOKEN_KEY_PREFIX + token, loginUser, EXPIRE_DAYS, TimeUnit.DAYS);
        redisCache.setCacheSet(ACCOUNT_TOKEN_KEY_PREFIX + loginUser.getAccountId(), Collections.singleton(token));
        redisCache.expire(ACCOUNT_TOKEN_KEY_PREFIX + loginUser.getAccountId(), EXPIRE_DAYS, TimeUnit.DAYS);
        return token;
    }

    /** 获取外管登录会话 */
    public ExternalLoginUser getLoginUser(String token)
    {
        ExternalLoginUser loginUser = redisCache.getCacheObject(TOKEN_KEY_PREFIX + token);
        if (loginUser == null || Boolean.TRUE.equals(redisCache.hasKey(DISABLED_ACCOUNT_KEY_PREFIX + loginUser.getAccountId())))
        {
            return null;
        }
        return loginUser;
    }

    /** 清除账号所有登录令牌 */
    public void invalidateAccountTokens(Long accountId)
    {
        Set<String> tokens = redisCache.getCacheSet(ACCOUNT_TOKEN_KEY_PREFIX + accountId);
        if (tokens != null)
        {
            for (String token : tokens)
            {
                redisCache.deleteObject(TOKEN_KEY_PREFIX + token);
            }
        }
        redisCache.deleteObject(ACCOUNT_TOKEN_KEY_PREFIX + accountId);
    }

    /** 停用账号并使其全部登录令牌立即失效 */
    public void disableAccountTokens(Long accountId)
    {
        invalidateAccountTokens(accountId);
        redisCache.setCacheObject(DISABLED_ACCOUNT_KEY_PREFIX + accountId, Boolean.TRUE);
    }

    /** 启用账号并解除登录令牌停用标记 */
    public void enableAccountTokens(Long accountId)
    {
        redisCache.deleteObject(DISABLED_ACCOUNT_KEY_PREFIX + accountId);
    }

    /** 清除单个登录令牌 */
    public void invalidateToken(String token)
    {
        redisCache.deleteObject(TOKEN_KEY_PREFIX + token);
    }
}
