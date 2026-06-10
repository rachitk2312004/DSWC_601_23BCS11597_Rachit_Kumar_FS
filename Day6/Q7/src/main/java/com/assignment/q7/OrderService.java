package com.assignment.q7;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class OrderService {
    private InventoryService inventoryService;

    public OrderService() {
    }

    @Autowired
    public void setInventoryService(InventoryService inventoryService) {
        this.inventoryService = inventoryService;
    }

    public void placeOrder() {
        inventoryService.reserveStock();
        System.out.println("Order placed");
    }
}
