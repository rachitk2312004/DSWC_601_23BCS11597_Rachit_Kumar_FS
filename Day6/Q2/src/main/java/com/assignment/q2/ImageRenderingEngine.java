package com.assignment.q2;

import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

@Component
@Lazy
public class ImageRenderingEngine {
    public void renderImage(String scanId) {
        System.out.println("Rendering image for scan " + scanId);
    }
}
