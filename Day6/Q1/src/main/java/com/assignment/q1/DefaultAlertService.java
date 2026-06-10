package com.assignment.q1;

import org.springframework.stereotype.Component;

@Component
public class DefaultAlertService implements AlertService {
    @Override
    public void notifyAdmin(String message) {
        System.out.println("ALERT: " + message);
    }
}
