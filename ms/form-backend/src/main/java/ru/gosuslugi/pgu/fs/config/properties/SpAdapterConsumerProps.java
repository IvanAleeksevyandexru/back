package ru.gosuslugi.pgu.fs.config.properties;

import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import ru.gosuslugi.pgu.common.kafka.properties.AbstractConsumerProps;

@Data
@Configuration
@RequiredArgsConstructor
@ConfigurationProperties(prefix = "spring.kafka.consumer.sp-adapter")
public class SpAdapterConsumerProps extends AbstractConsumerProps {

    private String responseTopicName;

    private String errorTopicName;

}
