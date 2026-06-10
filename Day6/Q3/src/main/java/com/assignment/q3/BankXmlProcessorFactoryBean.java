package com.assignment.q3;

import org.springframework.beans.factory.FactoryBean;
import org.springframework.stereotype.Component;

@Component("bankXmlProcessor")
public class BankXmlProcessorFactoryBean implements FactoryBean<PaymentProcessor> {
    @Override
    public PaymentProcessor getObject() {
        return new BankXmlProcessor.Builder()
                .bankName("LegacyBank")
                .endpoint("https://legacy-bank.example/xml")
                .timeoutSeconds(30)
                .build();
    }

    @Override
    public Class<?> getObjectType() {
        return PaymentProcessor.class;
    }

    @Override
    public boolean isSingleton() {
        return true;
    }
}
