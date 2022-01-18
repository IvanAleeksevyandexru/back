package ru.gosuslugi.pgu.fs.component.logic

import org.springframework.mock.web.MockHttpServletRequest
import org.springframework.web.context.request.RequestContextHolder
import org.springframework.web.context.request.ServletRequestAttributes
import ru.gosuslugi.pgu.common.core.json.JsonProcessingUtil
import ru.gosuslugi.pgu.dto.ApplicantAnswer
import ru.gosuslugi.pgu.dto.ScenarioDto
import ru.gosuslugi.pgu.dto.cycled.CycledApplicantAnswers
import ru.gosuslugi.pgu.dto.descriptor.FieldComponent
import ru.gosuslugi.pgu.dto.descriptor.types.ComponentType
import ru.gosuslugi.pgu.fs.common.descriptor.MainDescriptorService
import ru.gosuslugi.pgu.fs.common.service.*
import ru.gosuslugi.pgu.fs.common.service.impl.ComponentReferenceServiceImpl
import ru.gosuslugi.pgu.fs.common.service.impl.JsonProcessingServiceImpl
import ru.gosuslugi.pgu.fs.common.variable.ServiceIdVariable
import ru.gosuslugi.pgu.fs.common.variable.VariableRegistry
import ru.gosuslugi.pgu.fs.utils.CalculatedAttributesHelper
import ru.gosuslugi.pgu.fs.utils.ParseAttrValuesHelper
import spock.lang.Specification

class ValueCalculatorComponentSpec extends Specification {
    private static final String CALCULATIONS_ATTR = 'calculations'
    private static final String REFS_ATTR = 'refs'
    private static final String ARGUMENTS_ATTR = 'arguments'
    private static final String CODE_ATTR = 'code'
    private static final String ANOTHER_PERSON_ATTR = 'anotherperson'
    private static final String LAST_NAME_REF_ATTR = 'lastNameRef'
    private static final String SHORT_LAST_NAME_REF_ATTR = 'shortLastNameRef'
    private static final String DATE_REF_ATTR = 'dateRef'
    private static final String DEFAULT_DATE_REF_ATTR = 'defaultDateRef'
    private static final String MESSAGE_ATTR = 'message'
    private static final String LAST_NAME_ATTR = 'lastName'

    private static JsonProcessingService jsonProcessingService
    private static ValueCalculatorComponent valueCalculatorComponent

    private static def request = [] as MockHttpServletRequest

    def setup() {
        RequestContextHolder.setRequestAttributes([request] as ServletRequestAttributes)
        jsonProcessingService = new JsonProcessingServiceImpl(JsonProcessingUtil.getObjectMapper())
        def serviceIdVariable = new ServiceIdVariable(Stub(MainDescriptorService), jsonProcessingService, Stub(RuleConditionService))
        def parseAttrValuesHelper = new ParseAttrValuesHelper(Stub(VariableRegistry), jsonProcessingService, Stub(ProtectedFieldService))
        def calculatedAttributesHelper = new CalculatedAttributesHelper(serviceIdVariable, parseAttrValuesHelper, null)
        def componentReferenceService = new ComponentReferenceServiceImpl(jsonProcessingService, Stub(UserCookiesService), Stub(LinkedValuesService))
        calculatedAttributesHelper.postConstruct()
        valueCalculatorComponent = new ValueCalculatorComponent(calculatedAttributesHelper, componentReferenceService)
        valueCalculatorComponent.jsonProcessingService = jsonProcessingService
    }

    def 'component type is ValueCalculator'() {
        expect:
        valueCalculatorComponent.getType() == ComponentType.ValueCalculator
    }

    def "empty value check"() {
        given:
        def fieldComponent = new FieldComponent(id: 'vc1')
        def scenarioDto = new ScenarioDto()

        when:
        valueCalculatorComponent.getInitialValue(fieldComponent, scenarioDto)
        Map<String, Object> result = jsonProcessingService.fromJson(
                scenarioDto.getApplicantAnswerByFieldId(fieldComponent.getId()).value, Map.class
        )

        then:
        result.size() == 0
    }

