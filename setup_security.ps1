$ErrorActionPreference = 'Stop'

$services = @("kpi-service", "goal-service", "analytics-service", "notification-service", "audit-service", "dashboard-service", "organization-service", "user-service", "tenant-service", "integration-service", "ai-service", "report-service", "alert-service")
$baseDir = "d:\prj_vdt"

foreach ($svc in $services) {
    $svcPath = Join-Path $baseDir $svc
    if (-not (Test-Path $svcPath)) {
        Write-Host "Skipping $svc - not found"
        continue
    }
    
    # 1. Update POM
    $pomPath = Join-Path $svcPath "pom.xml"
    if (Test-Path $pomPath) {
        $pom = Get-Content $pomPath -Raw
        if ($pom -notmatch "spring-boot-starter-security") {
            $deps = @"
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-security</artifactId>
        </dependency>
        <dependency>
            <groupId>io.jsonwebtoken</groupId>
            <artifactId>jjwt-api</artifactId>
            <version>0.11.5</version>
        </dependency>
        <dependency>
            <groupId>io.jsonwebtoken</groupId>
            <artifactId>jjwt-impl</artifactId>
            <version>0.11.5</version>
            <scope>runtime</scope>
        </dependency>
        <dependency>
            <groupId>io.jsonwebtoken</groupId>
            <artifactId>jjwt-jackson</artifactId>
            <version>0.11.5</version>
            <scope>runtime</scope>
        </dependency>
"@
            $pom = $pom -replace "</dependencies>", "$deps`n    </dependencies>"
            Set-Content $pomPath $pom -NoNewline
        }
    }
    
    # 2. Update Application YML
    $ymlPath = Join-Path $svcPath "src\main\resources\application.yml"
    if (Test-Path $ymlPath) {
        $yml = Get-Content $ymlPath -Raw
        if ($yml -notmatch "jwt.secret") {
            $yml += "`n`njwt.secret: `${JWT_SECRET:kemp-super-secret-jwt-key-must-be-256-bits-long-replace-in-prod}`n"
            Set-Content $ymlPath $yml -NoNewline
        }
    }
    
    # 3. Find base package
    $srcMainJava = Join-Path $svcPath "src\main\java"
    $appClassPath = Get-ChildItem -Path $srcMainJava -Recurse -Filter "*Application.java" | Select-Object -First 1
    if ($appClassPath) {
        $content = Get-Content $appClassPath.FullName -Raw
        if ($content -match "package\s+([a-zA-Z0-9_.]+);") {
            $basePackage = $matches[1]
            $basePkgPath = $appClassPath.DirectoryName
            $configDir = Join-Path $basePkgPath "infrastructure\config"
            if (-not (Test-Path $configDir)) {
                New-Item -ItemType Directory -Path $configDir -Force | Out-Null
            }
            
            # Write TenantContext if not exists
            $tcPath = Join-Path $configDir "TenantContext.java"
            if (-not (Test-Path $tcPath)) {
                $tcContent = @"
package $basePackage.infrastructure.config;

import java.util.UUID;

public class TenantContext {
    private static final ThreadLocal<UUID> currentTenant = new ThreadLocal<>();

    public static void setTenantId(UUID tenantId) {
        currentTenant.set(tenantId);
    }

    public static UUID getTenantId() {
        return currentTenant.get();
    }

    public static void clear() {
        currentTenant.remove();
    }
}
"@
                Set-Content $tcPath $tcContent
            }
            
            # Write JwtAuthenticationFilter
            $jwtPath = Join-Path $configDir "JwtAuthenticationFilter.java"
            $jwtContent = @"
package $basePackage.infrastructure.config;

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
    @Value("`${jwt.secret}") private String jwtSecret;
    
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain) throws ServletException, IOException {
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
                List<String> roles = claims.get("roles", List.class);
                
                // Set TenantContext for downstream repository filtering
                TenantContext.setTenantId(tenantId);
                
                // Set Spring Security context
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
        try {
            chain.doFilter(request, response);
        } finally {
            TenantContext.clear(); // ALWAYS clear after request
        }
    }
}
"@
            Set-Content $jwtPath $jwtContent
            
            # Write SecurityConfig
            $secPath = Join-Path $configDir "SecurityConfig.java"
            $secContent = @"
package $basePackage.infrastructure.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
public class SecurityConfig {
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http, JwtAuthenticationFilter jwtFilter) throws Exception {
        http
            .csrf(AbstractHttpConfigurer::disable) // CSRF handled at gateway level
            .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/actuator/health", "/actuator/info", "/actuator/prometheus").permitAll()
                .requestMatchers("/v3/api-docs/**", "/swagger-ui/**").permitAll()
                .anyRequest().authenticated()
            )
            .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }
}
"@
            Set-Content $secPath $secContent
            Write-Host "Processed $svc"
        }
    }
}
