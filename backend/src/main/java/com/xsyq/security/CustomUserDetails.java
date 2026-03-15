package com.xsyq.security;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.Collections;

@Data
@AllArgsConstructor
public class CustomUserDetails implements UserDetails {

    private Long id;
    private String role; // "USER" 或 "ADMIN"

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        // 将角色转换为 Spring Security 的权限格式，如 "ROLE_USER", "ROLE_ADMIN"
        return Collections.singletonList(new SimpleGrantedAuthority("ROLE_" + role));
    }

    @Override
    public String getPassword() {
        return null; // 我们使用JWT，不需要返回真实密码给SecurityContext
    }

    @Override
    public String getUsername() {
        return id.toString(); // 以 ID 作为识别主体
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }
}
