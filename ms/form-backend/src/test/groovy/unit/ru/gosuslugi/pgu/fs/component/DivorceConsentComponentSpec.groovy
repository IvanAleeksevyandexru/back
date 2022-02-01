package unit.ru.gosuslugi.pgu.fs.component

import ru.gosuslugi.pgu.dto.ApplicantAnswer
import ru.gosuslugi.pgu.dto.ScenarioDto
import ru.gosuslugi.pgu.fs.common.exception.FormBaseException
import ru.gosuslugi.pgu.common.core.json.JsonProcessingUtil
import ru.gosuslugi.pgu.dto.descriptor.FieldComponent
import ru.gosuslugi.pgu.dto.descriptor.ServiceDescriptor
import ru.gosuslugi.pgu.components.dto.FieldDto
import ru.gosuslugi.pgu.components.dto.FormDto
import ru.gosuslugi.pgu.components.dto.StateDto
import ru.gosuslugi.pgu.fs.component.ComponentTestUtil
import ru.gosuslugi.pgu.fs.component.divorce.DivorceConsentComponent
import spock.lang.Specification

class DivorceConsentComponentSpec extends Specification {

    DivorceConsentComponent component

    static final USER_DATA_ANSWER   = [
            "storedValues": ["lastName": "Иванов", "birthDate": "01.01.2000", "rfPasportSeries": "1111",
                             "rfPasportNumber": "123456"]]
    static final REG_ADDR_ANSWER    = ["regAddr": ["fullAddress": "Москва, ул. Ленина, д.1, кв. 1"]]
    static final DIVORCE_DATE       = ["timeSlot": ["visitTimeStr": "2025-01-01T10:00:00"]]
    static final DIVORCE_ZAGS       = ["title": "Первый гродской ЗАГС", "attributeValues": ["zags_address": "Москва, ул. Мира, д. 1"]]

    def setup() {
        // не мокаю mapper так как он больше похож на util класс
        component = new DivorceConsentComponent(JsonProcessingUtil.getObjectMapper())
        ComponentTestUtil.setAbstractComponentServices(component)
    }

    def "Can pre set component value"() {
        given:
        def refMap = ["userData"       : "userData",
                      "regAddr"        : "regAddr",
                      "nationality"    : "nationality",
                      "education"      : "education",
                      "isFirstMarriage": "isFirstMarriage",
                      "newLastName"    : "newLastName"]
        def fieldComponent = new FieldComponent(
                attrs: [
                        "refs": [
                                "participant2"             : refMap,
                                "marriageCertificateNumber": "marriageCertificateNumber",
                                "marriageCertificateDate"  : "marriageCertificateDate",
                                "marriageCertificateZags"  : "marriageCertificateZags",
                                "divorceZags"              : "divorceZags",
                                "divorceDate"              : "divorceDate"]])
        def scenarioDto = createScenarioDto()
        def serviceDescriptor = new ServiceDescriptor()

        when:
        component.process(fieldComponent, scenarioDto, serviceDescriptor)

        then:
        thrown(FormBaseException)

        when:
        fieldComponent.attrs["refs"]["participant1"] = 1
        component.process(fieldComponent, scenarioDto, serviceDescriptor)

        then:
        thrown(FormBaseException)

        when:
        fieldComponent.attrs["refs"]["participant1"] = refMap
        component.process(fieldComponent, scenarioDto, serviceDescriptor)
        def expectedResult = JsonProcessingUtil.toJson(expectedForm())

        then:
        fieldComponent.value == expectedResult
    }

    static def createScenarioDto() {
        new ScenarioDto(applicantAnswers: [
                "userData"   : new ApplicantAnswer(value: JsonProcessingUtil.toJson(USER_DATA_ANSWER)),
                "regAddr"    : new ApplicantAnswer(value: JsonProcessingUtil.toJson(REG_ADDR_ANSWER)),
                "divorceDate": new ApplicantAnswer(value: JsonProcessingUtil.toJson(DIVORCE_DATE)),
                "divorceZags": new ApplicantAnswer(value: JsonProcessingUtil.toJson(DIVORCE_ZAGS))
        ])
    }

