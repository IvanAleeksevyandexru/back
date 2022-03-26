package ru.gosuslugi.pgu.fs.config;

import org.codehaus.jackson.map.DeserializationConfig;
import org.mapstruct.factory.Mappers;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.*;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.remoting.jaxws.JaxWsPortProxyFactoryBean;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.annotation.RequestScope;
import org.uddi.v3_service.UDDIInquiryPortType;
import ru.atc.carcass.common.PKCS7SignService;
import ru.atc.carcass.common.spring.AppContextUtil;
import ru.atc.carcass.common.ws.JaxWSClientFactoryImpl;
import ru.atc.carcass.common.ws.JaxWsClientFactory;
import ru.atc.carcass.security.service.impl.EsiaRestClientServiceImpl;
import ru.atc.carcass.security.service.impl.ThreadLocalTokensContainerManagerService;
import ru.gosuslugi.pgu.common.core.interceptor.creator.RestTemplateCreator;
import ru.gosuslugi.pgu.common.core.service.HealthHolder;
import ru.gosuslugi.pgu.common.core.service.OkatoHolder;
import ru.gosuslugi.pgu.common.core.service.impl.HealthHolderImpl;
import ru.gosuslugi.pgu.common.esia.search.dto.UserOrgData;
import ru.gosuslugi.pgu.common.esia.search.dto.UserPersonalData;
import ru.gosuslugi.pgu.common.esia.search.service.OrgSearchService;
import ru.gosuslugi.pgu.common.esia.search.service.PersonSearchService;
import ru.gosuslugi.pgu.common.esia.search.service.UddiService;
import ru.gosuslugi.pgu.common.esia.search.service.impl.*;
import ru.gosuslugi.pgu.common.logging.service.SpanService;
import ru.gosuslugi.pgu.core.service.client.rest.RestServiceClientImpl;
import ru.gosuslugi.pgu.fs.config.properties.EsiaServiceProperties;
import ru.gosuslugi.pgu.pgu_common.nsi.service.NsiDadataService;
import ru.gosuslugi.pgu.pgu_common.nsi.service.NsiDictionaryService;
import ru.gosuslugi.pgu.pgu_common.nsi.service.impl.NsiDadataServiceImpl;
import ru.gosuslugi.pgu.pgu_common.nsi.service.impl.NsiDictionaryServiceImpl;
import ru.gosuslugi.pgu.pgu_common.payment.mapper.DictionaryResponseMapper;
import ru.gosuslugi.pgu.pgu_common.payment.service.BillingService;
import ru.gosuslugi.pgu.pgu_common.payment.service.PaymentService;
import ru.gosuslugi.pgu.pgu_common.payment.service.impl.BillingServiceImpl;
import ru.gosuslugi.pgu.pgu_common.payment.service.impl.PaymentServiceImpl;
import ru.gosuslugi.pgu.common.sop.service.SopDictionaryService;
import ru.gosuslugi.pgu.common.sop.service.impl.SopDictionaryServiceImpl;

import java.net.MalformedURLException;
import java.net.URL;

/**
 * Pgu integration bean configuration
 * possible should be moved to separate module (in case if there will be backend nsi integration module)
 */
@Configuration
@EnableConfigurationProperties(EsiaServiceProperties.class)
@ComponentScan(basePackages = {"ru.gosuslugi.pgu.pgu_common.gibdd.service.impl", "ru.gosuslugi.pgu.pgu_common.gibdd.mapper",
        "ru.gosuslugi.pgu.common.eaisdo.service", "ru.gosuslugi.pgu.common.certificate.service"})
public class PguLibConfig {

    @Bean
    public PKCS7SignService pkcs7SignService(EsiaServiceProperties properties) {
        return new PKCS7SignService(properties.getKeystorePasswd(), properties.getKeyStoreAlias(), properties.getKeystore(), properties.getCrt());
    }

    @Bean
    public AppContextUtil appContextUtil(ApplicationContext context) {
        var util = new AppContextUtil();
        util.setApplicationContext(context);
        return util;
    }

