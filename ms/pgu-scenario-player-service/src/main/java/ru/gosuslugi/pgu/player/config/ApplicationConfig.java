package ru.gosuslugi.pgu.player.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;
import ru.gosuslugi.pgu.core.service.client.rest.RestServiceClientImpl;
import ru.gosuslugi.pgu.fs.common.helper.HelperScreenRegistry;
import ru.gosuslugi.pgu.fs.common.service.ComponentService;
import ru.gosuslugi.pgu.fs.common.service.ComputeAnswerService;
import ru.gosuslugi.pgu.fs.common.service.DisplayReferenceService;
import ru.gosuslugi.pgu.fs.common.service.FormServiceNsiClient;
import ru.gosuslugi.pgu.fs.common.service.JsonProcessingService;
import ru.gosuslugi.pgu.fs.common.service.ScenarioDtoService;
import ru.gosuslugi.pgu.fs.common.service.ScreenFinderService;
import ru.gosuslugi.pgu.fs.common.service.impl.ComputeDictionaryItemService;
import ru.gosuslugi.pgu.fs.common.service.impl.ScenarioDtoServiceImpl;

/**
 * Ручное создание бинов, которые не создаются через @Service
 */
@Configuration
public class ApplicationConfig {

    @Bean
    public ScenarioDtoService scenarioDtoService(ComponentService componentService, HelperScreenRegistry screenRegistry,
                                                 ScreenFinderService screenFinderService, JsonProcessingService jsonProcessingService,
                                                 DisplayReferenceService displayReferenceService, ComputeAnswerService computeAnswerService) {
        return new ScenarioDtoServiceImpl(componentService, screenRegistry, screenFinderService, jsonProcessingService, displayReferenceService, computeAnswerService);
    }

    @Bean
    public ComputeDictionaryItemService computeDictionaryItemService(RestTemplate restTemplate, @Value("${pgu.dictionary-url:empty}") String pguNsiUrl){
        RestServiceClientImpl restServiceClient = new RestServiceClientImpl();
        restServiceClient.setRestTemplate(restTemplate);
        FormServiceNsiClient nsiApiRestClient = new FormServiceNsiClient(restServiceClient, pguNsiUrl);
        return new ComputeDictionaryItemService(nsiApiRestClient);
    }
}
