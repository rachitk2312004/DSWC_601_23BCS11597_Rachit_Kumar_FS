package com.assignment.q1;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import org.springframework.beans.factory.BeanNameAware;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class TradingEngine implements BeanNameAware, InitializingBean {
    private final MarketDataService marketDataService;
    private final List<TradingStrategy> strategies;
    private AlertService alertService;
    private String beanName;

    public TradingEngine(MarketDataService marketDataService, List<TradingStrategy> strategies) {
        this.marketDataService = marketDataService;
        this.strategies = strategies;
    }

    @Autowired(required = false)
    public void setAlertService(AlertService alertService) {
        this.alertService = alertService;
    }

    @Override
    public void setBeanName(String name) {
        this.beanName = name;
        System.out.println("Bean name captured: " + beanName);
    }

    @PostConstruct
    public void warmUpCache() {
        marketDataService.refreshCache();
        System.out.println("Cache warm-up complete for " + beanName);
    }

    @Override
    public void afterPropertiesSet() {
        if (marketDataService == null || strategies == null || strategies.isEmpty()) {
            throw new IllegalStateException("TradingEngine is not fully initialized");
        }
        System.out.println("Safety validation complete for " + beanName);
    }

    @PreDestroy
    public void closeOpenPositions() {
        System.out.println("Closing all open market positions");
        if (alertService != null) {
            alertService.notifyAdmin("TradingEngine shutting down safely");
        }
    }

    public void runStrategies() {
        for (TradingStrategy strategy : strategies) {
            strategy.executeTrade();
        }
    }
}
