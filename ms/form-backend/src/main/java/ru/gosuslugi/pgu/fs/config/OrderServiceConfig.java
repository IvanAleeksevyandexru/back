package ru.gosuslugi.pgu.fs.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;
import org.springframework.web.client.RestTemplate;
import ru.gosuslugi.pgu.common.esia.search.dto.UserOrgData;
import ru.gosuslugi.pgu.common.esia.search.dto.UserPersonalData;
import ru.gosuslugi.pgu.fs.common.service.UserCookiesService;
import ru.gosuslugi.pgu.fs.config.cache.RequestCacheResolver;
import ru.gosuslugi.pgu.fs.pgu.client.PguClient;
import ru.gosuslugi.pgu.fs.pgu.mapper.HighLoadOrderPguMapper;
import ru.gosuslugi.pgu.fs.pgu.service.CatalogService;
import ru.gosuslugi.pgu.fs.pgu.service.PguOrderService;
import ru.gosuslugi.pgu.fs.pgu.service.impl.CatalogServiceImpl;
import ru.gosuslugi.pgu.fs.pgu.service.impl.CatalogServiceStub;
import ru.gosuslugi.pgu.fs.pgu.service.impl.PguOrderServiceImpl;
import ru.gosuslugi.pgu.fs.pgu.service.impl.PguOrderServiceStub;
import ru.gosuslugi.pgu.fs.service.EmpowermentService;

import static org.springframework.context.annotation.ScopedProxyMode.TARGET_CLASS;
import static org.springframework.web.context.WebApplicationContext.SCOPE_REQUEST;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class OrderServiceConfig {

    @Value("${pgu.order-url}")
    private String pguUrl;

    private final RestTemplate restTemplate;

    private final UserPersonalData userPersonalData;
    private final UserOrgData userOrgData;
    private final PguClient pguClient;
    private final UserCookiesService userCookiesService;
    private final EmpowermentService empowermentService;
    private final HighLoadOrderPguMapper highLoadOrderPguMapper;

    @Bean
    @ConditionalOnProperty(value = "orderid.integration", matchIfMissing = true, havingValue = "true")
    public PguOrderService pguOrderService() {
        return new PguOrderServiceImpl(userPersonalData, userOrgData, pguClient, userCookiesService, empowermentService, highLoadOrderPguMapper);
    }

    @Bean
    @ConditionalOnProperty(value = "orderid.integration", havingValue = "false")
    public PguOrderService pguOrderServiceStub() {
        return new PguOrderServiceStub();
    }

    @Bean
    @Scope(value = SCOPE_REQUEST, proxyMode = TARGET_CLASS)
    public RequestCacheResolver requestCacheResolver() {
        return new RequestCacheResolver();
    }

    @Bean
    @ConditionalOnProperty(value = "orderid.integration", matchIfMissing = true, havingValue = "true")
    public CatalogService catalogService() {
        return new CatalogServiceImpl(restTemplate);
    }

    @Bean
    @ConditionalOnProperty(value = "orderid.integration", havingValue = "false")
    public CatalogService catalogServiceStub() {
        return new CatalogServiceStub();
    }
}
