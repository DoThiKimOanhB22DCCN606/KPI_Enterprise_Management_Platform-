package com.enterprise.audit.infrastructure.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@FeignClient(name = "user-service", url = "${downstream.user-service.url:http://user-service:8080}")
public interface UserClient {

    @PostMapping("/v1/users/tenants/batch")
    Map<UUID, UUID> getTenantMappings(@RequestBody List<UUID> userIds);
}
