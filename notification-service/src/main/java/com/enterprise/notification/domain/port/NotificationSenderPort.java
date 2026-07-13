package com.enterprise.notification.domain.port;

import com.enterprise.notification.domain.model.NotificationChannel;

public interface NotificationSenderPort {
    
    /**
     * Determines if this adapter supports the given channel.
     */
    boolean supports(NotificationChannel channel);

    /**
     * Sends the message to the recipient.
     * Returns true if successful, false otherwise.
     */
    boolean send(String message, String recipientId);
}
