package ru.gosuslugi.pgu.fs.config.actuator;

import com.teketik.spring.health.AsyncHealth;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.stereotype.Component;

/**
 * Запускает проверку HealthCheck на отдельном пуле, чтобы в случае исчерпания пула томката kuber не прибивал под.
 */
@AsyncHealth
@Component
public class AsyncHealthCheck implements HealthIndicator {

    @Override
    public Health health() { //will be executed on a separate thread pool
        return Health.up().build();
    }
}
