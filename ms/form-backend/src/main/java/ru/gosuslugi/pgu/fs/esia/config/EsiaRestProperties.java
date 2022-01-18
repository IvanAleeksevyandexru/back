package ru.gosuslugi.pgu.fs.esia.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties("esia-rest")
public class EsiaRestProperties {

    private String version;

    private String url;
}
