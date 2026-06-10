package com.assignment.q5;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.SmartInitializingSingleton;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Component
public class WebhookDispatcher implements ApplicationContextAware, SmartInitializingSingleton {
    private ApplicationContext applicationContext;
    private final List<WebhookIntegration> cachedIntegrations = new ArrayList<>();

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    @Override
    public void afterSingletonsInstantiated() {
        Map<String, WebhookIntegration> integrations = applicationContext.getBeansOfType(WebhookIntegration.class);
        cachedIntegrations.clear();
        cachedIntegrations.addAll(integrations.values());
        System.out.println("Webhook routing table ready with " + cachedIntegrations.size() + " integrations");
    }

    public void dispatch(String payload) {
        for (WebhookIntegration integration : cachedIntegrations) {
            integration.send(payload);
        }
    }
}
