package com.xsyq.security;

import com.xsyq.utils.JwtUtils;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtUtils jwtUtils;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        
        final String authHeader = request.getHeader("Authorization");
        
        // 验证请求头是否包含有效的 Authorization (Bearer )
        if (!StringUtils.hasText(authHeader) || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        final String jwt = authHeader.substring(7);
        
        try {
            // 验证 token 并在上下文中无认证信息时进行设置
            if (jwtUtils.validateToken(jwt) && SecurityContextHolder.getContext().getAuthentication() == null) {
                Long id = jwtUtils.extractId(jwt);
                String role = jwtUtils.extractRole(jwt);

                CustomUserDetails userDetails = new CustomUserDetails(id, role);

                UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                        userDetails,
                        null,
                        userDetails.getAuthorities()
                );
                authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                // 将认证信息存入上下文
                SecurityContextHolder.getContext().setAuthentication(authToken);
            }
        } catch (Exception e) {
            // 如果 Token 验证失败，记录日志，继续走 filter (不设置 Authentication)，后续会被 Spring Security 拦截
            logger.error("Token 验证失败: " + e.getMessage());
        }

        filterChain.doFilter(request, response);
    }
}
