package com.xsyq.utils;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Component
public class JwtUtils {

    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.accessTokenExpiration}")
    private long accessTokenExpiration;

    @Value("${jwt.adminTokenExpiration}")
    private long adminTokenExpiration;

    private Key getSigningKey() {
        return Keys.hmacShaKeyFor(secret.getBytes());
    }

    /**
     * 生成 App 用户 Token
     */
    public String generateUserToken(Long userId) {
        return generateToken(userId, "USER", accessTokenExpiration);
    }

    /**
     * 生成 Admin 管理员 Token
     */
    public String generateAdminToken(Long adminId) {
        return generateToken(adminId, "ADMIN", adminTokenExpiration);
    }

    private String generateToken(Long id, String role, long expiration) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("id", id);
        claims.put("role", role); // 用于区分前后台用户

        return Jwts.builder()
                .setClaims(claims)
                .setSubject(id.toString())
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + expiration))
                .signWith(getSigningKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    /**
     * 从 token 中提取所有的 claims
     */
    public Claims extractAllClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    /**
     * 验证 token 并获取用户ID
     */
    public Long extractId(String token) {
        return extractAllClaims(token).get("id", Long.class);
    }

    /**
     * 验证 token 并获取用户角色
     */
    public String extractRole(String token) {
        return extractAllClaims(token).get("role", String.class);
    }

    /**
     * 检查 token 是否过期
     */
    public boolean isTokenExpired(String token) {
        try {
            return extractAllClaims(token).getExpiration().before(new Date());
        } catch (Exception e) {
            return true;
        }
    }

    /**
     * 验证 token 的有效性
     */
    public boolean validateToken(String token) {
        try {
            Jwts.parserBuilder().setSigningKey(getSigningKey()).build().parseClaimsJws(token);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}
