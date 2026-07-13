$ErrorActionPreference = 'Stop'

$services = @(
    "gateway-service", "auth-service", "kpi-service", "goal-service",
    "report-service", "alert-service", "web-bff-service", "notification-service",
    "analytics-service", "ai-service", "audit-service", "user-service",
    "tenant-service", "organization-service", "dashboard-service", "integration-service"
)

$actuatorDep = @"
		<dependency>
			<groupId>org.springframework.boot</groupId>
			<artifactId>spring-boot-starter-actuator</artifactId>
		</dependency>
"@

$prometheusDep = @"
		<dependency>
			<groupId>io.micrometer</groupId>
			<artifactId>micrometer-registry-prometheus</artifactId>
		</dependency>
"@

$actuatorConfig = @"

management:
  endpoints:
    web:
      exposure:
        include: health,info,prometheus,metrics
  endpoint:
    health:
      show-details: always
      probes:
        enabled: true
  metrics:
    export:
      prometheus:
        enabled: true
"@

$correlationFilter = @"
package com.kemp.replace.infrastructure.config;

import org.slf4j.MDC;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Optional;
import java.util.UUID;

@Component
public class CorrelationIdFilter extends OncePerRequestFilter {
    @Override
    protected void doFilterInternal(HttpServletRequest req, HttpServletResponse res, FilterChain chain) throws ServletException, IOException {
        String correlationId = Optional.ofNullable(req.getHeader("X-Correlation-ID"))
            .orElse(UUID.randomUUID().toString());
        MDC.put("correlationId", correlationId);
        res.setHeader("X-Correlation-ID", correlationId);
        try {
            chain.doFilter(req, res);
        } finally {
            MDC.clear();
        }
    }
}
"@

$logbackConfig = @"
<configuration>
  <appender name="STDOUT" class="ch.qos.logback.core.ConsoleAppender">
    <encoder>
      <pattern>%d{ISO8601} [%X{correlationId}] %-5level %logger{36} - %msg%n</pattern>
    </encoder>
  </appender>
  <root level="INFO">
    <appender-ref ref="STDOUT"/>
  </root>
</configuration>
"@

$missingReport = @()

foreach ($svc in $services) {
    $svcDir = "d:\prj_vdt\$svc"
    if (-not (Test-Path $svcDir)) { continue }

    $missing = $false

    # 1. pom.xml
    $pomPath = "$svcDir\pom.xml"
    if (Test-Path $pomPath) {
        $pom = Get-Content $pomPath -Raw
        $pomUpdated = $false
        if ($pom -notmatch "spring-boot-starter-actuator") {
            $pom = $pom -replace "</dependencies>", "$actuatorDep`n`t</dependencies>"
            $pomUpdated = $true
            $missing = $true
        }
        if ($pom -notmatch "micrometer-registry-prometheus") {
            $pom = $pom -replace "</dependencies>", "$prometheusDep`n`t</dependencies>"
            $pomUpdated = $true
            $missing = $true
        }
        if ($pomUpdated) {
            Set-Content $pomPath $pom -NoNewline
        }
    }

    # 2. application.yml
    $ymlPath = "$svcDir\src\main\resources\application.yml"
    if (Test-Path $ymlPath) {
        $yml = Get-Content $ymlPath -Raw
        if ($yml -notmatch "management:") {
            $yml = $yml + $actuatorConfig
            Set-Content $ymlPath $yml -NoNewline
            $missing = $true
        }
    }

    if ($missing) {
        $missingReport += $svc
    }

    # 3. CorrelationIdFilter.java
    # We need to find the base package infrastructure/config/
    $pkgName = $svc.Replace("-service", "").Replace("-", "")
    if ($svc -eq "web-bff-service") { $pkgName = "webbff" }
    
    $configDir = "$svcDir\src\main\java\com\kemp\$pkgName\infrastructure\config"
    if (-not (Test-Path $configDir)) {
        New-Item -ItemType Directory -Force -Path $configDir | Out-Null
    }
    
    $filterPath = "$configDir\CorrelationIdFilter.java"
    if (-not (Test-Path $filterPath)) {
        $filterContent = $correlationFilter -replace "replace", $pkgName
        Set-Content $filterPath $filterContent -NoNewline
    }

    # 4. logback-spring.xml
    $resDir = "$svcDir\src\main\resources"
    if (-not (Test-Path $resDir)) {
        New-Item -ItemType Directory -Force -Path $resDir | Out-Null
    }
    $logbackPath = "$resDir\logback-spring.xml"
    if (-not (Test-Path $logbackPath)) {
        Set-Content $logbackPath $logbackConfig -NoNewline
    }
}

Write-Host "Services that were missing actuator/prometheus:"
$missingReport | ForEach-Object { Write-Host "- $_" }
