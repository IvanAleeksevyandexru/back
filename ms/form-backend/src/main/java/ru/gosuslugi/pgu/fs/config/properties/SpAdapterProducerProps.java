package ru.gosuslugi.pgu.fs.config.properties;

import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import ru.gosuslugi.pgu.common.kafka.properties.AbstractProducerProps;

@Data
@Configuration
@RequiredArgsConstructor
@ConfigurationProperties(prefix = "spring.kafka.producer.sp-adapter")
public class SpAdapterProducerProps extends AbstractProducerProps {
}
