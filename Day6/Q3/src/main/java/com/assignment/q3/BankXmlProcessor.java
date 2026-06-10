package com.assignment.q3;

public class BankXmlProcessor implements PaymentProcessor {
    private final String bankName;
    private final String endpoint;
    private final int timeoutSeconds;

    BankXmlProcessor(String bankName, String endpoint, int timeoutSeconds) {
        this.bankName = bankName;
        this.endpoint = endpoint;
        this.timeoutSeconds = timeoutSeconds;
    }

    @Override
    public String processPayment(double amount) {
        return bankName + " XML payment sent to " + endpoint + " for " + amount;
    }

    static class Builder {
        private String bankName;
        private String endpoint;
        private int timeoutSeconds;

        Builder bankName(String bankName) {
            this.bankName = bankName;
            return this;
        }

        Builder endpoint(String endpoint) {
            this.endpoint = endpoint;
            return this;
        }

        Builder timeoutSeconds(int timeoutSeconds) {
            this.timeoutSeconds = timeoutSeconds;
            return this;
        }

        BankXmlProcessor build() {
            return new BankXmlProcessor(bankName, endpoint, timeoutSeconds);
        }
    }
}
