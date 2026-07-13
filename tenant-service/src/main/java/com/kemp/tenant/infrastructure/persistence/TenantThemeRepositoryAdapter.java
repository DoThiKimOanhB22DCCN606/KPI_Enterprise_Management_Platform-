package com.kemp.tenant.infrastructure.persistence;

import com.kemp.tenant.domain.model.TenantTheme;
import com.kemp.tenant.domain.repository.TenantThemeRepository;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class TenantThemeRepositoryAdapter implements TenantThemeRepository {
    
    private final TenantThemeJpaRepository themeRepo;

    @Override
    public TenantTheme save(TenantTheme theme) {
        TenantThemeEntity entity = new TenantThemeEntity();
        entity.setId(theme.getId() == null ? UUID.randomUUID() : theme.getId());
        entity.setTenantId(theme.getTenantId());
        entity.setPrimaryColor(theme.getPrimaryColor());
        entity.setSecondaryColor(theme.getSecondaryColor());
        entity.setLogoUrl(theme.getLogoUrl());
        entity.setFaviconUrl(theme.getFaviconUrl());
        entity.setFontFamily(theme.getFontFamily());
        entity.setCompanyName(theme.getCompanyName());
        entity.setTagline(theme.getTagline());
        entity = themeRepo.save(entity);
        theme.setId(entity.getId());
        return theme;
    }

    @Override
    public Optional<TenantTheme> findByTenantId(UUID tenantId) {
        return themeRepo.findByTenantId(tenantId).map(this::toThemeDomain);
    }

    private TenantTheme toThemeDomain(TenantThemeEntity entity) {
        return TenantTheme.builder()
            .id(entity.getId())
            .tenantId(entity.getTenantId())
            .primaryColor(entity.getPrimaryColor())
            .secondaryColor(entity.getSecondaryColor())
            .logoUrl(entity.getLogoUrl())
            .faviconUrl(entity.getFaviconUrl())
            .fontFamily(entity.getFontFamily())
            .companyName(entity.getCompanyName())
            .tagline(entity.getTagline())
            .updatedAt(entity.getUpdatedAt())
            .build();
    }
}
