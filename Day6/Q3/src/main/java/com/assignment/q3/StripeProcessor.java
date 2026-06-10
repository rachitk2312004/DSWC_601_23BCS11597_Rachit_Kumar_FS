package com.assignment.q3;

import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

@Component
@Primary
public class StripeProcessor implements PaymentProcessor {
    @Override
    public String processPayment(double amount) {
        return "Stripe processed " + amount;
    }
}
