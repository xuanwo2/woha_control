package com.woha.control.common.core.domain.model;

import java.io.Serializable;
import java.util.Collection;
import java.util.Collections;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

/**
 * 外管登录会话
 *
 * @author luowenshen
 */
public class ExternalLoginUser implements UserDetails, Serializable
{
    private static final long serialVersionUID = 1L;
    /** 外管账号主键 */
    private Long accountId;
    /** 登录账号 */
    private String loginName;
    /** 是否需要修改密码 */
    private boolean passwordChangeRequired;

    public ExternalLoginUser() { }

    public ExternalLoginUser(Long accountId, String loginName, boolean passwordChangeRequired)
    {
        this.accountId = accountId;
        this.loginName = loginName;
        this.passwordChangeRequired = passwordChangeRequired;
    }

    public Long getAccountId() { return accountId; }

    public String getLoginName() { return loginName; }

    public boolean isPasswordChangeRequired() { return passwordChangeRequired; }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() { return Collections.emptySet(); }

    @Override
    public String getPassword() { return ""; }

    @Override
    public String getUsername() { return loginName; }

    @Override
    public boolean isAccountNonExpired() { return true; }

    @Override
    public boolean isAccountNonLocked() { return true; }

    @Override
    public boolean isCredentialsNonExpired() { return true; }

    @Override
    public boolean isEnabled() { return true; }
}
