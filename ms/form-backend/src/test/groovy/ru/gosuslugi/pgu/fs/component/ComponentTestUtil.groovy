package ru.gosuslugi.pgu.fs.component

import com.jayway.jsonpath.DocumentContext
import org.springframework.web.client.RestTemplate
import ru.gosuslugi.pgu.common.core.json.JsonProcessingUtil
import ru.gosuslugi.pgu.common.esia.search.dto.UserOrgData
import ru.gosuslugi.pgu.common.esia.search.dto.UserPersonalData
import ru.gosuslugi.pgu.components.descriptor.attr_factory.AttrsFactory
import ru.gosuslugi.pgu.dto.ApplicantAnswer
import ru.gosuslugi.pgu.dto.DisplayRequest
import ru.gosuslugi.pgu.dto.ScenarioDto
import ru.gosuslugi.pgu.dto.ServiceInfoDto
import ru.gosuslugi.pgu.dto.descriptor.FieldComponent
import ru.gosuslugi.pgu.dto.descriptor.LinkedValue
import ru.gosuslugi.pgu.fs.common.component.AbstractComponent
import ru.gosuslugi.pgu.fs.common.component.ComponentRegistry
import ru.gosuslugi.pgu.fs.common.descriptor.impl.MainDescriptorServiceImpl
import ru.gosuslugi.pgu.fs.common.jsonlogic.JsonLogic
import ru.gosuslugi.pgu.fs.common.jsonlogic.Parser
import ru.gosuslugi.pgu.fs.common.service.InitialValueFromService
import ru.gosuslugi.pgu.fs.common.service.JsonProcessingService
import ru.gosuslugi.pgu.fs.common.service.LinkedValuesService
import ru.gosuslugi.pgu.fs.common.service.ProtectedFieldService
import ru.gosuslugi.pgu.fs.common.service.condition.*
import ru.gosuslugi.pgu.fs.common.service.functions.impl.ContextFunctionServiceImpl
import ru.gosuslugi.pgu.fs.common.service.impl.*
import ru.gosuslugi.pgu.fs.common.variable.ServiceIdVariable
import ru.gosuslugi.pgu.fs.common.variable.VariableRegistry
import ru.gosuslugi.pgu.fs.pgu.client.impl.PguEmpowermentClientImpl
import ru.gosuslugi.pgu.fs.pgu.client.impl.PguEmpowermentClientImplV2
import ru.gosuslugi.pgu.fs.pgu.client.impl.PguUtilsClientImpl
import ru.gosuslugi.pgu.fs.service.impl.EmpowermentServiceImpl
import ru.gosuslugi.pgu.fs.service.impl.InitialValueFromImpl
import ru.gosuslugi.pgu.fs.service.impl.ProtectedFieldServiceImpl
import ru.gosuslugi.pgu.fs.utils.CalculatedAttributesHelper
import ru.gosuslugi.pgu.fs.utils.ParseAttrValuesHelper
import ru.gosuslugi.pgu.sd.storage.client.ServiceDescriptorClientImpl
import ru.gosuslugi.pgu.sd.storage.config.ServiceDescriptorClientProperties
import spock.lang.Specification
import spock.mock.DetachedMockFactory

class ComponentTestUtil extends Specification {

    private final static mockFactory = new DetachedMockFactory()

    @SuppressWarnings("GroovyAccessibility")
    static void setAbstractComponentServices(AbstractComponent component) {
        def jsonProcessingService = new JsonProcessingServiceImpl(JsonProcessingUtil.objectMapper)
        def linkedValuesService = new LinkedValuesService() {
            @Override
            void fillLinkedValues(FieldComponent fieldComponent, ScenarioDto scenarioDto, DocumentContext... externalContexts) {

            }

            @Override
            void fillLinkedValues(DisplayRequest displayRequest, ScenarioDto scenarioDto) {

            }

            @Override
            String getValue(LinkedValue linkedValue, ScenarioDto scenarioDto, AttrsFactory attrsFactory, DocumentContext... externalContexts) {
                return null
            }
        }
        component.componentReferenceService = new ComponentReferenceServiceImpl(jsonProcessingService, new UserCookiesServiceImpl(), linkedValuesService)
        component.jsonProcessingService = jsonProcessingService
        component.linkedValuesService = getLinkedValuesService(jsonProcessingService)
    }

    @SuppressWarnings("GroovyAccessibility")
    static void setAbstractComponentServices(AbstractComponent component, JsonProcessingService jsonProcessingService) {
        def linkedValuesService = new LinkedValuesService() {
            @Override
            void fillLinkedValues(FieldComponent fieldComponent, ScenarioDto scenarioDto, DocumentContext... externalContexts) {

            }

            @Override
            void fillLinkedValues(DisplayRequest displayRequest, ScenarioDto scenarioDto) {

            }

            @Override
            String getValue(LinkedValue linkedValue, ScenarioDto scenarioDto, AttrsFactory attrsFactory, DocumentContext... externalContexts) {
                return null
            }
        }
        component.componentReferenceService = new ComponentReferenceServiceImpl(jsonProcessingService, new UserCookiesServiceImpl(), linkedValuesService)
        component.jsonProcessingService = jsonProcessingService
        component.linkedValuesService = getLinkedValuesService(jsonProcessingService)
    }

