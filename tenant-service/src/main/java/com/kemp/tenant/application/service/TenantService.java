package com.kemp.tenant.application.service;

import com.kemp.tenant.application.dto.CreateTenantRequest;
import com.kemp.tenant.application.dto.UpdateSubscriptionRequest;
import com.kemp.tenant.application.dto.UpdateTenantRequest;
import com.kemp.tenant.application.dto.UpdateThemeRequest;
import com.kemp.tenant.domain.model.Tenant;
import com.kemp.tenant.domain.model.TenantSubscription;
import com.kemp.tenant.domain.model.TenantTheme;
import com.kemp.tenant.domain.repository.TenantRepository;
import com.kemp.tenant.domain.repository.TenantSubscriptionRepository;
import com.kemp.tenant.domain.repository.TenantThemeRepository;
import com.kemp.tenant.infrastructure.messaging.TenantEventPublisher;
import com.kemp.tenant.infrastructure.storage.StorageService;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
public class TenantService {

    private final TenantRepository tenantRepository;
    private final TenantSubscriptionRepository subscriptionRepository;
    private final TenantThemeRepository themeRepository;
    private final TenantEventPublisher eventPublisher;
    private final StorageService storageService;

    @Transactional
    public Tenant createTenant(CreateTenantRequest request) {
        Tenant tenant = Tenant.builder()
            .code(request.getCode())
            .name(request.getName())
            .timezone(request.getTimezone())
            .status("ACTIVE")
            .build();
        tenant = tenantRepository.save(tenant);

        TenantSubscription sub = TenantSubscription.builder()
            .tenantId(tenant.getId())
            .planType("FREE")
            .maxUsers(10)
            .maxKpis(50)
            .build();
        subscriptionRepository.save(sub);

        TenantTheme theme = TenantTheme.builder()
            .tenantId(tenant.getId())
            .primaryColor("#1976D2")
            .secondaryColor("#424242")
            .fontFamily("Inter")
            .build();
        themeRepository.save(theme);

        eventPublisher.publishTenantCreated(tenant.getId(), tenant.getCode());
        return tenant;
    }

    public Tenant getTenant(UUID id) {
        return tenantRepository.findById(id).orElseThrow(() -> new RuntimeException("Tenant not found"));
    }

    @Transactional
    public Tenant updateTenant(UUID id, UpdateTenantRequest request) {
        Tenant tenant = getTenant(id);
        tenant.setName(request.getName());
        tenant.setTimezone(request.getTimezone());
        return tenantRepository.save(tenant);
    }

    @Transactional
    public Tenant deactivateTenant(UUID id) {
        Tenant tenant = getTenant(id);
        tenant.setStatus("INACTIVE");
        return tenantRepository.save(tenant);
    }

    public TenantSubscription getSubscription(UUID id) {
        return subscriptionRepository.findByTenantId(id).orElseThrow(() -> new RuntimeException("Subscription not found"));
    }

    @Transactional
    public TenantSubscription updateSubscription(UUID id, UpdateSubscriptionRequest request) {
        TenantSubscription sub = getSubscription(id);
        sub.setPlanType(request.getPlanType());
        sub.setMaxUsers(request.getMaxUsers());
        sub.setMaxKpis(request.getMaxKpis());
        sub.setExpiresAt(request.getExpiresAt());
        return subscriptionRepository.save(sub);
    }

    public TenantTheme getTheme(UUID id) {
        return themeRepository.findByTenantId(id).orElseThrow(() -> new RuntimeException("Theme not found"));
    }

    @Transactional
    public TenantTheme updateTheme(UUID id, UpdateThemeRequest request) {
        TenantTheme theme = getTheme(id);
        if (request.getPrimaryColor() != null) theme.setPrimaryColor(request.getPrimaryColor());
        if (request.getSecondaryColor() != null) theme.setSecondaryColor(request.getSecondaryColor());
        if (request.getFontFamily() != null) theme.setFontFamily(request.getFontFamily());
        if (request.getCompanyName() != null) theme.setCompanyName(request.getCompanyName());
        if (request.getTagline() != null) theme.setTagline(request.getTagline());
        return themeRepository.save(theme);
    }

    @Transactional
    public String uploadLogo(UUID id, MultipartFile file) {
        Tenant tenant = getTenant(id);
        String ext = file.getOriginalFilename() != null && file.getOriginalFilename().contains(".") 
            ? file.getOriginalFilename().substring(file.getOriginalFilename().lastIndexOf(".")) : ".png";
        String objectKey = id.toString() + "/logos/" + UUID.randomUUID() + ext;
        
        String path = storageService.upload(file, objectKey);
        
        TenantTheme theme = getTheme(id);
        theme.setLogoUrl(path);
        themeRepository.save(theme);
        
        tenant.setLogoUrl(path);
        tenantRepository.save(tenant);
        
        return path;
    }
}