    def "not empty value check"() {
        given:
        def fieldComponent = getFieldComponent()
        def scenarioDto = getScenarioDto()

        when:
        valueCalculatorComponent.getInitialValue(fieldComponent, scenarioDto)
        Map<String, Object> result = JsonProcessingUtil.fromJson(
                scenarioDto.getApplicantAnswerByFieldId(fieldComponent.getId()).value, Map.class
        )

        then:
        result.containsKey(CALCULATIONS_ATTR) && result.containsKey(REFS_ATTR) && result.containsKey(ARGUMENTS_ATTR)

        Map<String, Object> calculations = (Map<String, Object>) result.get(CALCULATIONS_ATTR)
        calculations.size() == 2
        calculations.containsKey(CODE_ATTR) && calculations.get(CODE_ATTR) == '-12345'
        calculations.containsKey(ANOTHER_PERSON_ATTR) && calculations.get(ANOTHER_PERSON_ATTR) == 'N'

        Map<String, String> refs = (Map<String, String>) result.get(REFS_ATTR)
        refs.size() == 4
        refs.containsKey(LAST_NAME_REF_ATTR) && refs.get(LAST_NAME_REF_ATTR) == 'Каменев'
        refs.containsKey(SHORT_LAST_NAME_REF_ATTR) && refs.get(SHORT_LAST_NAME_REF_ATTR) == 'К'
        refs.containsKey(DATE_REF_ATTR) && refs.get(DATE_REF_ATTR) == '16 ноября 2020 г. в 16:00'
        refs.containsKey(DEFAULT_DATE_REF_ATTR) && refs.get(DEFAULT_DATE_REF_ATTR) == 'Нет данных'

        Map<String, String> arguments = (Map<String, String>) result.get(ARGUMENTS_ATTR)
        arguments.size() == 2
        arguments.containsKey(MESSAGE_ATTR) && arguments.get(MESSAGE_ATTR) == 'hello'
        arguments.containsKey(LAST_NAME_ATTR) && arguments.get(LAST_NAME_ATTR) == 'Каменев'
    }

    def "when ValueCalculator is cycled"() {
        given:
        def fieldComponent = getFieldComponent()
        fieldComponent.getAttrs().put("isCycled", true)

        def scenarioDto = getScenarioDto()
        scenarioDto.setCycledApplicantAnswers(new CycledApplicantAnswers(currentAnswerId: "anysomeCurrentAnswerId"))

        when:
        valueCalculatorComponent.getInitialValue(fieldComponent, scenarioDto)
        def cycledApplicantAnswers = scenarioDto.getCycledApplicantAnswers()

        then:
        cycledApplicantAnswers.getCurrentAnswer().getItems().size() == 1
        fieldComponent.getId().equals(cycledApplicantAnswers.getCurrentAnswer().getItem(fieldComponent.getId()).get().getId())

        cycledApplicantAnswers.getCurrentAnswer().getItem(fieldComponent.getId()).get().getItemAnswers().size() == 3
        cycledApplicantAnswers.getCurrentAnswer().getItem(fieldComponent.getId()).get().getItemAnswers().containsKey(REFS_ATTR)
        cycledApplicantAnswers.getCurrentAnswer().getItem(fieldComponent.getId()).get().getItemAnswers().containsKey(ARGUMENTS_ATTR)
        cycledApplicantAnswers.getCurrentAnswer().getItem(fieldComponent.getId()).get().getItemAnswers().containsKey(CALCULATIONS_ATTR)

    }

    def static getFieldComponent() {
        new FieldComponent(
                id: 'vc1',
                type: ComponentType.ValueCalculator,
                attrs: [
                        calculations: [
                                [
                                        attributeName: 'code',
                                        expr: '\'-12345\'',
                                        valueType: 'calc'
                                ],
                                [
                                        attributeName: 'anotherperson',
                                        expr: '$q1.value == \'Себя\' ? \'N\' : \'Y\'',
                                        valueType: 'calc'
                                ]
                        ],
                        refs: [
                                lastNameRef: 'pd2.value.storedValues.lastName',
                                shortLastNameRef: [
                                        path: 'pd2.value.storedValues.lastName',
                                        converter: 'SPEL',
                                        expression: 'charAt(0)'
                                ],
                                dateRef: [
                                        path: 'pd3.value.storedValues.date',
                                        converter: 'DATE',
                                        format: 'dd MMMM yyyy \'г. в\' HH:mm'
                                ],
                                defaultDateRef: [
                                        path: 'other.value.storedValues.date',
                                        converter: 'DATE',
                                        format: 'dd MMMM yyyy \'г. в\' HH:mm',
                                        default: 'Нет данных'
                                ]
                        ]
                ],
                arguments: [message: 'hello', lastName: 'Каменев']
        )
    }

    def static getScenarioDto() {
        new ScenarioDto(
                applicantAnswers: [
                        q1: new ApplicantAnswer(value: 'Себя'),
                        pd1: new ApplicantAnswer(
                                value: '{"states":[{"groupName":"Каменев Игорь Витальевич","fields":[{"label":"Дата рождения","value":"17.08.1969"},{"label":"СНИЛС","value":"112-233-446 96"}]}],"storedValues":{"firstName":"Игорь","lastName":"Каменев","middleName":"Витальевич","birthDate":"17.08.1969","gender":"M","citizenship":"RUS","citizenshipCode":"RUS","snils":"112-233-446 96"},"errors":[]}',
                        ),
                        pd2: new ApplicantAnswer(
                                value: '{"states":[{"groupName":"Каменев Игорь Витальевич","fields":[{"label":"Номер полиса ОМС","value":"2012961391756185"}]}],"storedValues":{"firstName":"Игорь","lastName":"Каменев","middleName":"Витальевич","gender":"M","citizenship":"RUS","citizenshipCode":"RUS","omsNumber":"2012961391756185"},"errors":[]}'
                        ),
                        pd3: new ApplicantAnswer(
                                value: "{\"storedValues\":{\"date\":\"2020-11-16T16:00:00.000+06:00\"}}"
                        )
                ]
        )
    }
}
