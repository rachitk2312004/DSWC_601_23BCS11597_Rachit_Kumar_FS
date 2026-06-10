package com.assignment.q7;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class InventoryService {
    private final OrderService orderService;

    @Autowired
    public InventoryService(OrderService orderService) {
        this.orderService = orderService;
    }

    public void reserveStock() {
        System.out.println("Stock reserved for order");
    }
}
