package ru.gosuslugi.pgu.fs.component.input

import com.fasterxml.jackson.databind.ObjectMapper
import ru.gosuslugi.pgu.common.core.json.JsonFileUtil
import ru.gosuslugi.pgu.common.core.json.JsonProcessingUtil
import ru.gosuslugi.pgu.common.esia.search.dto.UserOrgData
import ru.gosuslugi.pgu.common.esia.search.dto.UserPersonalData
import ru.gosuslugi.pgu.dto.ApplicantAnswer
import ru.gosuslugi.pgu.dto.ScenarioDto
import ru.gosuslugi.pgu.dto.ServiceInfoDto
import ru.gosuslugi.pgu.dto.descriptor.FieldComponent
import ru.gosuslugi.pgu.fs.common.component.input.StringInputComponent
import ru.gosuslugi.pgu.fs.common.service.EvaluationExpressionService
import ru.gosuslugi.pgu.fs.common.service.JsonProcessingService
import ru.gosuslugi.pgu.fs.common.service.ProtectedFieldService
import ru.gosuslugi.pgu.fs.common.service.condition.*
import ru.gosuslugi.pgu.fs.common.service.impl.EvaluationExpressionServiceImpl
import ru.gosuslugi.pgu.fs.common.service.impl.JsonProcessingServiceImpl
import ru.gosuslugi.pgu.fs.common.variable.ServiceIdVariable
import ru.gosuslugi.pgu.fs.common.variable.VariableRegistry
import ru.gosuslugi.pgu.fs.component.ComponentTestUtil
import ru.gosuslugi.pgu.fs.service.EmpowermentService
import ru.gosuslugi.pgu.fs.service.impl.InitialValueFromImpl
import ru.gosuslugi.pgu.fs.service.impl.ProtectedFieldServiceImpl
import ru.gosuslugi.pgu.fs.service.impl.UserDataServiceImpl
import ru.gosuslugi.pgu.fs.utils.CalculatedAttributesHelper
import ru.gosuslugi.pgu.fs.utils.ParseAttrValuesHelper
import spock.lang.Shared
import spock.lang.Specification

class StringInputComponentTest extends Specification {

    ScenarioDto scenarioDto = Mock(ScenarioDto)

    @Shared
    ServiceIdVariable serviceIdVariable = Stub(ServiceIdVariable)

    VariableRegistry variableRegistry = new VariableRegistry()
    @Shared
    EmpowermentService empowermentServiceMock = Mock(EmpowermentService)
    @Shared
    ProtectedFieldService protectedFieldService = new ProtectedFieldServiceImpl(
            new UserPersonalData(),
            new UserOrgData(),
            new UserDataServiceImpl(new UserPersonalData(), new UserOrgData()),
            empowermentServiceMock
    )
    @Shared
    ObjectMapper objectMapper = JsonProcessingUtil.getObjectMapper()
    @Shared
    JsonProcessingService jsonProcessingService = new JsonProcessingServiceImpl(objectMapper)
    @Shared
    ParseAttrValuesHelper parseAttrValuesHelper = new ParseAttrValuesHelper(variableRegistry, jsonProcessingService, protectedFieldService)
    @Shared
    CalculatedAttributesHelper calculatedAttributesHelper = new CalculatedAttributesHelper(serviceIdVariable, parseAttrValuesHelper, null)
    @Shared
    InitialValueFromImpl initialValueFromService = new InitialValueFromImpl(parseAttrValuesHelper, calculatedAttributesHelper)
    @Shared
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
    @Shared
    EvaluationExpressionService evaluationExpressionService = new EvaluationExpressionServiceImpl()
    @Shared
    StringInputComponent stringInputComponent

    def setupSpec() {
        calculatedAttributesHelper.postConstruct()
        stringInputComponent = new StringInputComponent(initialValueFromService, conditionCheckerHelper, evaluationExpressionService)
        ComponentTestUtil.setAbstractComponentServices(stringInputComponent, jsonProcessingService)
    }

