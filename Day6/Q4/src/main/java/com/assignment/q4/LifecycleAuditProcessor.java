package com.assignment.q4;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.stereotype.Component;

@Component
public class LifecycleAuditProcessor implements BeanPostProcessor {
    private final ObjectProvider<SecurityRegistry> securityRegistryProvider;

    public LifecycleAuditProcessor(ObjectProvider<SecurityRegistry> securityRegistryProvider) {
        this.securityRegistryProvider = securityRegistryProvider;
    }

    @Override
    public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
        return bean;
    }

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        if (bean instanceof PIIProcessor) {
            securityRegistryProvider.ifAvailable(registry -> registry.register(beanName));
        }
        return bean;
    }
}
