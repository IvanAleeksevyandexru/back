package ru.gosuslugi.pgu.fs.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import ru.gosuslugi.pgu.fs.config.decorator.ContextCopyingDecorator;

import java.util.concurrent.Executor;

@Configuration
public class GibddConfig {

    @Value("${gibdd.execution.pool.core-size:1}")
    private Integer corePoolSize;

    @Value("${gibdd.execution.pool.max-size:1}")
    private Integer maxPoolSize;

    @Value("${gibdd.execution.pool.queue-capacity:10}")
    private Integer queueCapacity;

    @Value("${gibdd.execution.thread-name-prefix:Gibdd-}")
    private String threadNamePrefix;

    @Bean
    public Executor gibddExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(corePoolSize);
        executor.setMaxPoolSize(maxPoolSize);
        executor.setQueueCapacity(queueCapacity);
        executor.setThreadNamePrefix(threadNamePrefix);
        executor.setTaskDecorator(new ContextCopyingDecorator());
        executor.initialize();
        return executor;
    }
}