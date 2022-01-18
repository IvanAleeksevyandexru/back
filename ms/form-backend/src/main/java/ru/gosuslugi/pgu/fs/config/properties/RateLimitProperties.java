package ru.gosuslugi.pgu.fs.config.properties;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
@RequiredArgsConstructor
@ConfigurationProperties(prefix = "ratelimit")
public class RateLimitProperties {

    private String pguUrl;

    private Long ttl;

    private Long limit;
}