    @Bean
    public RestServiceClientImpl restServiceClient(AppContextUtil util, JaxWsClientFactory factory, RestTemplate restTemplate) {
        org.codehaus.jackson.map.ObjectMapper mapper = new org.codehaus.jackson.map.ObjectMapper();
        mapper.configure(DeserializationConfig.Feature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        mapper.configure(DeserializationConfig.Feature.ACCEPT_SINGLE_VALUE_AS_ARRAY, true);

        RestServiceClientImpl restServiceClient = new RestServiceClientImpl();
        restServiceClient.setRestTemplate(restTemplate);
        restServiceClient.setJaxWsClientFactory(factory);
        restServiceClient.setObjectMapper(mapper);

        return restServiceClient;
    }

    @Bean
    public JaxWsPortProxyFactoryBean jaxWsPortProxyFactoryBean(EsiaServiceProperties properties) throws MalformedURLException {
        JaxWsPortProxyFactoryBean proxyFactoryBean = new JaxWsPortProxyFactoryBean();
        proxyFactoryBean.setServiceInterface(org.uddi.v3_service.UDDIInquiryPortType.class);
        proxyFactoryBean.setServiceName("UDDIInquiryService");
        proxyFactoryBean.setLookupServiceOnStartup(false);

        proxyFactoryBean.setWsdlDocumentUrl(
                new URL(properties.getUddiUrl()));
        return proxyFactoryBean;
    }


    @Bean
    public JaxWSClientFactoryImpl jaxWsClientFactory(AppContextUtil appContextUtil, JaxWsPortProxyFactoryBean uddi, ConfigurableEnvironment env) {
        RestTemplateCreator.copyApplicationPropertiesToSystem(env, "uddi");
        JaxWSClientFactoryImpl jaxWSClientFactory = new JaxWSClientFactoryImpl();
        CacheImpl cache = new CacheImpl();
        jaxWSClientFactory.setCacheControlService(cache);
        return jaxWSClientFactory;
    }

    @Bean
    public UddiService uddiService(JaxWSClientFactoryImpl jaxWsClientFactory, SpanService spanService) {
        return new UddiService(jaxWsClientFactory, spanService);
    }

    @Bean
    public ThreadLocalTokensContainerManagerService threadLocalTokensContainerManagerService() {
        ThreadLocalTokensContainerManagerService threadLocalTokensContainerManagerService = new ThreadLocalTokensContainerManagerService();
        return threadLocalTokensContainerManagerService;
    }

    @Bean
    public EsiaRestClientServiceImpl esiaRestClientService(EsiaServiceProperties properties, ConfigurableEnvironment env) {
        RestTemplateCreator.copyApplicationPropertiesToSystem(env, "esia-pd");
        EsiaRestClientServiceImpl esiaRestClientService = new EsiaRestClientServiceImpl();
        esiaRestClientService.setEsiaUrl(properties.getUrl());
        esiaRestClientService.setProxyUrl(properties.getProxyUrl());
        esiaRestClientService.setRedirectHost(properties.getRedirectUrl());
        esiaRestClientService.setTokensContainerManagerService(threadLocalTokensContainerManagerService());
        return esiaRestClientService;
    }

    @Bean
    public NsiDadataService nsiDadataService(RestTemplate restTemplate, OkatoHolder okatoHolder) {
        return new NsiDadataServiceImpl(restTemplate, healthHolder(), okatoHolder);
    }

    @Bean
    @Scope(value = WebApplicationContext.SCOPE_REQUEST, proxyMode = ScopedProxyMode.INTERFACES)
    public HealthHolder healthHolder() {
        return new HealthHolderImpl();
    }

    @Bean
    @ConditionalOnProperty(value = "esia.personSearchStub.enabled", havingValue = "false", matchIfMissing = true)
    public PersonSearchService personSearchService(RestTemplate restTemplate, UddiService uddiService) {
        return new PersonSearchServiceImpl(restTemplate, uddiService);
    }

    @Bean
    @ConditionalOnProperty(value = "esia.personSearchStub.enabled", havingValue = "true")
    public PersonSearchService personSearchServiceStub(EsiaServiceProperties properties) {
        return new PersonSearchServiceStub(properties.getPersonSearchStubFilePath());
    }

    @Bean
    @ConditionalOnProperty(value = "esia.orgSearchStub.enabled", havingValue = "false", matchIfMissing = true)
    public OrgSearchService orgSearchService(RestTemplate restTemplate, UddiService uddiService) {
        return new OrgSearchServiceImpl(restTemplate, uddiService);
    }

    @Bean
    @ConditionalOnProperty(value = "esia.orgSearchStub.enabled", havingValue = "true")
    public OrgSearchService orgSearchServiceStub(EsiaServiceProperties properties) {
        return new OrgSearchServiceStub(properties.getOrgSearchStubFilePath());
    }

    @Bean
    public NsiDictionaryService nsiDictionaryService(RestTemplate restTemplate, OkatoHolder okatoHolder) {
        return new NsiDictionaryServiceImpl(restTemplate, healthHolder(), okatoHolder);
    }

    @Bean
    public SopDictionaryService sopDictionaryService(RestTemplate restTemplate) {
        return new SopDictionaryServiceImpl(restTemplate, healthHolder());
    }

    @Bean
    public PaymentService paymentService(RestTemplate restTemplate) {
        return new PaymentServiceImpl(restTemplate);
    }

    @Bean
    public BillingService billingService(RestTemplate restTemplate) {
        DictionaryResponseMapper dictionaryResponseMapper = Mappers.getMapper(DictionaryResponseMapper.class);
        return new BillingServiceImpl(restTemplate, dictionaryResponseMapper);
    }

    @Bean
    @RequestScope
    UserPersonalData userPersonalData() {
        return new UserPersonalData();
    }

    @Bean
    @RequestScope
    UserOrgData userOrgData() {
        return new UserOrgData();
    }

}
