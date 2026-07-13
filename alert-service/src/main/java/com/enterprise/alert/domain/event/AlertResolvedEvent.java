package com.enterprise.alert.domain.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.OffsetDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AlertResolvedEvent {
    @Builder.Default
    private UUID eventId = UUID.randomUUID();
    private UUID alertId;
    private UUID tenantId;
    private UUID ruleId;
    private OffsetDateTime resolvedAt;
}
