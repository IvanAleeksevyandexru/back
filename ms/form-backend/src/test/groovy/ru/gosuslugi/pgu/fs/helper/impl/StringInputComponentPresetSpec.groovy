package ru.gosuslugi.pgu.fs.helper.impl

import com.fasterxml.jackson.databind.ObjectMapper
import ru.gosuslugi.pgu.common.core.json.JsonProcessingUtil
import ru.gosuslugi.pgu.common.esia.search.dto.UserOrgData
import ru.gosuslugi.pgu.common.esia.search.dto.UserPersonalData
import ru.gosuslugi.pgu.draft.model.DraftHolderDto
import ru.gosuslugi.pgu.dto.ScenarioDto
import ru.gosuslugi.pgu.dto.descriptor.FieldComponent
import ru.gosuslugi.pgu.dto.descriptor.ServiceDescriptor
import ru.gosuslugi.pgu.fs.common.component.ComponentResponse
import ru.gosuslugi.pgu.fs.common.component.input.StringInputComponent
import ru.gosuslugi.pgu.fs.common.service.EvaluationExpressionService
import ru.gosuslugi.pgu.fs.common.service.JsonProcessingService
import ru.gosuslugi.pgu.fs.common.service.ProtectedFieldService
import ru.gosuslugi.pgu.fs.common.service.condition.*
import ru.gosuslugi.pgu.fs.common.service.impl.EvaluationExpressionServiceImpl
import ru.gosuslugi.pgu.fs.common.service.impl.JsonProcessingServiceImpl
import ru.gosuslugi.pgu.fs.common.variable.ServiceIdVariable
import ru.gosuslugi.pgu.fs.common.variable.VariableRegistry
import ru.gosuslugi.pgu.fs.service.EmpowermentService
import ru.gosuslugi.pgu.fs.service.impl.InitialValueFromImpl
import ru.gosuslugi.pgu.fs.service.impl.ProtectedFieldServiceImpl
import ru.gosuslugi.pgu.fs.utils.CalculatedAttributesHelper
import ru.gosuslugi.pgu.fs.utils.ParseAttrValuesHelper
import spock.lang.Shared
import spock.lang.Specification

class StringInputComponentPresetSpec extends Specification {

    @Shared
    ObjectMapper objectMapper

    @Shared
    JsonProcessingService jsonProcessingService

    @Shared
    StringInputComponent stringInputComponent

    ScenarioDto scenarioDto = getResource("ru/gosuslugi/pgu/fs/helper/impl/StringInputComponentPresetTest-draft.json", DraftHolderDto.class).getBody()
    ServiceDescriptor serviceDescriptor = getResource("ru/gosuslugi/pgu/fs/helper/impl/StringInputComponentPresetTest-ServerDescriptor.json", ServiceDescriptor.class)

    def setupSpec() {
        objectMapper = JsonProcessingUtil.getObjectMapper()
        jsonProcessingService = new JsonProcessingServiceImpl(objectMapper)

        ServiceIdVariable serviceIdVariable = Stub(ServiceIdVariable)
        VariableRegistry variableRegistry = new VariableRegistry()
        EmpowermentService empowermentServiceMock = Mock(EmpowermentService)
        ProtectedFieldService protectedFieldService = new ProtectedFieldServiceImpl(new UserPersonalData(), new UserOrgData(), empowermentServiceMock)
        ParseAttrValuesHelper parseAttrValuesHelper = new ParseAttrValuesHelper(variableRegistry, jsonProcessingService, null)
        CalculatedAttributesHelper calculatedAttributesHelper = new CalculatedAttributesHelper(serviceIdVariable, parseAttrValuesHelper, null)
        calculatedAttributesHelper.postConstruct()
        InitialValueFromImpl initialValueFromService = new InitialValueFromImpl(parseAttrValuesHelper, calculatedAttributesHelper)
        ConditionCheckerHelper conditionCheckerHelper = new ConditionCheckerHelper(
                new StringPredicateFactory(),
                new IntegerPredicateFactory(),
                new BooleanPredicateFactory(),
                new DatePredicateFactory(),
                new ArrayPredicateFactory(),
                new ApplicantAnswerPredicateFactory(),
                protectedFieldService,
                variableRegistry
        )
        EvaluationExpressionService evaluationExpressionService = new EvaluationExpressionServiceImpl();

        stringInputComponent = new StringInputComponent(initialValueFromService, conditionCheckerHelper, evaluationExpressionService)
    }

