package ru.gosuslugi.pgu.player.config.properties;

import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;

import static org.springframework.util.StringUtils.hasText;

@Data
@ConfigurationProperties(prefix = "esia")
public class EsiaServiceProperties {
    @Value("${esia.keystore.alias}")
    private String keyStoreAlias;
    private String crt;
    private String keystore;
    @Value("${esia.keystore.passwd}")
    private String keystorePasswd;

    private String url;
    @Value("${esia.pd.proxy.url:#{null}}")
    private String proxyUrl;
    @Value("${esia.redirect.url}")
    private String redirectUrl;

    public String getCalculatedUrl() {
        return hasText(proxyUrl) ? proxyUrl : url;
    }
}
