package com.woha.control.system.domain.external;

import java.util.Date;
import lombok.Data;

/**
 * 外管举报记录。
 *
 * @author luowenshen
 */
@Data
public class ExternalReport
{
    /** 举报记录主键 */
    private Long id;
    /** 举报外管账号主键 */
    private Long reporterAccountId;
    /** 举报外管账号 */
    private String reporterLoginName;
    /** 被举报用户UID */
    private Long targetUid;
    /** 被举报用户Woha账号 */
    private Long targetChatNo;
    /** 违规类型 */
    private String violationType;
    /** 举报说明 */
    private String description;
    /** 证据图片路径，使用换行符分隔 */
    private String evidenceUrls;
    /** 处理状态 */
    private String status;
    /** 处理说明 */
    private String processRemark;
    /** 所属大区编号 */
    private Integer regionId;
    /** 创建时间 */
    private Date createTime;
    /** 处理时间 */
    private Date processTime;
}
