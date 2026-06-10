package com.assignment.q2;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Component
@Scope("prototype")
public class PatientContext {
    private final long createdAt = System.nanoTime();

    public long getCreatedAt() {
        return createdAt;
    }
}
