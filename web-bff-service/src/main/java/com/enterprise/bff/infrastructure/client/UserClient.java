package com.enterprise.bff.infrastructure.client;

import com.enterprise.bff.application.dto.UserDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.UUID;

@FeignClient(name = "user-service", url = "${downstream.user-service.url}")
public interface UserClient {
    
    @GetMapping("/v1/users/{id}")
    UserDTO getUserById(@PathVariable("id") UUID id);
}
