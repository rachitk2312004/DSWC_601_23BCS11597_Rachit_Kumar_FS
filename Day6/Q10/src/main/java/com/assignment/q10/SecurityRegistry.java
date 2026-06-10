package com.assignment.q10;

import org.springframework.stereotype.Component;

@Component
public class SecurityRegistry {
    public void register(String beanName) {
        System.out.println("Registered in security registry: " + beanName);
    }
}
