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
import org.springframework.web.filter.OncePerRequestFilter;
import com.woha.control.common.core.domain.model.ExternalLoginUser;
import com.woha.control.common.utils.StringUtils;
import com.woha.control.framework.web.service.ExternalTokenService;

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

    public ExternalAuthenticationTokenFilter(ExternalTokenService externalTokenService)
    {
        this.externalTokenService = externalTokenService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException
    {
        String token = request.getHeader(TOKEN_HEADER);
        if (StringUtils.isNotEmpty(token) && SecurityContextHolder.getContext().getAuthentication() == null)
        {
            ExternalLoginUser loginUser = externalTokenService.getLoginUser(token);
            if (loginUser != null)
            {
                UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(loginUser, null, loginUser.getAuthorities());
                authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(authentication);
            }
        }
        chain.doFilter(request, response);
    }
}
