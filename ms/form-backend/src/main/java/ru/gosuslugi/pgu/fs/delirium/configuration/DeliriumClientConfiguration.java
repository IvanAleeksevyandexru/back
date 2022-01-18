package ru.gosuslugi.pgu.fs.delirium.configuration;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@Data
@ConfigurationProperties("delirium.client")
public class DeliriumClientConfiguration {

    private String url;

}
