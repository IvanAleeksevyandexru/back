package ru.gosuslugi.pgu.player.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import ru.atc.carcass.common.PKCS7SignService;
import ru.atc.carcass.security.service.impl.EsiaRestClientServiceImpl;
import ru.atc.carcass.security.service.impl.ThreadLocalTokensContainerManagerService;
import ru.gosuslugi.pgu.common.esia.search.dto.UserPersonalData;
import ru.gosuslugi.pgu.player.config.properties.EsiaServiceProperties;

@Configuration
@EnableConfigurationProperties(EsiaServiceProperties.class)
public class PguLibConfig {

    @Bean
    public EsiaRestClientServiceImpl esiaRestClientService(EsiaServiceProperties properties) {
        EsiaRestClientServiceImpl esiaRestClientService = new EsiaRestClientServiceImpl();
        esiaRestClientService.setPkcs7SignService(pkcs7SignService(properties));
        esiaRestClientService.setEsiaUrl(properties.getUrl());
        esiaRestClientService.setProxyUrl(properties.getProxyUrl());
        esiaRestClientService.setRedirectHost(properties.getRedirectUrl());
        esiaRestClientService.setTokensContainerManagerService(threadLocalTokensContainerManagerService());
        return esiaRestClientService;
    }


    @Bean
    public ThreadLocalTokensContainerManagerService threadLocalTokensContainerManagerService() {
        ThreadLocalTokensContainerManagerService threadLocalTokensContainerManagerService = new ThreadLocalTokensContainerManagerService();
        return threadLocalTokensContainerManagerService;
    }

    @Bean
    public PKCS7SignService pkcs7SignService(EsiaServiceProperties properties) {
        return new PKCS7SignService(properties.getKeystorePasswd(), properties.getKeyStoreAlias(), properties.getKeystore(), properties.getCrt());
    }

}
