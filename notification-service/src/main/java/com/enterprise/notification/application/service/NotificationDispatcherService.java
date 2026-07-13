package com.enterprise.notification.application.service;

import com.enterprise.notification.domain.model.AlertTriggeredEvent;
import com.enterprise.notification.domain.model.EscalationLevel;
import com.enterprise.notification.domain.model.NotificationChannel;
import com.enterprise.notification.domain.model.Severity;
import com.enterprise.notification.domain.port.NotificationEventPublisherPort;
import com.enterprise.notification.infrastructure.adapter.channel.*;
import com.enterprise.notification.infrastructure.persistence.NotificationEntity;
import com.enterprise.notification.infrastructure.persistence.NotificationRepository;
import com.enterprise.notification.infrastructure.client.UserClient;
import com.enterprise.notification.application.dto.UserDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.jdbc.core.JdbcTemplate;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationDispatcherService {

    private final EmailSenderAdapter emailAdapter;
    private final SlackSenderAdapter slackAdapter;
    private final TelegramSenderAdapter telegramAdapter;
    private final ZaloSenderAdapter zaloAdapter;
    private final InAppNotificationAdapter inAppAdapter;

    private final NotificationRepository notificationRepository;
    private final NotificationEventPublisherPort eventPublisherPort;
    private final UserClient userClient;
    private final JdbcTemplate jdbcTemplate;

    private UUID resolveUserIdFromKpi(String kpiIdStr) {
        try {
            UUID kpiId = UUID.fromString(kpiIdStr);
            String sql = "SELECT assignee_id FROM kpis WHERE id = ?";
            return jdbcTemplate.queryForObject(sql, UUID.class, kpiId);
        } catch (Exception e) {
            log.warn("Failed to resolve assignee_id for KPI {}", kpiIdStr);
            return UUID.fromString("00000000-0000-0000-0000-000000000002");
        }
    }

    public void dispatch(AlertTriggeredEvent event) {
        log.info("Dispatching notification for alert: {}", event.getEventId());

        EscalationLevel escalationLevel = determineEscalationLevel(event.getSeverity());
        List<NotificationChannel> channels = determineChannels(escalationLevel);

        for (NotificationChannel channel : channels) {
            String recipientId = "recipient-" + escalationLevel.name().toLowerCase();
            UUID userId = UUID.fromString("00000000-0000-0000-0000-000000000002");
            if ("KPI".equalsIgnoreCase(event.getSourceType()) && event.getSourceId() != null) {
                userId = resolveUserIdFromKpi(event.getSourceId());
            }
            
            String targetEmail = "test@example.com";
            try {
                UserDTO user = userClient.getUserById(userId);
                if (user != null && user.getEmail() != null) {
                    targetEmail = user.getEmail();
                }
            } catch (Exception e) {
                log.warn("Failed to fetch user profile from user-service for user {}. Using fallback.", userId);
            }
            
            boolean success = false;
            try {
                switch (channel) {
                    case EMAIL:
                        emailAdapter.send(targetEmail, "Alert " + event.getSeverity(), event.getMessage());
                        success = true;
                        break;
                    case SLACK:
                        slackAdapter.send(event.getMessage());
                        success = true;
                        break;
                    case TELEGRAM:
                        telegramAdapter.send("12345", event.getMessage());
                        success = true;
                        break;
                    case ZALO:
                        zaloAdapter.send("67890", event.getMessage());
                        success = true;
                        break;
                    case IN_APP:
                        inAppAdapter.send(userId, event.getMessage());
                        success = true; // InApp adapter handles its own saving
                        break;
                }
            } catch (Exception e) {
                log.error("Failed to send notification via {}", channel, e);
            }

            if (channel != NotificationChannel.IN_APP) {
                NotificationEntity notification = new NotificationEntity();
                notification.setId(UUID.randomUUID());
                notification.setUserId(userId);
                notification.setChannel(channel.name());
                notification.setMessage(event.getMessage());
                notification.setCreatedAt(Instant.now());
                if (success) {
                    notification.setStatus("SENT");
                    notification.setSentAt(Instant.now());
                } else {
                    notification.setStatus("FAILED");
                    // publish to retry queue could happen here
                }
                notificationRepository.save(notification);
            }

            eventPublisherPort.publishNotificationSentEvent(
                    UUID.randomUUID().toString(),
                    event.getTenantId(),
                    event.getEventId(),
                    channel,
                    escalationLevel,
                    success
            );
        }
    }

    private EscalationLevel determineEscalationLevel(Severity severity) {
        if (severity == null) return EscalationLevel.EMPLOYEE;
        return switch (severity) {
            case INFO -> EscalationLevel.EMPLOYEE;
            case WARNING -> EscalationLevel.MANAGER;
            case CRITICAL -> EscalationLevel.CEO;
        };
    }

    public void dispatch(com.enterprise.notification.domain.model.KpiDroppedEvent event) {
        log.info("Dispatching notification for KPI dropped alert: {}", event.getEventId());

        EscalationLevel escalationLevel = determineEscalationLevel(event.getSeverity());
        List<NotificationChannel> channels = determineChannels(escalationLevel);

        for (NotificationChannel channel : channels) {
            UUID userId = resolveUserIdFromKpi(event.getKpiId().toString()); 
            
            String targetEmail = "test@example.com";
            try {
                UserDTO user = userClient.getUserById(userId);
                if (user != null && user.getEmail() != null) {
                    targetEmail = user.getEmail();
                }
            } catch (Exception e) {
                log.warn("Failed to fetch user profile from user-service for user {}. Using fallback.", userId);
            }
            
            boolean success = false;
            try {
                switch (channel) {
                    case EMAIL:
                        emailAdapter.send(targetEmail, "KPI Dropped Alert: " + event.getSeverity(), event.getMessage());
                        success = true;
                        break;
                    case SLACK:
                        slackAdapter.send("KPI Dropped Alert: " + event.getMessage());
                        success = true;
                        break;
                    case TELEGRAM:
                        telegramAdapter.send("12345", "KPI Dropped Alert: " + event.getMessage());
                        success = true;
                        break;
                    case ZALO:
                        zaloAdapter.send("67890", "KPI Dropped Alert: " + event.getMessage());
                        success = true;
                        break;
                    case IN_APP:
                        inAppAdapter.send(userId, "KPI Dropped Alert: " + event.getMessage());
                        success = true; 
                        break;
                }
            } catch (Exception e) {
                log.error("Failed to send notification via {}", channel, e);
                // Throw an exception here so that Spring Retry in the Consumer gets triggered
                throw new com.enterprise.notification.domain.exception.NotificationDeliveryException("Delivery failed", e);
            }

            if (channel != NotificationChannel.IN_APP) {
                NotificationEntity notification = new NotificationEntity();
                notification.setId(UUID.randomUUID());
                notification.setUserId(userId);
                notification.setChannel(channel.name());
                notification.setMessage(event.getMessage());
                notification.setCreatedAt(Instant.now());
                if (success) {
                    notification.setStatus("SENT");
                    notification.setSentAt(Instant.now());
                } else {
                    notification.setStatus("FAILED");
                }
                notificationRepository.save(notification);
            }

            eventPublisherPort.publishNotificationSentEvent(
                    UUID.randomUUID().toString(),
                    event.getTenantId(),
                    event.getEventId(),
                    channel,
                    escalationLevel,
                    success
            );
        }
    }

    private List<NotificationChannel> determineChannels(EscalationLevel level) {
        return switch (level) {
            case EMPLOYEE -> List.of(NotificationChannel.EMAIL, NotificationChannel.IN_APP, NotificationChannel.ZALO);
            case MANAGER -> List.of(NotificationChannel.SLACK, NotificationChannel.IN_APP);
            case DIRECTOR -> List.of(NotificationChannel.EMAIL, NotificationChannel.SLACK, NotificationChannel.IN_APP);
            case CEO -> List.of(NotificationChannel.TELEGRAM, NotificationChannel.IN_APP);
        };
    }
}
