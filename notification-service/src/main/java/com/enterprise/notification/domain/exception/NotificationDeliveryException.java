package com.enterprise.notification.domain.exception;

public class NotificationDeliveryException extends RuntimeException {
    private final String channel;
    
    public NotificationDeliveryException(String channel, Throwable cause) {
        super("Failed to deliver notification via " + channel, cause);
        this.channel = channel;
    }
    
    public String getChannel() { return channel; }
}
