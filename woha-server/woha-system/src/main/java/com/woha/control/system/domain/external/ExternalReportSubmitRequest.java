package com.woha.control.system.domain.external;

import java.util.List;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 外管提交举报请求。
 *
 * @author luowenshen
 */
@Data
public class ExternalReportSubmitRequest
{
    /** 被举报用户Woha账号 */
    @NotNull(message = "被举报用户Woha账号不能为空")
    private Long targetChatNo;
    /** 违规类型 */
    @NotBlank(message = "违规类型不能为空")
    @Size(max = 50, message = "违规类型长度不能超过50个字符")
    private String violationType;
    /** 举报说明 */
    @NotBlank(message = "举报说明不能为空")
    @Size(max = 200, message = "举报说明长度不能超过200个字符")
    private String description;
    /** 证据图片路径 */
    @Size(max = 3, message = "证据图片最多上传3张")
    private List<String> evidenceUrls;
}