    def "testREF"() {
        given:
        FieldComponent component = jsonProcessingService.fromJson(
                "{\"id\": \"add1\", \"type\": \"StringInput\", \"required\": true, \"label\": \"Идентификационный номер (VIN)\", \"attrs\": {\"preset_from\": {\"type\": \"REF\",\"value\": \"sn1a.snils\"}, \"ref\":[{\"relatedRel\":\"add3\",\"val\":true,\"relation\":\"disabled\"}],\"validation\":[{\"type\":\"RegExp\",\"value\":\"^[A-Z0-9]*\$\",\"errorMsg\":\"Поле может содержать только цифры и заглавные латинские буквы\"}, {\"type\":\"RegExp\",\"value\":\"^[^QIO]*\$\",\"errorMsg\":\"Не используются буквы Q, I, O\"}, {\"type\":\"RegExp\",\"value\":\"^(|.{17})\$\",\"errorMsg\":\"Длина поля 17 символов\"}, {\"type\":\"RegExp\",\"value\":\"^(|.{13}[0-9]{4})\$\",\"errorMsg\":\"Последние 4 символа - цифры\"}]}, \"value\": \"\", \"visited\": false}",
                FieldComponent.class
        )

        when:
        ComponentResponse<String> initialValue = stringInputComponent.getInitialValue(component, scenarioDto, serviceDescriptor)

        then:
        initialValue.get() == "057-551-458 77"
    }

    def "testCalcProduceTrue"() {
        given:
        FieldComponent component = jsonProcessingService.fromJson(
                "{\"id\": \"add1\", \"type\": \"StringInput\", \"required\": true, \"label\": \"Идентификационный номер (VIN)\", \"attrs\": {\"preset_from\": {\"type\": \"calc\",\"value\": \"\$q1.value == 'Нет' ? \$pd4.value.regAddr.fullAddress : \$fai17.value.text\"}, \"ref\":[{\"relatedRel\":\"add3\",\"val\":true,\"relation\":\"disabled\"}],\"validation\":[{\"type\":\"RegExp\",\"value\":\"^[A-Z0-9]*\$\",\"errorMsg\":\"Поле может содержать только цифры и заглавные латинские буквы\"}, {\"type\":\"RegExp\",\"value\":\"^[^QIO]*\$\",\"errorMsg\":\"Не используются буквы Q, I, O\"}, {\"type\":\"RegExp\",\"value\":\"^(|.{17})\$\",\"errorMsg\":\"Длина поля 17 символов\"}, {\"type\":\"RegExp\",\"value\":\"^(|.{13}[0-9]{4})\$\",\"errorMsg\":\"Последние 4 символа - цифры\"}]}, \"value\": \"\", \"visited\": false}",
                FieldComponent.class
        )

        when:
        ComponentResponse<String> initialValue = stringInputComponent.getInitialValue(component, scenarioDto, serviceDescriptor)

        then:
        initialValue.get() == "196247, г. Санкт-Петербург, пр-кт. Ленинский, д. 147, к. 5, кв. 55"
    }

    def "testCalcProduceFalse"() {
        given:
        FieldComponent component = jsonProcessingService.fromJson(
                "{\"id\": \"add1\", \"type\": \"StringInput\", \"required\": true, \"label\": \"Идентификационный номер (VIN)\", \"attrs\": {\"preset_from\": {\"type\": \"calc\",\"value\": \"\$q1.value == 'Да' ? \$pd4.value.regAddr.fullAddress : \$fai17.value.text\"}, \"ref\":[{\"relatedRel\":\"add3\",\"val\":true,\"relation\":\"disabled\"}],\"validation\":[{\"type\":\"RegExp\",\"value\":\"^[A-Z0-9]*\$\",\"errorMsg\":\"Поле может содержать только цифры и заглавные латинские буквы\"}, {\"type\":\"RegExp\",\"value\":\"^[^QIO]*\$\",\"errorMsg\":\"Не используются буквы Q, I, O\"}, {\"type\":\"RegExp\",\"value\":\"^(|.{17})\$\",\"errorMsg\":\"Длина поля 17 символов\"}, {\"type\":\"RegExp\",\"value\":\"^(|.{13}[0-9]{4})\$\",\"errorMsg\":\"Последние 4 символа - цифры\"}]}, \"value\": \"\", \"visited\": false}",
                FieldComponent.class
        )

        when:
        ComponentResponse<String> initialValue = stringInputComponent.getInitialValue(component, scenarioDto, serviceDescriptor)

        then:
        initialValue.get() == "Профессиональное среднее"
    }

    private <T> T getResource(String fileName, Class<T> clazz) {
        InputStream is
        try {
            is = getClass().getClassLoader().getResourceAsStream(fileName)

            return objectMapper.readValue(
                    is,
                    clazz
            )
        } finally {
            if (is != null) {
                is.close()
            }
        }
    }

}
