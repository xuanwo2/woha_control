package com.woha.control.framework.security.filter;

import java.io.IOException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.context.annotation.Lazy;
import org.springframework.web.filter.OncePerRequestFilter;
import com.alibaba.fastjson2.JSON;
import com.woha.control.common.constant.HttpStatus;
import com.woha.control.common.core.domain.AjaxResult;
import com.woha.control.common.core.domain.model.ExternalLoginUser;
import com.woha.control.common.utils.ServletUtils;
import com.woha.control.common.utils.StringUtils;
import com.woha.control.framework.web.service.ExternalTokenService;
import com.woha.control.system.service.IExternalManagerAccountService;

/**
 * 外管令牌认证过滤器
 *
 * @author luowenshen
 */
@Component
public class ExternalAuthenticationTokenFilter extends OncePerRequestFilter
{
    /** 外管令牌请求头 */
    public static final String TOKEN_HEADER = "X-External-Token";
    private final ExternalTokenService externalTokenService;
    private final IExternalManagerAccountService accountService;

    public ExternalAuthenticationTokenFilter(ExternalTokenService externalTokenService,
            @Lazy IExternalManagerAccountService accountService)
    {
        this.externalTokenService = externalTokenService;
        this.accountService = accountService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException
    {
        String token = request.getHeader(TOKEN_HEADER);
        if (StringUtils.isNotEmpty(token))
        {
            ExternalLoginUser loginUser = externalTokenService.getLoginUser(token);
            // 每次请求核验业务库状态，避免跨服务缓存前缀不同导致停用账号继续访问。
            if (loginUser == null || !accountService.isAccountEnabled(loginUser.getAccountId()))
            {
                externalTokenService.invalidateToken(token);
                SecurityContextHolder.clearContext();
                ServletUtils.renderString(response, JSON.toJSONString(AjaxResult.error(HttpStatus.UNAUTHORIZED,
                        "外管账号已停用或登录状态已失效，请重新登录")));
                return;
            }
            if (SecurityContextHolder.getContext().getAuthentication() == null)
            {
                UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(loginUser, null, loginUser.getAuthorities());
                authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(authentication);
            }
        }
        chain.doFilter(request, response);
    }
}
