package com.assignment.q5;

import org.springframework.stereotype.Component;

@Component
public class EmailIntegration implements WebhookIntegration {
    @Override
    public void send(String payload) {
        System.out.println("Email: " + payload);
    }
}
