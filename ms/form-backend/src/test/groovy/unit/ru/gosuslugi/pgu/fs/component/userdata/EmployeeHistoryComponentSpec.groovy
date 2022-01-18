package unit.ru.gosuslugi.pgu.fs.component.userdata

import groovy.json.JsonBuilder
import ru.gosuslugi.pgu.components.descriptor.types.EmployeeHistory
import ru.gosuslugi.pgu.dto.ApplicantAnswer
import ru.gosuslugi.pgu.dto.descriptor.FieldComponent
import ru.gosuslugi.pgu.dto.descriptor.types.ComponentType
import ru.gosuslugi.pgu.fs.common.component.ComponentResponse
import ru.gosuslugi.pgu.fs.common.utils.AnswerUtil
import ru.gosuslugi.pgu.fs.component.ComponentTestUtil
import ru.gosuslugi.pgu.fs.component.userdata.EmployeeHistoryComponent
import spock.lang.Specification

import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle

class EmployeeHistoryComponentSpec extends Specification {

    static final def DATE_FROM_FIRST = LocalDate.now().getYear() - 10
    static final def DATE_TO_FIRST = LocalDate.now().getYear() - 9
    static final def DATE_FROM_SECOND = LocalDate.now().getYear() - 9
    static final def DATE_TO_SECOND = LocalDate.now().getYear() - 8
    static final def DATE_FROM_THIRD = LocalDate.now().getYear() - 8
    static final def DATE_TO_THIRD = LocalDate.now().getYear() - 6
    static final def DATE_FROM_LAST = LocalDate.now().getYear() - 6
    static final def DATE_TO_LAST = LocalDate.now().getYear()
    static final def MONTH_INDEX = LocalDate.now().getMonth().ordinal()
    static final def MONTH_NAME = LocalDate.now().getMonth().getDisplayName(TextStyle.FULL_STANDALONE, Locale.forLanguageTag("ru")).capitalize()

    static final def ERR_MESSAGE_DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM")

    static final def EMPLOYEE_HISTORY_LIST = [
            [
                    type          : "student",
                    label         : "Я учился",
                    from          : [year: DATE_FROM_FIRST, month: MONTH_INDEX, monthCode: MONTH_NAME],
                    to            : [year: DATE_TO_FIRST, month: MONTH_INDEX, monthCode: MONTH_NAME],
                    place         : "Институт информационных технологий при поддержке Индийского фонда почета и процветания",
                    address       : "г. Москва, ул. Ганеша",
                    checkboxToDate: false
            ] as EmployeeHistory,
            [
                    type          : "unemployed",
                    label         : "Я не работал и не учился",
                    from          : [year: DATE_FROM_SECOND, month: MONTH_INDEX, monthCode: MONTH_NAME],
                    to            : [year: DATE_TO_SECOND, month: MONTH_INDEX, monthCode: MONTH_NAME],
                    address       : "г. Москва",
                    checkboxToDate: false
            ] as EmployeeHistory,
            [
                    type          : "military",
                    label         : "Я служил",
                    from          : [year: DATE_FROM_THIRD, month: MONTH_INDEX, monthCode: MONTH_NAME],
                    to            : [year: DATE_TO_THIRD, month: MONTH_INDEX, monthCode: MONTH_NAME],
                    position      : "Разведчик",
                    place         : "в/ч 64044",
                    address       : "Псков",
                    checkboxToDate: false
            ] as EmployeeHistory,
            [
                    type          : "employed",
                    label         : "Я работал",
                    from          : [year: DATE_FROM_LAST, month: MONTH_INDEX, monthCode: MONTH_NAME],
                    to            : [year: DATE_TO_LAST, month: MONTH_INDEX, monthCode: MONTH_NAME],
                    position      : "Уборщик",
                    place         : "\"ИП Светлячок\" Склад Ядерных Отходов",
                    address       : "г. Балашиха, ул. Радиоактивная, стр. 22Б",
                    checkboxToDate: false
            ] as EmployeeHistory
    ] as List<EmployeeHistory>

    EmployeeHistoryComponent component
    FieldComponent fieldComponent

