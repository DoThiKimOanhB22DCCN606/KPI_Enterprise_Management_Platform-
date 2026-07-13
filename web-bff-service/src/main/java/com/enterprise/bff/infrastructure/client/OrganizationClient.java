package com.enterprise.bff.infrastructure.client;

import com.enterprise.bff.application.dto.OrgDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.UUID;

@FeignClient(name = "org-service", url = "${downstream.org-service.url}")
public interface OrganizationClient {

    @GetMapping("/v1/organizations/{id}")
    OrgDTO getOrganizationById(@PathVariable("id") UUID id);
}
