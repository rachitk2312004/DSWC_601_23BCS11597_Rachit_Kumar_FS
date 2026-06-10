package com.assignment.q9;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class NotificationRoutingService {
    private final NotificationService smsNotificationService;
    private final List<NotificationService> allNotifications;

    public NotificationRoutingService(
            @Qualifier("smsNotificationService") NotificationService smsNotificationService,
            List<NotificationService> allNotifications) {
        this.smsNotificationService = smsNotificationService;
        this.allNotifications = allNotifications;
    }

    public void sendTargetedAlert(String message) {
        smsNotificationService.send(message);
    }

    public void broadcast(String message) {
        for (NotificationService service : allNotifications) {
            service.send(message);
        }
    }
}