    void setup() {
        component = new EmployeeHistoryComponent()
        ComponentTestUtil.setAbstractComponentServices(component)
        fieldComponent = configureTestFieldComponent(10, true)
    }

    def "Should have correct initial state"() {
        expect:
        component.getType() == ComponentType.EmployeeHistory
        component.getInitialValue(fieldComponent) == ComponentResponse.empty()
        !component.isCycled()
    }

    def "Can validate component"() {
        given:
        Map<String, String> incorrectAnswers = [:]
        List<EmployeeHistory> activities = [
                [
                        type          : testType,
                        from          : testFrom,
                        to            : testTo,
                        position      : testPosition,
                        place         : testPlace,
                        address       : testAddress,
                        checkboxToDate: false
                ] as EmployeeHistory
        ] as List<EmployeeHistory>

        when:
        component.validate(incorrectAnswers, fieldComponent, activities)

        then:
        incorrectAnswers['eh1'] == errorMsgSingle
        incorrectAnswers['eh1.activities[0].type'] == errorMsgType
        incorrectAnswers['eh1.activities[0].from'] == errorMsgFrom
        incorrectAnswers['eh1.activities[0].to'] == errorMsgTo
        incorrectAnswers['eh1.activities[0].position'] == errorMsgPosition
        incorrectAnswers['eh1.activities[0].place'] == errorMsgPlace
        incorrectAnswers['eh1.activities[0].address'] == errorMsgAddress

        where:
        testType << ["employed", "employed", null]
        testFrom << [[year: LocalDate.now().getYear() - 5, month: MONTH_INDEX, monthCode: MONTH_NAME],
                     [year: LocalDate.now().getYear() - 5, month: MONTH_INDEX, monthCode: MONTH_NAME], null]
        testTo << [[year: LocalDate.now().getYear(), month: MONTH_INDEX, monthCode: MONTH_NAME],
                   [year: LocalDate.now().getYear(), month: MONTH_INDEX, monthCode: MONTH_NAME], null]
        testPosition << ["Уборщик", null, null]
        testPlace << ["\"ИП Светлячок\" Склад Ядерных Отходов", null, null]
        testAddress << ["г. Балашиха, ул. Радиоактивная, стр. 22Б", null, null]
        errorMsgSingle << [
                "Missing periods: ${LocalDate.now().minusYears(10).format(ERR_MESSAGE_DATE_FORMATTER)}" +
                        " — ${LocalDate.now().minusYears(5).minusMonths(1).format(ERR_MESSAGE_DATE_FORMATTER)}",
                "Missing periods: ${LocalDate.now().minusYears(10).format(ERR_MESSAGE_DATE_FORMATTER)}" +
                        " — ${LocalDate.now().minusYears(5).minusMonths(1).format(ERR_MESSAGE_DATE_FORMATTER)}",
                "Missing periods: ${LocalDate.now().minusYears(10).format(ERR_MESSAGE_DATE_FORMATTER)}" +
                        " — ${LocalDate.now().format(ERR_MESSAGE_DATE_FORMATTER)}"
        ]
        errorMsgType << [null, null, "Activity type cannot be null or blank"]
        errorMsgFrom << [null, null, "Date from cannot be null"]
        errorMsgTo << [null, null, "Date to cannot be null"]
        errorMsgPosition << [null, "Position cannot be null or blank", "Position cannot be null or blank"]
        errorMsgPlace << [null, "Place cannot be null or blank", "Place cannot be null or blank"]
        errorMsgAddress << [null, "Address cannot be null or blank", "Address cannot be null or blank"]
    }

