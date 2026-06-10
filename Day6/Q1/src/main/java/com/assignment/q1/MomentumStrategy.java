package com.assignment.q1;

import org.springframework.stereotype.Component;

@Component
public class MomentumStrategy extends AbstractStrategy {
    public MomentumStrategy() {
        this.assetClass = "EQUITY";
    }

    @Override
    public void executeTrade() {
        System.out.println("Momentum strategy executed for " + assetClass);
    }
}
