package com.assignment.q6;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Component
@Scope("prototype")
public class ReportTask {
    public void execute(String data) {
        System.out.println("Processing report data: " + data);
    }
}
