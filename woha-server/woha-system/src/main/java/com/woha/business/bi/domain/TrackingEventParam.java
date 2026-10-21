package com.woha.business.bi.domain;

import java.io.Serializable;

/**
 * 埋点事件参数。
 *
 * @author luowenshen
 */
public class TrackingEventParam implements Serializable
{
    /** 序列化版本号。 */
    private static final long serialVersionUID = 1L;

    /** 参数编号。 */
    private Long paramId;

    /** 事件编号。 */
    private Long eventId;

    /** 参数名称。 */
    private String paramName;

    /** 参数数据类型。 */
    private String paramType;

    /** 是否必填，0 表示否，1 表示是。 */
    private String requiredFlag;

    /** 是否为公共参数，0 表示否，1 表示是。 */
    private String commonFlag;

    /** 参数说明。 */
    private String description;

    /** 排序号。 */
    private Integer sortOrder;

    public Long getParamId() { return paramId; }
    public void setParamId(Long paramId) { this.paramId = paramId; }
    public Long getEventId() { return eventId; }
    public void setEventId(Long eventId) { this.eventId = eventId; }
    public String getParamName() { return paramName; }
    public void setParamName(String paramName) { this.paramName = paramName; }
    public String getParamType() { return paramType; }
    public void setParamType(String paramType) { this.paramType = paramType; }
    public String getRequiredFlag() { return requiredFlag; }
    public void setRequiredFlag(String requiredFlag) { this.requiredFlag = requiredFlag; }
    public String getCommonFlag() { return commonFlag; }
    public void setCommonFlag(String commonFlag) { this.commonFlag = commonFlag; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public Integer getSortOrder() { return sortOrder; }
    public void setSortOrder(Integer sortOrder) { this.sortOrder = sortOrder; }
}
