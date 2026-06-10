package com.assignment.q3;

import org.springframework.stereotype.Component;

@Component
public class SquareProcessor implements PaymentProcessor {
    @Override
    public String processPayment(double amount) {
        return "Square processed " + amount;
    }
}
