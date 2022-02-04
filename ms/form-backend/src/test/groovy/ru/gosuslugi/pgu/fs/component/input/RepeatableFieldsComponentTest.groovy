package ru.gosuslugi.pgu.fs.component.input

import ru.gosuslugi.pgu.common.core.json.JsonProcessingUtil
import ru.gosuslugi.pgu.components.FieldComponentUtil
import ru.gosuslugi.pgu.dto.ApplicantAnswer
import ru.gosuslugi.pgu.dto.ScenarioDto
import ru.gosuslugi.pgu.dto.descriptor.FieldComponent
import ru.gosuslugi.pgu.dto.descriptor.ServiceDescriptor
import ru.gosuslugi.pgu.dto.descriptor.types.ComponentType
import ru.gosuslugi.pgu.fs.common.component.ComponentRegistry
import ru.gosuslugi.pgu.fs.common.component.input.StringInputComponent
import ru.gosuslugi.pgu.fs.common.service.impl.ListComponentItemUniquenessServiceImpl
import ru.gosuslugi.pgu.fs.component.dictionary.LookupComponent
import spock.lang.Specification

class RepeatableFieldsComponentTest extends Specification {

    private static RepeatableFieldsComponent REPEATABLE_FIELDS_COMPONENT
    private static RepeatableFieldsComponent REPEATABLE_FIELDS_COMPONENT_WITH_REGISTRY
    private static RepeatableFieldsComponent REPEATABLE_FIELDS_COMPONENT_WITH_LIST

    private static ScenarioDto SCENARIO_DTO
    private static final String KEY = "key"

    def setupSpec() {

        REPEATABLE_FIELDS_COMPONENT = new RepeatableFieldsComponent(new ComponentRegistry(), new ListComponentItemUniquenessServiceImpl())
        SCENARIO_DTO = new ScenarioDto()
    }

    def 'Test getType'() {
        expect:
        REPEATABLE_FIELDS_COMPONENT.getType() == ComponentType.RepeatableFields
    }

    def 'Test preProcess: FieldComponent with empty attrs'() {
        given:
        def component = new FieldComponent(attrs: Collections.EMPTY_MAP)

        def componentFirst = new FieldComponent(id: "pd1_1")
        def componentSecond = new FieldComponent(id: "pd1_2")
        def componentThird = new FieldComponent(id: "w1")
        def listComponents = Arrays.asList(componentFirst, componentSecond, componentThird)

        def serviceDescriptor = Stub(ServiceDescriptor) {
            getApplicationFields() >> listComponents
        }

        when:
        REPEATABLE_FIELDS_COMPONENT.preProcess(component, SCENARIO_DTO, serviceDescriptor)

        then:
        component.getAttrs().isEmpty()
    }

    def 'Test preProcess: FieldComponent with attrs'() {
        given:
        def component = new FieldComponent(attrs: [components: ["pd1_1", "pd1_2"]])

        def componentFirst = new FieldComponent(id: "pd1_1")
        def componentSecond = new FieldComponent(id: "pd1_2")
        def componentThird = new FieldComponent(id: "w1")
        def listComponents = Arrays.asList(componentFirst, componentSecond, componentThird)

        def serviceDescriptor = Stub(ServiceDescriptor) {
            getApplicationFields() >> listComponents
        }

        when:
        REPEATABLE_FIELDS_COMPONENT.preProcess(component, SCENARIO_DTO, serviceDescriptor)

        then:
        component.getAttrs().get(FieldComponentUtil.COMPONENTS_KEY) as Set == Arrays.asList(componentFirst, componentSecond) as Set
    }

    def 'Test getInitialValue: FieldComponent with empty attrs components '() {
        given:
        def component = new FieldComponent(attrs: [components: Collections.EMPTY_LIST])

        def serviceDescriptor = Mock(ServiceDescriptor)
        def listComponentItemUniquenessService = Mock(ListComponentItemUniquenessServiceImpl)
        def lookupComponent = Mock(LookupComponent)

        def componentRegistry = Stub(ComponentRegistry) {
            getComponent(ComponentType.Lookup) >> lookupComponent
            getComponent(_ as ComponentType) >> null
        }
        REPEATABLE_FIELDS_COMPONENT_WITH_REGISTRY = new RepeatableFieldsComponent(
                componentRegistry, listComponentItemUniquenessService
        )

        expect:
        REPEATABLE_FIELDS_COMPONENT_WITH_REGISTRY.getInitialValue(component, SCENARIO_DTO, serviceDescriptor)
                .get() == JsonProcessingUtil.toJson(Arrays.asList(Collections.EMPTY_MAP))
    }

