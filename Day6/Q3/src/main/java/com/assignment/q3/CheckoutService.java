package com.assignment.q3;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

@Component
public class CheckoutService {
    private final PaymentProcessor defaultProcessor;
    private final PaymentProcessor bankProcessor;

    public CheckoutService(PaymentProcessor defaultProcessor,
                           @Qualifier("bankXmlProcessor") PaymentProcessor bankProcessor) {
        this.defaultProcessor = defaultProcessor;
        this.bankProcessor = bankProcessor;
    }

    public void checkout(double amount) {
        System.out.println(defaultProcessor.processPayment(amount));
        System.out.println(bankProcessor.processPayment(amount));
    }
}