    def "Can validate after submit"() {
        given:
        Map<String, String> incorrectAnswers = [:]

        List<EmployeeHistory> answerValue = [EMPLOYEE_HISTORY_LIST[2], EMPLOYEE_HISTORY_LIST[1], EMPLOYEE_HISTORY_LIST[3], EMPLOYEE_HISTORY_LIST[0]]
        Map.Entry<String, ApplicantAnswer> entry = AnswerUtil.createAnswerEntry("eh1", new JsonBuilder(answerValue).toPrettyString())

        when:
        component.validateAfterSubmit(incorrectAnswers, entry, fieldComponent)

        then:
        incorrectAnswers == [:]
        entry.getValue().getValue() ==
                "[" +
                "{\"type\":\"student\",\"label\":\"Я учился\",\"from\":{\"year\":${DATE_FROM_FIRST},\"month\":${MONTH_INDEX},\"monthCode\":\"${MONTH_NAME}\"},\"to\":{\"year\":${DATE_TO_FIRST},\"month\":${MONTH_INDEX},\"monthCode\":\"${MONTH_NAME}\"},\"place\":\"Институт информационных технологий при поддержке Индийского фонда почета и процветания\",\"address\":\"г. Москва, ул. Ганеша\",\"checkboxToDate\":false}," +
                "{\"type\":\"unemployed\",\"label\":\"Я не работал и не учился\",\"from\":{\"year\":${DATE_FROM_SECOND},\"month\":${MONTH_INDEX},\"monthCode\":\"${MONTH_NAME}\"},\"to\":{\"year\":${DATE_TO_SECOND},\"month\":${MONTH_INDEX},\"monthCode\":\"${MONTH_NAME}\"},\"address\":\"г. Москва\",\"checkboxToDate\":false}," +
                "{\"type\":\"military\",\"label\":\"Я служил\",\"from\":{\"year\":${DATE_FROM_THIRD},\"month\":${MONTH_INDEX},\"monthCode\":\"${MONTH_NAME}\"},\"to\":{\"year\":${DATE_TO_THIRD},\"month\":${MONTH_INDEX},\"monthCode\":\"${MONTH_NAME}\"},\"position\":\"Разведчик\",\"place\":\"в/ч 64044\",\"address\":\"Псков\",\"checkboxToDate\":false}," +
                "{\"type\":\"employed\",\"label\":\"Я работал\",\"from\":{\"year\":${DATE_FROM_LAST},\"month\":${MONTH_INDEX},\"monthCode\":\"${MONTH_NAME}\"},\"to\":{\"year\":${DATE_TO_LAST},\"month\":${MONTH_INDEX},\"monthCode\":\"${MONTH_NAME}\"},\"position\":\"Уборщик\",\"place\":\"\\\"ИП Светлячок\\\" Склад Ядерных Отходов\",\"address\":\"г. Балашиха, ул. Радиоактивная, стр. 22Б\",\"checkboxToDate\":false}" +
                "]"
    }

    def "Should sort employee history list [util]"() {
        given:
        List<EmployeeHistory> expectedList = [EMPLOYEE_HISTORY_LIST[0], EMPLOYEE_HISTORY_LIST[1], EMPLOYEE_HISTORY_LIST[2], EMPLOYEE_HISTORY_LIST[3]]
        List<EmployeeHistory> actualList = [EMPLOYEE_HISTORY_LIST[3], EMPLOYEE_HISTORY_LIST[2], EMPLOYEE_HISTORY_LIST[0], EMPLOYEE_HISTORY_LIST[1]]

        when:
        List<EmployeeHistory> employeeHistories = EmployeeHistoryComponent.sortEmployeeHistory(actualList)

        then:
        employeeHistories == expectedList
    }

    def "Should create list of integers (bounds included) [util]"() {
        given:
        List<Integer> listOfIntegers = EmployeeHistoryComponent.createListForNumbers(lowerBound, upperBound)

        expect:
        listOfIntegers.size() == size
        listOfIntegers == list

        where:
        size << [5, 7, 10]
        lowerBound << [1, -1, 1]
        upperBound << [5, 5, 10]
        list << [[1, 2, 3, 4, 5], [-1, 0, 1, 2, 3, 4, 5], [1, 2, 3, 4, 5, 6, 7, 8, 9, 10]]
    }

    static def configureTestFieldComponent(int yearsPeriod, boolean isNonStop) {
        new FieldComponent(
                id: 'eh1', type: ComponentType.EmployeeHistory, label: "", attrs: [fields: [years: yearsPeriod, nonStop: isNonStop]]
        )
    }
}
