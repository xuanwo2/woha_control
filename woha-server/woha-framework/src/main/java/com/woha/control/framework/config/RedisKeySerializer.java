package com.woha.control.framework.config;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.data.redis.serializer.SerializationException;

/**
 * Redis 顶层 key 统一前缀序列化器。
 *
 * @author luowenshen
 */
public class RedisKeySerializer implements RedisSerializer<Object>
{
    /** BI 系统所有 Redis key 的统一前缀。 */
    public static final String KEY_PREFIX = "bi:";

    /** Redis key 使用的字符编码。 */
    private static final Charset KEY_CHARSET = StandardCharsets.UTF_8;

    @Override
    public byte[] serialize(Object key) throws SerializationException
    {
        if (key == null)
        {
            return null;
        }
        String keyText = key.toString();
        String prefixedKey = keyText.startsWith(KEY_PREFIX) ? keyText : KEY_PREFIX + keyText;
        return prefixedKey.getBytes(KEY_CHARSET);
    }

    @Override
    public Object deserialize(byte[] bytes) throws SerializationException
    {
        if (bytes == null || bytes.length == 0)
        {
            return null;
        }
        String key = new String(bytes, KEY_CHARSET);
        return key.startsWith(KEY_PREFIX) ? key.substring(KEY_PREFIX.length()) : key;
    }
}
