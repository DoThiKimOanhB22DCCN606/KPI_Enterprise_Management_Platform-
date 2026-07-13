package com.enterprise.notification.interfaces.rest;

import com.enterprise.notification.infrastructure.persistence.NotificationEntity;
import com.enterprise.notification.infrastructure.persistence.NotificationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/v1/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationRepository notificationRepository;

    private UUID getCurrentUserId(String header) {
        if (header != null && !header.isEmpty()) {
            try {
                return UUID.fromString(header);
            } catch (Exception e) {
                // fallback
            }
        }
        return UUID.fromString("00000000-0000-0000-0000-000000000001");
    }

    @GetMapping("/inbox")
    public ResponseEntity<List<NotificationEntity>> getInbox(
            @RequestHeader(value = "X-User-Id", required = false) String userIdHeader,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        UUID userId = getCurrentUserId(userIdHeader);
        List<NotificationEntity> unread = notificationRepository.findUnreadInAppNotifications(userId, PageRequest.of(page, size));
        return ResponseEntity.ok(unread);
    }

    @PostMapping("/{id}/read")
    public ResponseEntity<Void> markAsRead(@PathVariable UUID id) {
        notificationRepository.findById(id).ifPresent(notification -> {
            notification.setReadAt(Instant.now());
            notificationRepository.save(notification);
        });
        return ResponseEntity.ok().build();
    }

    @GetMapping("/unread-count")
    public ResponseEntity<Long> getUnreadCount(@RequestHeader(value = "X-User-Id", required = false) String userIdHeader) {
        UUID userId = getCurrentUserId(userIdHeader);
        long count = notificationRepository.countUnreadInAppNotifications(userId);
        return ResponseEntity.ok(count);
    }
}
