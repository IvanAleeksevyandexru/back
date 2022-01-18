package ru.gosuslugi.pgu.fs.config.properties;

import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;

import static org.springframework.util.StringUtils.hasText;

/**
 * PKCS7 settings class for pkcs7SignService
 */
@Data
@ConfigurationProperties(prefix = "esia")
public class EsiaServiceProperties {
    /**
     * Used as alias property in PKCS7SignService
     */
    @Value("${esia.keystore.alias}")
    private String keyStoreAlias;

    /**
     * Used as certPath property in PKCS7SignService
     */
    private String crt;

    /**
     * Used as keyStorePath property in PKCS7SignService
     */
    private String keystore;

    /**
     * Used as keyStorePass property in PKCS7SignService
     */
    @Value("${esia.keystore.passwd}")
    private String keystorePasswd;

    private String url;

    @Value("${esia.pd.proxy.url:#{null}}")
    private String proxyUrl;

    @Value("${esia.redirect.url}")
    private String redirectUrl;

    @Value("${esia.uddi.url}")
    private String uddiUrl;

    @Value("${esia.system.token.host}")
    private String systemTokenHost;

    @Value("${esia.personSearchStub.enabled:false}")
    private boolean personSearchStubEnabled;

    @Value("${esia.personSearchStub.sourceFilePath:#{null}}")
    private String personSearchStubFilePath;

    @Value("${esia.orgSearchStub.enabled:false}")
    private boolean orgSearchStubEnabled;

    @Value("${esia.orgSearchStub.sourceFilePath:#{null}}")
    private String orgSearchStubFilePath;

    public String getCalculatedUrl() {
        return hasText(proxyUrl) ? proxyUrl : url;
    }
}
