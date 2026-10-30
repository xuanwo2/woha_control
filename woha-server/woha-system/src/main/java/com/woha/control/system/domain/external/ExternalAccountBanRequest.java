package com.woha.control.system.domain.external;

import java.util.List;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 外管账号封禁请求。
 *
 * @author luowenshen
 */
@Data
public class ExternalAccountBanRequest
{
    /** 目标用户Woha账号 */
    @NotNull(message = "用户Woha账号不能为空")
    private Long chatNo;
    /** 封禁天数 */
    @NotNull(message = "封禁天数不能为空")
    @Min(value = 1, message = "封禁天数不能小于1天")
    @Max(value = 7, message = "封禁天数不能大于7天")
    private Integer dayNum;
    /** 违规类型 */
    @NotBlank(message = "违规类型不能为空")
    @Size(max = 50, message = "违规类型长度不能超过50个字符")
    private String violationType;
    /** 封禁原因 */
    @NotBlank(message = "封禁原因不能为空")
    @Size(max = 200, message = "封禁原因长度不能超过200个字符")
    private String reason;
    /** 证据图片路径 */
    @Size(max = 3, message = "证据图片最多上传3张")
    private List<String> evidenceUrls;
}
