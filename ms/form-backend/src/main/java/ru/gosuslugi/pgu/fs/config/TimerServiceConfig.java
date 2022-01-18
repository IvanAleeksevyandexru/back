package ru.gosuslugi.pgu.fs.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;
import ru.gosuslugi.pgu.fs.service.TimerClient;
import ru.gosuslugi.pgu.fs.service.timer.impl.TimerClientImpl;
import ru.gosuslugi.pgu.fs.service.timer.impl.TimerClientStub;

@Configuration
public class TimerServiceConfig {

    @Bean
    @ConditionalOnProperty(value = "timer-service.integration", havingValue = "true", matchIfMissing = true)
    public TimerClient timerClient(RestTemplate restTemplate, @Value("${pgu.timer-url:empty}") String timerUrl) {
        return new TimerClientImpl(restTemplate, timerUrl);
    }

    @Bean
    @ConditionalOnProperty(value = "timer-service.integration", havingValue = "false")
    public TimerClient timerClientStub() {
        return new TimerClientStub();
    }

}