    def 'answer validation test'() {
        given:
        FieldComponent component = JsonProcessingUtil.fromJson(JsonFileUtil.getJsonFromFile(this.getClass(), "-component.json"), FieldComponent.class)
        Map.Entry<String, ApplicantAnswer> entry = geEntry("fai1", value)
        def scenarioDto = ComponentTestUtil.mockScenario(new HashMap<String, ApplicantAnswer>(), new HashMap<String, ApplicantAnswer>(), new ServiceInfoDto())

        when:
        Map<String, String> map = stringInputComponent.validate(entry, scenarioDto, component)

        then:
        map != null
        map.size() == size
        map.get("fai1") == text

        where:
        value      | size | text
        null       | 1    | "Значение не задано"
        ''         | 1    | "Значение не задано"
        '2020 год' | 1    | "Поле может содержать только русские буквы"
        'год'      | 0    | null
    }

    def 'answer validation relation test'() {
        given:
        FieldComponent component = JsonProcessingUtil.fromJson(JsonFileUtil.getJsonFromFile(this.getClass(), "-component-relation.json"), FieldComponent.class)
        Map<String, ApplicantAnswer> currentAnswer = getCurrentAnswers("add3", value)
        def scenarioDto = ComponentTestUtil.mockScenario(currentAnswer, new HashMap<String, ApplicantAnswer>(), new ServiceInfoDto())
        Map.Entry<String, ApplicantAnswer> entry = geEntry("add1", "Wrong answer!")

        when:
        Map<String, String> map = stringInputComponent.validate(entry, scenarioDto, component)

        then:
        map != null
        map.size() == size
        map.get("add1") == text

        where:
        value   | size | text
        "true"  | 0    | null
        "false" | 1    | "Поле может содержать только цифры и заглавные латинские буквы, Длина поля 17 символов, Последние 4 символа - цифры"
    }

    def 'answer calculation validation test'() {
        given:
        FieldComponent component = JsonProcessingUtil.fromJson(JsonFileUtil.getJsonFromFile(this.getClass(), "-component-calculationPredicate.json"), FieldComponent.class)
        Map<String, ApplicantAnswer> currentAnswer = Map.of(
                "PurchaseFullCost", [visited: true, value: fullCost] as ApplicantAnswer,
                "PurchasePaid", [visited: true, value: purchaseCost] as ApplicantAnswer
        )
        def scenarioDto = ComponentTestUtil.mockScenario(currentAnswer, new HashMap<String, ApplicantAnswer>(), new ServiceInfoDto())
        conditionCheckerHelper.getFirstFromContexts("PurchaseFullCost.value", _, String.class) >> fullCost
        conditionCheckerHelper.getFirstFromContexts("PurchasePaid.value", _, String.class) >> purchaseCost
        Map.Entry<String, ApplicantAnswer> entry = Map.entry("fai1", [value: 100.05] as ApplicantAnswer)

        when:
        Map<String, String> map = stringInputComponent.validate(entry, scenarioDto, component)
        jsonProcessingService.releaseThreadCache()
        then:
        jsonProcessingService.releaseThreadCache()
        map != null
        map.size() == size
        map.get("fai1") == text
        jsonProcessingService.releaseThreadCache()

        where:
        fullCost| purchaseCost   | size | text
        20.05   | 20.05          | 1    | "Полная стоимость путёвки должна превышать оплаченную"
        1000.05  | 100.05        | 0    | null
    }


    private Map<String, ApplicantAnswer> getCurrentAnswers(String componentId, String value) {
        Map<String, ApplicantAnswer> currentAnswer = new HashMap<>()
        ApplicantAnswer answer = new ApplicantAnswer()
        answer.setVisited(true)
        answer.setValue(value)
        currentAnswer.put(componentId, answer)
        return currentAnswer
    }

    private Map.Entry<String, ApplicantAnswer> geEntry(String id, String value) {
        ApplicantAnswer answer = new ApplicantAnswer();
        answer.setValue(value);
        Map.Entry<String, ApplicantAnswer> entry = new AbstractMap.SimpleEntry<String, ApplicantAnswer>(id, answer);
        return entry;
    }
}
