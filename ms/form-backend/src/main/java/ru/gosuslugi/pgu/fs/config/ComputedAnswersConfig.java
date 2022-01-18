package ru.gosuslugi.pgu.fs.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;
import ru.gosuslugi.pgu.core.service.client.rest.RestServiceClientImpl;
import ru.gosuslugi.pgu.fs.common.service.FormServiceNsiClient;
import ru.gosuslugi.pgu.fs.common.service.impl.ComputeDictionaryItemService;

@Configuration
public class ComputedAnswersConfig {

    @Bean
    public ComputeDictionaryItemService computeDictionaryItemService(RestTemplate restTemplate, @Value("${pgu.dictionary-url:empty}") String pguNsiUrl){
        RestServiceClientImpl restServiceClient = new RestServiceClientImpl();
        restServiceClient.setRestTemplate(restTemplate);
        FormServiceNsiClient nsiApiRestClient = new FormServiceNsiClient(restServiceClient, pguNsiUrl);
        return new ComputeDictionaryItemService(nsiApiRestClient);
    }

}
