package com.assignment.q9;

import org.springframework.stereotype.Service;

@Service("pushNotificationService")
public class PushNotificationService implements NotificationService {
    @Override
    public void send(String message) {
        System.out.println("Push -> " + message);
    }
}
