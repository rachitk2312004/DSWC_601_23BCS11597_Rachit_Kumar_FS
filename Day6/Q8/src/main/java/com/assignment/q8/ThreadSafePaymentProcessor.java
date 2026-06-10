package com.assignment.q8;

import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
public class ThreadSafePaymentProcessor {
    public BigDecimal processPayment(String transactionId, BigDecimal amount) {
        BigDecimal fee = amount.multiply(new BigDecimal("0.02"));
        BigDecimal netAmount = amount.subtract(fee);
        System.out.println("Transaction " + transactionId + " processed with fee " + fee);
        return netAmount;
    }
}
