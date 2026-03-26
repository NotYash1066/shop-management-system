package com.shopmanagement.config;

import java.util.concurrent.Executor;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

@Configuration
public class AsyncConfig {

    @Bean(name = "stockReconciliationExecutor")
    public Executor stockReconciliationExecutor(
            @Value("${app.async.stock-reconciliation.core-pool-size:8}") int corePoolSize,
            @Value("${app.async.stock-reconciliation.max-pool-size:32}") int maxPoolSize,
            @Value("${app.async.stock-reconciliation.queue-capacity:500}") int queueCapacity) {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(corePoolSize);
        executor.setMaxPoolSize(maxPoolSize);
        executor.setQueueCapacity(queueCapacity);
        executor.setThreadNamePrefix("stock-reconciliation-");
        executor.initialize();
        return executor;
    }
}
