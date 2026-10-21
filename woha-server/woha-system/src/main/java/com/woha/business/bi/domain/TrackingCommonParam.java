package com.woha.business.bi.domain;

import com.woha.business.common.core.domain.BaseEntity;

/**
 * 埋点公共参数。
 *
 * @author luowenshen
 */
public class TrackingCommonParam extends BaseEntity
{
    /** 序列化版本号。 */
    private static final long serialVersionUID = 1L;

    /** 公共参数编号。 */
    private Long commonParamId;

    /** 参数名称。 */
    private String paramName;

    /** 参数数据类型。 */
    private String paramType;

    /** 参数说明。 */
    private String description;

    /** 首次启用版本。 */
    private String versionNo;

    /** 状态，0 表示启用，1 表示停用，2 表示已废弃。 */
    private String status;

    public Long getCommonParamId() { return commonParamId; }
    public void setCommonParamId(Long commonParamId) { this.commonParamId = commonParamId; }
    public String getParamName() { return paramName; }
    public void setParamName(String paramName) { this.paramName = paramName; }
    public String getParamType() { return paramType; }
    public void setParamType(String paramType) { this.paramType = paramType; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getVersionNo() { return versionNo; }
    public void setVersionNo(String versionNo) { this.versionNo = versionNo; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}
