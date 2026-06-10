package com.assignment.q6;

import org.springframework.beans.factory.ObjectFactory;
import org.springframework.stereotype.Service;

@Service
public class ReportManager {
    private final ObjectFactory<ReportTask> taskFactory;

    public ReportManager(ObjectFactory<ReportTask> taskFactory) {
        this.taskFactory = taskFactory;
    }

    public void generate(String payload) {
        ReportTask task = taskFactory.getObject();
        task.execute(payload);
    }
}