    def 'Test getInitialValue: FieldComponent with attrs components'() {
        given:
        def componentFirst = new FieldComponent(id: "pd1_1", type: ComponentType.Lookup, value: "valueFirst")
        def componentSecond = new FieldComponent(id: "pd1_2", type: ComponentType.StringInput, value: "valueSecond")
        def component = new FieldComponent(attrs: [components: [componentFirst, componentSecond]])

        def serviceDescriptor = Mock(ServiceDescriptor)
        def listComponentItemUniquenessService = Mock(ListComponentItemUniquenessServiceImpl)
        def lookupComponent = Mock(LookupComponent)

        def componentRegistry = Stub(ComponentRegistry) {
            getComponent(ComponentType.Lookup) >> lookupComponent
            getComponent(_ as ComponentType) >> null
        }
        REPEATABLE_FIELDS_COMPONENT_WITH_REGISTRY = new RepeatableFieldsComponent(
                componentRegistry, listComponentItemUniquenessService
        )
        def result = new HashMap<String, Object>()
        result.put("pd1_1", "valueFirst")
        result.put("pd1_2", "valueSecond")

        expect:
        REPEATABLE_FIELDS_COMPONENT_WITH_REGISTRY.getInitialValue(component, SCENARIO_DTO, serviceDescriptor)
                .get() == JsonProcessingUtil.toJson(Arrays.asList(result))
    }

    def 'Test getCycledInitialValue: FieldComponent with empty attrs components'() {
        given:
        def component = new FieldComponent(attrs: [components: Collections.EMPTY_LIST])
        def externalData = new HashMap<String, Object>()

        expect:
        REPEATABLE_FIELDS_COMPONENT.getCycledInitialValue(component, externalData)
                .get() == JsonProcessingUtil.toJson(Arrays.asList(Collections.EMPTY_MAP))
    }

    def 'Test getCycledInitialValue: FieldComponent with attrs components'() {
        given:
        def componentFirst = new FieldComponent(id: "pd1_1", type: ComponentType.Lookup, value: "valueFirst")
        def componentSecond = new FieldComponent(id: "pd1_2", type: ComponentType.StringInput, value: "valueSecond")
        def component = new FieldComponent(attrs: [components: [componentFirst, componentSecond]])

        def externalData = new HashMap<String, Object>()

        def result = new HashMap<String, Object>()
        result.put("pd1_1", "valueFirst")
        result.put("pd1_2", "valueSecond")

        expect:
        REPEATABLE_FIELDS_COMPONENT.getCycledInitialValue(component, externalData)
                .get() == JsonProcessingUtil.toJson(Arrays.asList(result))
    }

    def 'Test validateAfterSubmit: children components is not validate'() {
        given:
        def incorrectAnswers = new HashMap<String, String>()
        def valueApplicantAnswer = "[" +
                "{\"ai18_0\":\"7586231\"}," +
                "{\"ai18_0\":\"7586230\"}" +
                "]"
        def entry = Map.entry("ai18", new ApplicantAnswer(true, valueApplicantAnswer))

        def valueFieldComponent = "[" +
                "{\"ai18_0\":\"7586231\"}," +
                "{\"ai18_0\":\"7586230\"}," +
                "{\"ai18_0\":\"7597548\"}," +
                "]"
        def fieldComponent = new FieldComponent(id: "ai18", value: valueFieldComponent,
                attrs: [isCycled  : true,
                        components: [
                                [id: "ai18_0", type: "StringInput"],
                                [id: "ai18_00", type: "LabelSection"]
                        ]]
        )
        def scenarioDto = new ScenarioDto(display: [components: [fieldComponent]])
        def componentIncorrectAnswers = new HashMap<String, String>()
        componentIncorrectAnswers.put(KEY, "Неверное значение")
        def stringComponent = Stub(StringInputComponent) {
            validate(*_) >> componentIncorrectAnswers
            getType() >> ComponentType.StringInput
        }

        def componentRegistry = Stub(ComponentRegistry) {
            getComponent(ComponentType.StringInput) >> stringComponent
            getComponent(_ as ComponentType) >> null
        }
        def listComponentItemUniquenessService = Mock(ListComponentItemUniquenessServiceImpl)

        def component = new FieldComponent(id: "ai18",
                attrs: [isCycled  : true,
                        components: ["ai18_0", "ai18_00"]
                ])

        REPEATABLE_FIELDS_COMPONENT_WITH_REGISTRY = new RepeatableFieldsComponent(
                componentRegistry, listComponentItemUniquenessService
        )

        when:
        REPEATABLE_FIELDS_COMPONENT_WITH_REGISTRY.validateAfterSubmit(incorrectAnswers, entry, scenarioDto, component)

        then:
        incorrectAnswers.get("ai18").contains("Неверное значение")
    }

