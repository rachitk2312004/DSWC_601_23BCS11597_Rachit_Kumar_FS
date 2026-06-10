package com.assignment.q5;

import org.springframework.stereotype.Component;

@Component
public class SlackIntegration implements WebhookIntegration {
    @Override
    public void send(String payload) {
        System.out.println("Slack: " + payload);
    }
}
