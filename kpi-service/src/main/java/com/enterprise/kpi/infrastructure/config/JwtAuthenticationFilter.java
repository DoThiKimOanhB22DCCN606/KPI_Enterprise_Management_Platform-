package com.enterprise.kpi.infrastructure.config;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    @Value("${jwt.secret}") private String jwtSecret;
    
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain) throws ServletException, IOException {
        String xUserId = request.getHeader("X-User-Id");
        String xTenantId = request.getHeader("X-Tenant-Id");
        String xUserRoles = request.getHeader("X-User-Roles");

        if (xUserId != null && xTenantId != null) {
            try {
                UUID userId = UUID.fromString(xUserId);
                UUID tenantId = UUID.fromString(xTenantId);
                
                TenantContext.setTenantId(tenantId);
                
                List<GrantedAuthority> authorities = java.util.Arrays.stream(xUserRoles.split(","))
                    .filter(r -> !r.trim().isEmpty())
                    .map(r -> (GrantedAuthority) new SimpleGrantedAuthority("ROLE_" + r))
                    .collect(Collectors.toList());
                
                UsernamePasswordAuthenticationToken auth = 
                    new UsernamePasswordAuthenticationToken(userId, null, authorities);
                auth.setDetails(Map.of("tenantId", tenantId, "userId", userId));
                SecurityContextHolder.getContext().setAuthentication(auth);
            } catch (Exception e) {
                // Skip on malformed gateway headers
            }
        } else {
            String header = request.getHeader("Authorization");
            if (header != null && header.startsWith("Bearer ")) {
                String token = header.substring(7);
                try {
                    Claims claims = Jwts.parserBuilder()
                        .setSigningKey(Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8)))
                        .build()
                        .parseClaimsJws(token)
                        .getBody();
                    
                    UUID userId = UUID.fromString(claims.getSubject());
                    UUID tenantId = UUID.fromString(claims.get("tenantId", String.class));
                    @SuppressWarnings("unchecked")
                    List<String> roles = claims.get("roles", List.class);
                    
                    TenantContext.setTenantId(tenantId);
                    
                    List<GrantedAuthority> authorities = roles.stream()
                        .map(r -> (GrantedAuthority) new SimpleGrantedAuthority("ROLE_" + r))
                        .collect(Collectors.toList());
                    UsernamePasswordAuthenticationToken auth = 
                        new UsernamePasswordAuthenticationToken(userId, null, authorities);
                    auth.setDetails(Map.of("tenantId", tenantId, "userId", userId));
                    SecurityContextHolder.getContext().setAuthentication(auth);
                    
                } catch (JwtException e) {
                    response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                    response.getWriter().write("{\"error\":\"Invalid or expired token\"}");
                    return;
                }
            }
        }
        try {
            chain.doFilter(request, response);
        } finally {
            TenantContext.clear(); // ALWAYS clear after request
        }
    }
}