    static def expectedForm() {
        FormDto
                .builder()
                .states(List.of(
                        StateDto
                                .builder()
                                .groupName("Данные заявителя")
                                .fields(List.of(
                                        new FieldDto("ФИО", USER_DATA_ANSWER["storedValues"]["lastName"]),
                                        new FieldDto("Дата рождения", USER_DATA_ANSWER["storedValues"]["birthDate"]))
                                ).build(),

                        StateDto
                                .builder()
                                .groupName("Паспорт гражданина РФ")
                                .fields(List.of(
                                        new FieldDto("Серия и номер",
                                                "${USER_DATA_ANSWER["storedValues"]["rfPasportSeries"]} ${USER_DATA_ANSWER["storedValues"]["rfPasportNumber"]}"),
                                        new FieldDto("Дата выдачи", ""),
                                        new FieldDto("Кем выдан", ""),
                                        new FieldDto("Код подразделения", ""),
                                        new FieldDto("Место рождения", ""),
                                        new FieldDto("Гражданство", "Россия"))
                                ).build(),

                        StateDto
                                .builder()
                                .groupName("Место жительства")
                                .fields(List.of(
                                        new FieldDto("Адрес места жительства", REG_ADDR_ANSWER["regAddr"]["fullAddress"]))
                                ).build(),

                        StateDto
                                .builder()
                                .groupName("Дополнительные сведения")
                                .fields(List.of(
                                        new FieldDto("Национальность", ""),
                                        new FieldDto("Образование", ""),
                                        new FieldDto("Первый или повторный брак", ""),
                                        new FieldDto("Фамилия после расторжения брака", ""))
                                ).build(),

                        StateDto
                                .builder()
                                .groupName("Данные о супруге")
                                .fields(List.of(
                                        new FieldDto("ФИО", USER_DATA_ANSWER["storedValues"]["lastName"]),
                                        new FieldDto("Дата рождения", USER_DATA_ANSWER["storedValues"]["birthDate"]))
                                ).build(),

                        StateDto
                                .builder()
                                .groupName("Паспорт гражданина РФ")
                                .fields(List.of(
                                        new FieldDto("Серия и номер",
                                                "${USER_DATA_ANSWER["storedValues"]["rfPasportSeries"]} ${USER_DATA_ANSWER["storedValues"]["rfPasportNumber"]}"),
                                        new FieldDto("Дата выдачи", ""),
                                        new FieldDto("Кем выдан", ""),
                                        new FieldDto("Код подразделения", ""),
                                        new FieldDto("Место рождения", ""),
                                        new FieldDto("Гражданство", "Россия"))
                                ).build(),

                        StateDto
                                .builder()
                                .groupName("Место жительства")
                                .fields(List.of(
                                        new FieldDto("Адрес места жительства",REG_ADDR_ANSWER["regAddr"]["fullAddress"]))
                                ).build(),

                        StateDto
                                .builder()
                                .groupName("Дополнительные сведения")
                                .fields(List.of(
                                        new FieldDto("Национальность", ""),
                                        new FieldDto("Образование", ""),
                                        new FieldDto("Первый или повторный брак", ""),
                                        new FieldDto("Фамилия после расторжения брака", ""))
                                ).build(),

                        StateDto
                                .builder()
                                .groupName("Свидетельство о заключении брака")
                                .fields(List.of(
                                        new FieldDto("Номер записи актовой записи", ""),
                                        new FieldDto("Дата актовой записи", ""),
                                        new FieldDto("Отдел ЗАГС, составивший актовую запись", ""))
                                ).build(),

                        StateDto
                                .builder()
                                .groupName("Дата и место расторжения брака")
                                .fields(List.of(
                                        new FieldDto("Орган ЗАГС", DIVORCE_ZAGS["title"] as String),
                                        new FieldDto("Адрес ЗАГСа", DIVORCE_ZAGS["attributeValues"]["zags_address"] as String),
                                        new FieldDto("Дата и время регистрации", "01.01.2025 в 10:00"))
                                ).build()
                )).storedValues([:]).build()
    }




}