    static void setAbstractComponentGenderServices(AbstractGenderComponent component, ComponentRegistry componentRegistry) {
        setAbstractComponentServices(component)
        component.componentRegistry = componentRegistry
    }

    @SuppressWarnings("GroovyAccessibility")
    static InitialValueFromService getInitialValueFromService() {
        def jsonProcessingService = new JsonProcessingServiceImpl(JsonProcessingUtil.objectMapper)

        def userPersonalData = new UserPersonalData()
        def userOrgData = new UserOrgData()
        def restTemplate = new RestTemplate()
        def empowermentService = new EmpowermentServiceImpl(
                userOrgData,
                userPersonalData,
                new PguEmpowermentClientImpl(restTemplate),
                new PguEmpowermentClientImplV2(restTemplate),
                new PguUtilsClientImpl(restTemplate, userPersonalData),
        )
        def variableRegistry = new VariableRegistry()
        def protectedFieldService = new ProtectedFieldServiceImpl(userPersonalData, userOrgData, empowermentService)
        def parseAttrValuesHelper = new ParseAttrValuesHelper(variableRegistry, jsonProcessingService, protectedFieldService)

        def serviceDescriptorClient = new ServiceDescriptorClientImpl(restTemplate, new ServiceDescriptorClientProperties())
        def mainDescriptorService = new MainDescriptorServiceImpl(serviceDescriptorClient)

        def conditionCheckerHelper = new ConditionCheckerHelper(
                new StringPredicateFactory(),
                new IntegerPredicateFactory(),
                new BooleanPredicateFactory(),
                new DatePredicateFactory(),
                new ArrayPredicateFactory(),
                new ApplicantAnswerPredicateFactory(),
                protectedFieldService,
                variableRegistry
        )
        def ruleConditionService = new RuleConditionServiceImpl(conditionCheckerHelper, protectedFieldService, variableRegistry)
        def serviceIdVariable = new ServiceIdVariable(mainDescriptorService, jsonProcessingService, ruleConditionService)

        def linkedValuesService = new LinkedValuesServiceImpl(jsonProcessingService, getJsonLogic(jsonProcessingService), new ContextFunctionServiceImpl(), variableRegistry, protectedFieldService, new DefinitionResolver())

        def componentReferenceService = new ComponentReferenceServiceImpl(jsonProcessingService, new UserCookiesServiceImpl(), linkedValuesService)
        def calculatedAttributesHelper = new CalculatedAttributesHelper(serviceIdVariable, parseAttrValuesHelper, componentReferenceService)

        return new InitialValueFromImpl(parseAttrValuesHelper, calculatedAttributesHelper)
    }

    static AbstractMap.SimpleEntry<String, ApplicantAnswer> answerEntry(String key, String value) {
        return new AbstractMap.SimpleEntry<String, ApplicantAnswer>(key, new ApplicantAnswer(value: value))
    }

    static LinkedValuesService getLinkedValuesService(jsonProcessingService) {
        return new LinkedValuesServiceImpl(
                jsonProcessingService,
                getJsonLogic(jsonProcessingService),
                new ContextFunctionServiceImpl(),
                new VariableRegistry(),
                new ProtectedFieldService() {
                    @Override
                    Object getValue(String name) {
                        return null
                    }
                },
                new DefinitionResolver()
        )
    }

    static JsonLogic getJsonLogic(jsonProcessingService) {
        def userPersonalData = new UserPersonalData()
        def userOrgData = new UserOrgData()
        def restTemplate = new RestTemplate()
        def empowermentService = new EmpowermentServiceImpl(
                userOrgData,
                userPersonalData,
                new PguEmpowermentClientImpl(restTemplate),
                new PguEmpowermentClientImplV2(restTemplate),
                new PguUtilsClientImpl(restTemplate, userPersonalData),
        )
        def protectedFieldService = new ProtectedFieldServiceImpl(userPersonalData, userOrgData, empowermentService)
        def variableRegistry = new VariableRegistry()
        def conditionCheckerHelper = new ConditionCheckerHelper(
                new StringPredicateFactory(),
                new IntegerPredicateFactory(),
                new BooleanPredicateFactory(),
                new DatePredicateFactory(),
                new ArrayPredicateFactory(),
                new ApplicantAnswerPredicateFactory(),
                protectedFieldService,
                variableRegistry
        )
        return new JsonLogic(new Parser(
                protectedFieldService,
                variableRegistry,
                jsonProcessingService,
                conditionCheckerHelper))
    }

    static ScenarioDto mockScenario(Map<String, ApplicantAnswer> currentValue,
                                    Map<String, ApplicantAnswer> applicantAnswers, ServiceInfoDto serviceInfoDto) {
        ScenarioDto scenarioDto = new ScenarioDto()
        scenarioDto.setCurrentValue(currentValue)
        scenarioDto.setApplicantAnswers(applicantAnswers)
        scenarioDto.setServiceInfo(serviceInfoDto)
        return scenarioDto
    }
}
