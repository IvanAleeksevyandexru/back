package ru.gosuslugi.pgu.fs.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;
import ru.gosuslugi.pgu.fs.common.exception.dto.ErrorModalDescriptor;
import ru.gosuslugi.pgu.components.descriptor.SubServiceDescriptor;
import ru.gosuslugi.pgu.dto.descriptor.ServiceDescriptor;
import ru.gosuslugi.pgu.fs.common.service.JsonProcessingService;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Configuration
public class LoadResourceConfig {

    @Value("classpath:json/internal/profile-phone-update.json")
    private Resource phoneChangeResource;

    @Value("classpath:json/internal/profile-email-update.json")
    private Resource emailChangeResource;

    @Value("classpath:json/internal/profile-legal-email-update.json")
    private Resource legalEmailChangeResource;

    @Value("classpath:json/internal/profile-address-update_actual-residence.json")
    private Resource addressChangeActualResidenceResource;

    @Value("classpath:json/internal/profile-address-update_permanent-registry.json")
    private Resource addressChangePermanentRegistryResource;

    @Value("classpath:json/internal/profile-oms-update.json")
    private Resource omsUpdateEdiuserpolicy;

    @Value("classpath:json/service-305/setBillToOrder.json")
    private Resource setBillToOrderResource;

    @Value("classpath:json/service-305/generateScenario.json")
    private Resource generateScenarioResource;

    @Value("classpath:json/modals/error.json")
    private Resource errorModalsResource;

    @Value("classpath:json/modals/warning.json")
    private Resource warningModalsResource;

    @Value("classpath:json/modals/stop.json")
    private Resource stopModalsResource;

    @Value("classpath:json/internal/sms-confirmation-code.json")
    private Resource smsConfirmationCodeResource;

    @Value("classpath:json/internal/email-confirmation-code.json")
    private Resource emailConfirmationCodeResource;

    @Value("classpath:json/internal/criticalErrorScreen.json")
    private Resource criticalErrorScreen;

    @Bean
    public List<ServiceDescriptor> internalDescriptors(JsonProcessingService jsonProcessingService){
        return Stream.of(
                phoneChangeResource,
                emailChangeResource,
                legalEmailChangeResource,
                addressChangeActualResidenceResource,
                addressChangePermanentRegistryResource,
                omsUpdateEdiuserpolicy,
                smsConfirmationCodeResource,
                emailConfirmationCodeResource,
                criticalErrorScreen
            ).map(json -> jsonProcessingService.fromResource(json, SubServiceDescriptor.class)).collect(Collectors.toList());
    }

    @Bean
    public List<ServiceDescriptor> errorDescriptors(JsonProcessingService jsonProcessingService){

        return Stream.of(
                setBillToOrderResource,
                generateScenarioResource
            ).map(json -> jsonProcessingService.fromResource(json, SubServiceDescriptor.class)).collect(Collectors.toList());
    }

    @Bean
    public List<ErrorModalDescriptor> errorModals(JsonProcessingService jsonProcessingService) {
        return Stream.of(
                errorModalsResource,
                warningModalsResource,
                stopModalsResource
                ).map(json -> jsonProcessingService.fromResource(json, ErrorModalDescriptor.class)).collect(Collectors.toList());
    }
}
