package com.enterprise.notification.infrastructure.client;

import com.enterprise.notification.application.dto.UserDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.UUID;

@FeignClient(name = "user-service", url = "${downstream.user-service.url:http://user-service:8080}")
public interface UserClient {

    @GetMapping("/v1/users/{id}")
    UserDTO getUserById(@PathVariable("id") UUID id);
}
