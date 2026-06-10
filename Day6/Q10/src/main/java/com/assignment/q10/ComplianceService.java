package com.assignment.q10;

import org.springframework.stereotype.Component;

@Component
public class ComplianceService implements Auditable {
    public void audit() {
        System.out.println("Sensitive audit service active");
    }
}
