package com.woha.control.web.service;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.util.Locale;
import java.util.UUID;
import javax.imageio.ImageIO;
import jakarta.annotation.PreDestroy;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import com.qcloud.cos.COSClient;
import com.qcloud.cos.ClientConfig;
import com.qcloud.cos.auth.BasicCOSCredentials;
import com.qcloud.cos.model.ObjectMetadata;
import com.qcloud.cos.model.PutObjectRequest;
import com.qcloud.cos.region.Region;
import com.woha.control.common.exception.ServiceException;
import com.woha.control.web.config.TencentCosProperties;

/**
 * 腾讯云对象存储图片上传服务。
 *
 * @author luowenshen
 */
@Service
public class TencentCosUploadService
{
    /** 图片最大容量 */
    private static final long MAX_IMAGE_SIZE = 5L * 1024 * 1024;
    private final TencentCosProperties properties;
    private volatile COSClient cosClient;

    public TencentCosUploadService(TencentCosProperties properties)
    {
        this.properties = properties;
    }

    /** 上传公会封面图片并返回公网访问地址 */
    public String uploadImage(MultipartFile uploadFile)
    {
        validateImage(uploadFile);
        String extension = getExtension(uploadFile.getOriginalFilename());
        String objectKey = "external/guild/" + UUID.randomUUID().toString().replace("-", "") + "." + extension;
        ObjectMetadata metadata = new ObjectMetadata();
        metadata.setContentLength(uploadFile.getSize());
        metadata.setContentType(uploadFile.getContentType());
        try (InputStream inputStream = uploadFile.getInputStream())
        {
            getCosClient().putObject(new PutObjectRequest(properties.getBucket(), objectKey, inputStream, metadata));
        }
        catch (IOException exception)
        {
            throw new ServiceException("读取上传图片失败");
        }
        catch (Exception exception)
        {
            throw new ServiceException("上传图片到腾讯云对象存储失败");
        }
        return buildPublicUrl(objectKey);
    }

    /** 校验上传图片格式和大小 */
    private void validateImage(MultipartFile uploadFile)
    {
        if (uploadFile == null || uploadFile.isEmpty())
        {
            throw new ServiceException("请选择图片");
        }
        if (uploadFile.getSize() > MAX_IMAGE_SIZE)
        {
            throw new ServiceException("图片大小不能超过5MB");
        }
        String extension = getExtension(uploadFile.getOriginalFilename());
        if (!("jpg".equals(extension) || "jpeg".equals(extension) || "png".equals(extension)))
        {
            throw new ServiceException("仅支持 PNG、JPG、JPEG 格式图片");
        }
        try (InputStream inputStream = uploadFile.getInputStream())
        {
            BufferedImage image = ImageIO.read(inputStream);
            if (image == null)
            {
                throw new ServiceException("上传文件不是有效图片");
            }
        }
        catch (IOException exception)
        {
            throw new ServiceException("读取上传图片失败");
        }
    }

    /** 获取腾讯云对象存储客户端 */
    private COSClient getCosClient()
    {
        COSClient client = cosClient;
        if (client != null)
        {
            return client;
        }
        synchronized (this)
        {
            if (cosClient == null)
            {
                validateConfiguration();
                cosClient = new COSClient(new BasicCOSCredentials(properties.getSecretId(), properties.getSecretKey()),
                        new ClientConfig(new Region(properties.getRegion())));
            }
            return cosClient;
        }
    }

    /** 校验对象存储必要配置 */
    private void validateConfiguration()
    {
        if (!hasText(properties.getSecretId()) || !hasText(properties.getSecretKey()) || !hasText(properties.getBucket())
                || !hasText(properties.getRegion()))
        {
            throw new ServiceException("腾讯云对象存储配置不完整");
        }
    }

    /** 拼接对象存储公开访问地址 */
    private String buildPublicUrl(String objectKey)
    {
        if (hasText(properties.getUrl()))
        {
            return properties.getUrl().replaceAll("/+$", "") + "/" + objectKey;
        }
        return getCosClient().getObjectUrl(properties.getBucket(), objectKey).toString();
    }

    /** 获取规范化扩展名 */
    private String getExtension(String fileName)
    {
        if (fileName == null)
        {
            return "";
        }
        int index = fileName.lastIndexOf('.');
        return index < 0 ? "" : fileName.substring(index + 1).toLowerCase(Locale.ROOT);
    }

    /** 关闭对象存储客户端 */
    @PreDestroy
    public void shutdown()
    {
        if (cosClient != null)
        {
            cosClient.shutdown();
        }
    }

    /** 判断文本是否为空 */
    private boolean hasText(String value)
    {
        return value != null && !value.isBlank();
    }
}
