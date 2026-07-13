package com.kemp.integration.infrastructure.config;

import java.util.UUID;

public class TenantContext {
    private static final ThreadLocal<UUID> currentTenant = new ThreadLocal<>();
    private static final ThreadLocal<UUID> currentUser = new ThreadLocal<>();

    public static void setTenantId(UUID tenantId) { currentTenant.set(tenantId); }
    public static UUID getTenantId() { return currentTenant.get(); }
    
    public static void setUserId(UUID userId) { currentUser.set(userId); }
    public static UUID getUserId() { return currentUser.get(); }

    public static void clear() {
        currentTenant.remove();
        currentUser.remove();
    }
}
