package com.woha.control.web.controller.external;

import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import com.woha.control.common.core.domain.AjaxResult;
import com.woha.control.common.core.domain.model.ExternalLoginUser;
import com.woha.control.common.exception.ServiceException;
import com.woha.control.web.service.TencentCosUploadService;

/**
 * 外管图片上传接口。
 *
 * @author luowenshen
 */
@RestController
public class ExternalUploadController
{
    private final TencentCosUploadService uploadService;

    public ExternalUploadController(TencentCosUploadService uploadService)
    {
        this.uploadService = uploadService;
    }

    /** 上传图片到腾讯云对象存储 */
    @PostMapping(value = "/upload/img.action", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public AjaxResult uploadImage(@RequestParam("uploadFile") MultipartFile uploadFile)
    {
        requireExternalLogin();
        AjaxResult result = AjaxResult.success();
        result.put("path", uploadService.uploadImage(uploadFile));
        return result;
    }

    /** 校验外管登录身份 */
    private void requireExternalLogin()
    {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !(authentication.getPrincipal() instanceof ExternalLoginUser))
        {
            throw new ServiceException("外管登录状态无效");
        }
    }
}