    def 'Test validateAfterSubmit: children components validate'() {
        given:
        def incorrectAnswers = new HashMap<String, String>()
        def valueApplicantAnswer = "[" +
                "{\"ai18_0\":\"7586231\"}," +
                "{\"ai18_0\":\"7586230\"}" +
                "]"
        def entry = Map.entry("ai18", new ApplicantAnswer(true, valueApplicantAnswer))

        def valueFieldComponent = "[" +
                "{\"ai18_0\":\"7586231\"}," +
                "{\"ai18_0\":\"7586230\"}," +
                "{\"ai18_0\":\"7597548\"}," +
                "]"
        def fieldComponent = new FieldComponent(id: "ai18", value: valueFieldComponent,
                attrs: [isCycled  : true,
                        components: [
                                [id: "ai18_0", type: "StringInput"],
                                [id: "ai18_00", type: "LabelSection"]
                        ]]
        )
        def scenarioDto = new ScenarioDto(display: [components: [fieldComponent]])
        def stringComponent = Stub(StringInputComponent) {
            validate(*_) >> Collections.EMPTY_MAP
            getType() >> ComponentType.StringInput
        }

        def componentRegistry = Stub(ComponentRegistry) {
            getComponent(ComponentType.StringInput) >> stringComponent
            getComponent(_ as ComponentType) >> null
        }
        def listComponentItemUniquenessService = Mock(ListComponentItemUniquenessServiceImpl)

        def component = new FieldComponent(id: "ai18",
                attrs: [isCycled  : true,
                        components: ["ai18_0", "ai18_00"]
                ])

        REPEATABLE_FIELDS_COMPONENT_WITH_REGISTRY = new RepeatableFieldsComponent(
                componentRegistry, listComponentItemUniquenessService
        )

        when:
        REPEATABLE_FIELDS_COMPONENT_WITH_REGISTRY.validateAfterSubmit(incorrectAnswers, entry, scenarioDto, component)

        then:
        incorrectAnswers.isEmpty()
    }

    def 'Test validateItemsUniqueness: fieldComponent with param isCycled '() {
        given:
        def valueApplicantAnswer = "[" +
                "{\"ai18_0\":\"7586231\"}," +
                "{\"ai18_0\":\"7586230\"}" +
                "]"
        def entry = Map.entry("ai18", new ApplicantAnswer(true, valueApplicantAnswer))
        def componentRegistry = Mock(ComponentRegistry)
        def uniquenessErrors = Collections.singletonList(Arrays.asList(Map.of(KEY, "Ошибка")))
        def listComponentItemUniquenessService = Stub(ListComponentItemUniquenessServiceImpl) {
            validateRepeatableFieldsItemsUniqueness(*_) >> uniquenessErrors
        }
        def component = new FieldComponent(id: "ai18",
                attrs: [isCycled  : param,
                        components: ["ai18_0", "ai18_00"]
                ])
        REPEATABLE_FIELDS_COMPONENT_WITH_LIST = new RepeatableFieldsComponent(
                componentRegistry, listComponentItemUniquenessService
        )

        expect:
        REPEATABLE_FIELDS_COMPONENT_WITH_LIST.validateItemsUniqueness(entry, SCENARIO_DTO, component) == result

        where:
        param | result
        true  | Collections.emptyList()
        false | Collections.singletonList(Arrays.asList(Map.of(KEY, "Ошибка")))
    }
}