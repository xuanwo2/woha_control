package com.woha.control.system.domain.external;

import lombok.Data;

/**
 * 原业务库装扮摘要。
 *
 * @author luowenshen
 */
@Data
public class ExternalBusinessProp
{
    /** 道具主键 */
    private Long id;
    /** 道具类型 */
    private Integer propType;
    /** 道具名称 */
    private String propName;
}
