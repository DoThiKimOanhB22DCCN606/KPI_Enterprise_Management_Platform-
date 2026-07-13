package com.enterprise.notification.infrastructure.persistence;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface NotificationRepository extends JpaRepository<NotificationEntity, UUID> {
    
    @Query("SELECT n FROM NotificationEntity n WHERE n.userId = :userId AND n.channel = 'IN_APP' AND n.readAt IS NULL ORDER BY n.createdAt DESC")
    List<NotificationEntity> findUnreadInAppNotifications(@Param("userId") UUID userId, Pageable pageable);

    @Query("SELECT COUNT(n) FROM NotificationEntity n WHERE n.userId = :userId AND n.channel = 'IN_APP' AND n.readAt IS NULL")
    long countUnreadInAppNotifications(@Param("userId") UUID userId);
}
