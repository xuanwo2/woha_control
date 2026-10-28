package com.woha.control.system.domain.external;

/**
 * 外管账号开通方式。
 *
 * @author luowenshen
 */
public enum ExternalManagerOpenMethod
{
    /** 由运营人员在管理后台直接开通。 */
    OPERATION_OPEN("运营开通"),

    /** 由上级外管账号新增下级时开通。 */
    SUBORDINATE_OPEN("下级新增");

    /** 开通方式展示名称。 */
    private final String description;

    ExternalManagerOpenMethod(String description)
    {
        this.description = description;
    }

    /**
     * 获取开通方式展示名称。
     *
     * @return 开通方式展示名称
     */
    public String getDescription()
    {
        return description;
    }
}
