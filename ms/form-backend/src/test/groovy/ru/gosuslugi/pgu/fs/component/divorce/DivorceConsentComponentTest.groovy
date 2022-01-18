package ru.gosuslugi.pgu.fs.component.divorce

import ru.gosuslugi.pgu.dto.ScenarioDto
import ru.gosuslugi.pgu.dto.descriptor.FieldComponent
import ru.gosuslugi.pgu.dto.descriptor.ServiceDescriptor
import ru.gosuslugi.pgu.dto.descriptor.types.ComponentType
import spock.lang.Ignore
import spock.lang.Specification

// TODO Tests: Переписать тест
@Ignore
class DivorceConsentComponentTest extends Specification {

    def 'Divorce consent helper test' () {
        given:
        def PARTICIPANT_1_ATTR = "participant1";
        def PARTICIPANT_2_ATTR = "participant2";
        def MARRIAGE_CERTIFICATE_NUMBER_ATTR = "marriageCertificateNumber";
        def MARRIAGE_CERTIFICATE_DATE_ATTR = "marriageCertificateDate";
        def MARRIAGE_CERTIFICATE_ZAGS_ATTR = "marriageCertificateZags";
        def DIVORCE_DATE_ATTR = "divorceDate";
        def DIVORCE_ZAGS_ATTR = "divorceZags";

        DivorceConsentComponent helper = new DivorceConsentComponent()
        FieldComponent fieldComponent = new FieldComponent()
        Map<String, Object> attrs = new HashMap<>()
        Map<String, Object> refsMap = new HashMap<>()

        List<String> list = List.of(PARTICIPANT_1_ATTR, PARTICIPANT_2_ATTR, MARRIAGE_CERTIFICATE_NUMBER_ATTR,
                MARRIAGE_CERTIFICATE_DATE_ATTR, MARRIAGE_CERTIFICATE_ZAGS_ATTR, DIVORCE_DATE_ATTR, DIVORCE_ZAGS_ATTR)

        for (String s : list) {
            if (s.equals(PARTICIPANT_1_ATTR) || s.equals(PARTICIPANT_2_ATTR)) {
                Map<String, Object> participantMap = new HashMap<>()
                participantMap.put("regAddr", "")
                participantMap.put("nationality", "")
                participantMap.put("education", "")
                participantMap.put("isFirstMarriage", "")
                participantMap.put("newLastName", "")
                refsMap.put(s, participantMap)
            }else {
                refsMap.put(s, "")
            }


        }

        attrs.put("refs", refsMap)
        fieldComponent.setAttrs(attrs)
        fieldComponent.setType(ComponentType.DivorceConsent)
        ScenarioDto scenarioDto = new ScenarioDto()
        ServiceDescriptor serviceDescriptor = new ServiceDescriptor()
        serviceDescriptor.setApplicationFields(new LinkedList<FieldComponent>())
        expect:
        assert helper.preSetComponentValue(fieldComponent, scenarioDto) == "{\"states\":[{\"groupName\":\"Данные заявителя\",\"fields\":[{\"label\":\"ФИО\",\"value\":\"\"},{\"label\":\"Дата рождения\",\"value\":\"\"}]},{\"groupName\":\"Паспорт гражданина РФ\",\"fields\":[{\"label\":\"Серия и номер\",\"value\":\"\"},{\"label\":\"Дата выдачи\",\"value\":\"\"},{\"label\":\"Кем выдан\",\"value\":\"\"},{\"label\":\"Код подразделения\",\"value\":\"\"},{\"label\":\"Место рождения\",\"value\":\"\"},{\"label\":\"Гражданство\",\"value\":\"Россия\"}]},{\"groupName\":\"Место жительства\",\"fields\":[{\"label\":\"Адрес места жительства\",\"value\":\"\"}]},{\"groupName\":\"Дополнительные сведения\",\"fields\":[{\"label\":\"Национальность\",\"value\":\"\"},{\"label\":\"Образование\",\"value\":\"\"},{\"label\":\"Первый или повторный брак\",\"value\":\"\"},{\"label\":\"Фамилия после расторжения брака\",\"value\":\"\"}]},{\"groupName\":\"Данные о супруге\",\"fields\":[{\"label\":\"ФИО\",\"value\":\"\"},{\"label\":\"Дата рождения\",\"value\":\"\"}]},{\"groupName\":\"Паспорт гражданина РФ\",\"fields\":[{\"label\":\"Серия и номер\",\"value\":\"\"},{\"label\":\"Дата выдачи\",\"value\":\"\"},{\"label\":\"Кем выдан\",\"value\":\"\"},{\"label\":\"Код подразделения\",\"value\":\"\"},{\"label\":\"Место рождения\",\"value\":\"\"},{\"label\":\"Гражданство\",\"value\":\"Россия\"}]},{\"groupName\":\"Место жительства\",\"fields\":[{\"label\":\"Адрес места жительства\",\"value\":\"\"}]},{\"groupName\":\"Дополнительные сведения\",\"fields\":[{\"label\":\"Национальность\",\"value\":\"\"},{\"label\":\"Образование\",\"value\":\"\"},{\"label\":\"Первый или повторный брак\",\"value\":\"\"},{\"label\":\"Фамилия после расторжения брака\",\"value\":\"\"}]},{\"groupName\":\"Свидетельство о заключении брака\",\"fields\":[{\"label\":\"Номер записи актовой записи\",\"value\":\"\"},{\"label\":\"Дата актовой записи\",\"value\":\"\"},{\"label\":\"Отдел ЗАГС, составивший актовую запись\",\"value\":\"\"}]},{\"groupName\":\"Дата и место расторжения брака\",\"fields\":[{\"label\":\"Орган ЗАГС\",\"value\":\"\"},{\"label\":\"Адрес ЗАГСа\",\"value\":\"\"},{\"label\":\"Дата и время регистрации\",\"value\":\"\"}]}],\"storedValues\":{}}"

        assert helper.getType() == ComponentType.DivorceConsent
    }
}
