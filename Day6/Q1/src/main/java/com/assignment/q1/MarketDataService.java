package com.assignment.q1;

import org.springframework.stereotype.Component;

@Component
public class MarketDataService {
    public void refreshCache() {
        System.out.println("Market data cache warmed up");
    }
}
