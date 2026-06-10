package com.assignment.q4;

import org.springframework.stereotype.Component;

@Component
public class TransactionService implements PIIProcessor {
    public void process() {
        System.out.println("Processing sensitive transaction");
    }
}
