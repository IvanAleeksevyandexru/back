package ru.gosuslugi.pgu.fs.common.variable

import ru.gosuslugi.pgu.dto.ScenarioDto
import ru.gosuslugi.pgu.fs.common.exception.ServiceIdException
import ru.gosuslugi.pgu.common.core.json.JsonProcessingUtil
import ru.gosuslugi.pgu.dto.descriptor.ServiceDescriptor
import ru.gosuslugi.pgu.fs.common.descriptor.MainDescriptorService
import ru.gosuslugi.pgu.fs.common.service.JsonProcessingService
import ru.gosuslugi.pgu.fs.common.service.RuleConditionService
import ru.gosuslugi.pgu.fs.common.service.impl.JsonProcessingServiceImpl
import spock.lang.Shared
import spock.lang.Specification
import ru.gosuslugi.pgu.common.core.json.JsonFileUtil

class ServiceIdVariableTest extends Specification {

    MainDescriptorService mainDescriptorService = Stub()
    @Shared
    JsonProcessingService jsonProcessingService = new JsonProcessingServiceImpl(JsonProcessingUtil.getObjectMapper())
    RuleConditionService ruleConditionService = Stub()

    ServiceIdVariable serviceIdVariable = new ServiceIdVariable(
            mainDescriptorService, jsonProcessingService, ruleConditionService)

    @Shared
    ServiceDescriptor serviceDescriptorWithoutServiceIds
    @Shared
    ServiceDescriptor serviceDescriptorWithServiceIds
    @Shared
    ScenarioDto scenarioDto


    def setupSpec() {
        serviceDescriptorWithoutServiceIds = jsonProcessingService.fromJson(
                JsonFileUtil.getJsonFromFile(this.getClass(),"-descriptorWithoutServiceIdDefinition.json"),
                ServiceDescriptor.class)

        serviceDescriptorWithServiceIds = jsonProcessingService.fromJson(
                JsonFileUtil.getJsonFromFile(this.getClass(),"-descriptorWithServiceIdDefinition.json"),
                ServiceDescriptor.class)

        scenarioDto = jsonProcessingService.fromJson(
                JsonFileUtil.getJsonFromFile(this.getClass(),"-scenarioDto.json"),
                ScenarioDto.class)
    }

    def "getType should return correct variable type"() {
        when:
        VariableType type = serviceIdVariable.getType()
        then:
        type == VariableType.serviceId;
    }

    def "getValue should return serviceId from scenarioDto when there is no serviceIds definition in service descriptor"() {
        given:
        mainDescriptorService.getServiceDescriptor(_ as String) >> serviceDescriptorWithoutServiceIds
        when:
        String serviceId = serviceIdVariable.getValue(scenarioDto)
        then:
        serviceId == scenarioDto.getServiceCode()
    }

    def "getValue should return default ServiceId when no conditions met"() {
        given:
        mainDescriptorService.getServiceDescriptor(_ as String) >> serviceDescriptorWithServiceIds
        when:
        String serviceId = serviceIdVariable.getValue(scenarioDto)
        then:
        serviceId != scenarioDto.getServiceCode()
        serviceId == serviceDescriptorWithServiceIds.getServiceIds().getDefaultServiceId()
    }

    def "getValue should return ServiceId for met conditions"() {
        given:
        mainDescriptorService.getServiceDescriptor(_ as String) >> serviceDescriptorWithServiceIds
        ruleConditionService.isRuleApplyToAnswers(_ as Set, _ as List, _ as List, _ as ScenarioDto) >>> [false, false]
        when:
        String serviceId = serviceIdVariable.getValue(scenarioDto)
        then:
        serviceId != scenarioDto.getServiceCode()
        serviceId == serviceDescriptorWithServiceIds.getServiceIds().getDefaultServiceId()
    }



    def "getValue should return first serviceId based on met conditions"() {
        given:
        mainDescriptorService.getServiceDescriptor(_ as String) >> serviceDescriptorWithServiceIds
        ruleConditionService.isRuleApplyToAnswers(_ as Set, _ as List, _ as List, _ as ScenarioDto) >>> [true, false]
        when:
        String serviceId = serviceIdVariable.getValue(scenarioDto)
        then:
        serviceId != scenarioDto.getServiceCode()
        serviceId == serviceDescriptorWithServiceIds.getServiceIds().getIds().get(0).getId()
    }

    def "getValue should return second serviceId based on met conditions"() {
        given:
        mainDescriptorService.getServiceDescriptor(_ as String) >> serviceDescriptorWithServiceIds
        ruleConditionService.isRuleApplyToAnswers(_ as Set, _ as List, _ as List, _ as ScenarioDto) >>> [false, true]
        when:
        String serviceId = serviceIdVariable.getValue(scenarioDto)
        then:
        serviceId != scenarioDto.getServiceCode()
        serviceId == serviceDescriptorWithServiceIds.getServiceIds().getIds().get(1).getId()
    }

    def "getValue should throw exception"() {
        given:
        mainDescriptorService.getServiceDescriptor(_ as String) >> serviceDescriptorWithServiceIds
        ruleConditionService.isRuleApplyToAnswers(_ as Set, _ as List, _ as List, _ as ScenarioDto) >>> [true, true]
        when:
        serviceIdVariable.getValue(scenarioDto)
        then:
        thrown ServiceIdException
    }


}
