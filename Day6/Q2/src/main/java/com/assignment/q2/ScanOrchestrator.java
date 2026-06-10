package com.assignment.q2;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

@Component
public class ScanOrchestrator {
    private final ImageRenderingEngine imageRenderingEngine;
    private final ObjectProvider<PatientContext> patientContextProvider;

    public ScanOrchestrator(@Lazy ImageRenderingEngine imageRenderingEngine,
                            ObjectProvider<PatientContext> patientContextProvider) {
        this.imageRenderingEngine = imageRenderingEngine;
        this.patientContextProvider = patientContextProvider;
    }

    public void processMriScan(String scanId) {
        PatientContext context = patientContextProvider.getObject();
        imageRenderingEngine.renderImage(scanId);
        System.out.println("Using fresh patient context: " + context.getCreatedAt());
    }
}
