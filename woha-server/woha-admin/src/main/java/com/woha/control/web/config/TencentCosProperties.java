package com.woha.control.web.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 腾讯云对象存储配置。
 *
 * @author luowenshen
 */
@Data
@Component
@ConfigurationProperties(prefix = "external.tencent.cos")
public class TencentCosProperties
{
    /** 腾讯云应用编号 */
    private String appId;
    /** 腾讯云访问密钥标识 */
    private String secretId;
    /** 腾讯云访问密钥 */
    private String secretKey;
    /** 对象存储桶名称 */
    private String bucket;
    /** 对象存储地域 */
    private String region;
    /** 对象存储公网访问地址 */
    private String url;
}
