package com.assignment.q1;

import org.springframework.stereotype.Component;

@Component
public class ArbitrageStrategy extends AbstractStrategy {
    public ArbitrageStrategy() {
        this.assetClass = "FX";
    }

    @Override
    public void executeTrade() {
        System.out.println("Arbitrage strategy executed for " + assetClass);
    }
}
