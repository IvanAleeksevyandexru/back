package ru.gosuslugi.pgu.fs.component.divorce;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.gosuslugi.pgu.dto.ScenarioDto;
import ru.gosuslugi.pgu.fs.common.exception.FormBaseException;
import ru.gosuslugi.pgu.dto.descriptor.FieldComponent;
import ru.gosuslugi.pgu.components.descriptor.attr_factory.FieldComponentAttrsFactory;
import ru.gosuslugi.pgu.dto.descriptor.types.ComponentType;
import ru.gosuslugi.pgu.components.dto.FieldDto;
import ru.gosuslugi.pgu.components.dto.FormDto;
import ru.gosuslugi.pgu.components.dto.StateDto;
import ru.gosuslugi.pgu.fs.common.component.AbstractComponent;
import ru.gosuslugi.pgu.fs.common.component.ComponentResponse;
import ru.gosuslugi.pgu.fs.common.utils.AnswerUtil;
import ru.gosuslugi.pgu.fs.utils.PassportUtil;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Slf4j
@Component
@RequiredArgsConstructor
public class DivorceConsentComponent extends AbstractComponent<FormDto> {

    private final ObjectMapper mapper;

    private static final String EMPTY_VALUE = "";
    private static final DateTimeFormatter MARRIED_FORMATTER = DateTimeFormatter.ofPattern("dd.MM.yyyy");
    private static final DateTimeFormatter DIVORCE_FORMATTER = DateTimeFormatter.ofPattern("dd.MM.yyyy в HH:mm");

    private static final String USER_DATA_ATTR = "userData";
    private static final String PARTICIPANT_1_ATTR = "participant1";
    private static final String PARTICIPANT_2_ATTR = "participant2";
    private static final String MARRIAGE_CERTIFICATE_NUMBER_ATTR = "marriageCertificateNumber";
    private static final String MARRIAGE_CERTIFICATE_DATE_ATTR = "marriageCertificateDate";
    private static final String MARRIAGE_CERTIFICATE_ZAGS_ATTR = "marriageCertificateZags";
    private static final String DIVORCE_DATE_ATTR = "divorceDate";
    private static final String DIVORCE_ZAGS_ATTR = "divorceZags";

    @Override
    public ComponentType getType() {
        return ComponentType.DivorceConsent;
    }

    @Override
    public ComponentResponse<FormDto> getInitialValue(FieldComponent component, ScenarioDto scenarioDto) {
        FieldComponentAttrsFactory attrsFactory = new FieldComponentAttrsFactory(component);
        FormDto form = toForm(scenarioDto, attrsFactory.getRefsMap());
        return ComponentResponse.of(form);
    }

    private FormDto toForm(ScenarioDto scenarioDto, Map<String, Object> refsMap) {
        Map<String, Object> map = AnswerUtil.convertAnswersMapToObjectsMap(mapper, scenarioDto.getApplicantAnswers());

        if (!refsMap.keySet().containsAll(List.of(PARTICIPANT_1_ATTR, PARTICIPANT_2_ATTR, MARRIAGE_CERTIFICATE_NUMBER_ATTR,
                MARRIAGE_CERTIFICATE_DATE_ATTR, MARRIAGE_CERTIFICATE_ZAGS_ATTR, DIVORCE_DATE_ATTR, DIVORCE_ZAGS_ATTR))
                || !(refsMap.get(PARTICIPANT_1_ATTR) instanceof Map)
                || !(refsMap.get(PARTICIPANT_2_ATTR) instanceof Map)) {

            throw new FormBaseException("Поле refs задано неверно");
        }

        // получаем названия компонентов для проставляемых значений
        Map<String, String> participant1 = (Map<String, String>)refsMap.get(PARTICIPANT_1_ATTR);
        Map<String, String> participant2 = (Map<String, String>)refsMap.get(PARTICIPANT_2_ATTR);
        String marriageCertificateNumber = refsMap.get(MARRIAGE_CERTIFICATE_NUMBER_ATTR).toString();
        String marriageCertificateDate = refsMap.get(MARRIAGE_CERTIFICATE_DATE_ATTR).toString();
        String marriageCertificateZags = refsMap.get(MARRIAGE_CERTIFICATE_ZAGS_ATTR).toString();
        String divorceZags = refsMap.get(DIVORCE_ZAGS_ATTR).toString();

        // настраиваем параметры для отображения в форме

        // дата актоыой записи заключения брака
        String marriedData = getValueByPath(map, List.of(marriageCertificateDate));
        if (!marriedData.equals(EMPTY_VALUE)) {
            marriedData = (LocalDateTime.parse(marriedData, DateTimeFormatter.ISO_DATE_TIME)).format(MARRIED_FORMATTER);
        }
        // дата и время регистрации разбрака
        String divorceDate = getValueByPath(map, List.of(refsMap.get(DIVORCE_DATE_ATTR).toString(), "timeSlot", "visitTimeStr"));
        if (!divorceDate.equals(EMPTY_VALUE)) {
            divorceDate = (LocalDateTime.parse(divorceDate, DateTimeFormatter.ISO_DATE_TIME)).format(DIVORCE_FORMATTER);
        }
        // код подразделения для паспорта первого заявителя
        String participant1IssueId = PassportUtil.formatIssueId(
                getStoredValue(map, participant1.get(USER_DATA_ATTR), "rfPasportIssuedById")
        );

        // код подразделения для паспорта второго заявителя
        String participant2IssueId = PassportUtil.formatIssueId(
                getStoredValue(map, participant2.get(USER_DATA_ATTR), "rfPasportIssuedById")
        );

        return FormDto
                .builder()
                .states(List.of(
                        StateDto
                                .builder()
                                .groupName("Данные заявителя")
                                .fields(List.of(
                                        new FieldDto("ФИО", String.join(" ",
                                                getStoredValue(map, participant1.get(USER_DATA_ATTR), "lastName"),
                                                getStoredValue(map, participant1.get(USER_DATA_ATTR), "firstName"),
                                                getStoredValue(map, participant1.get(USER_DATA_ATTR), "middleName")).trim()),
                                        new FieldDto("Дата рождения", getStoredValue(map, participant1.get(USER_DATA_ATTR), "birthDate")))
                                ).build(),

                        StateDto
                                .builder()
                                .groupName("Паспорт гражданина РФ")
                                .fields(List.of(
                                        new FieldDto("Серия и номер", (getStoredValue(map, participant1.get(USER_DATA_ATTR), "rfPasportSeries") + " " +
                                                getStoredValue(map, participant1.get(USER_DATA_ATTR), "rfPasportNumber")).trim()),
                                        new FieldDto("Дата выдачи", getStoredValue(map, participant1.get(USER_DATA_ATTR), "rfPasportIssueDate")),
                                        new FieldDto("Кем выдан", getStoredValue(map, participant1.get(USER_DATA_ATTR), "rfPasportIssuedBy")),
                                        new FieldDto("Код подразделения", participant1IssueId),
                                        new FieldDto("Место рождения", getStoredValue(map, participant1.get(USER_DATA_ATTR), "birthPlace")),
                                        new FieldDto("Гражданство", "Россия"))
                                ).build(),

                        StateDto
                                .builder()
                                .groupName("Место жительства")
                                .fields(List.of(
                                        new FieldDto("Адрес места жительства",
                                                getValueByPath(map, List.of(participant1.get("regAddr"), "regAddr", "fullAddress"))))
                                ).build(),

                        StateDto
                                .builder()
                                .groupName("Дополнительные сведения")
                                .fields(List.of(
                                        new FieldDto("Национальность", getValueByPath(map, List.of(participant1.get("nationality"), "text"))),
                                        new FieldDto("Образование", getValueByPath(map, List.of(participant1.get("education"), "text"))),
                                        new FieldDto("Первый или повторный брак", getValueByPath(map, List.of(participant1.get("isFirstMarriage"), "text"))),
                                        new FieldDto("Фамилия после расторжения брака", getValueByPath(map, List.of(participant1.get("newLastName")))))
                                ).build(),

                        StateDto
                                .builder()
                                .groupName("Данные о супруге")
                                .fields(List.of(
                                        new FieldDto("ФИО", String.join(" ",
                                                getStoredValue(map, participant2.get(USER_DATA_ATTR), "lastName"),
                                                getStoredValue(map, participant2.get(USER_DATA_ATTR), "firstName"),
                                                getStoredValue(map, participant2.get(USER_DATA_ATTR), "middleName")).trim()),
                                        new FieldDto("Дата рождения", getStoredValue(map, participant2.get(USER_DATA_ATTR), "birthDate")))
                                ).build(),

                        StateDto
                                .builder()
                                .groupName("Паспорт гражданина РФ")
                                .fields(List.of(
                                        new FieldDto("Серия и номер", (getStoredValue(map, participant2.get(USER_DATA_ATTR), "rfPasportSeries") + " " +
                                                getStoredValue(map, participant2.get(USER_DATA_ATTR), "rfPasportNumber")).trim()),
                                        new FieldDto("Дата выдачи", getStoredValue(map, participant2.get(USER_DATA_ATTR), "rfPasportIssueDate")),
                                        new FieldDto("Кем выдан", getStoredValue(map, participant2.get(USER_DATA_ATTR), "rfPasportIssuedBy")),
                                        new FieldDto("Код подразделения", participant2IssueId),
                                        new FieldDto("Место рождения", getStoredValue(map, participant2.get(USER_DATA_ATTR), "birthPlace")),
                                        new FieldDto("Гражданство", "Россия"))
                                ).build(),

                        StateDto
                                .builder()
                                .groupName("Место жительства")
                                .fields(List.of(
                                        new FieldDto("Адрес места жительства",
                                                getValueByPath(map, List.of(participant2.get("regAddr"), "regAddr", "fullAddress"))))
                                ).build(),

                        StateDto
                                .builder()
                                .groupName("Дополнительные сведения")
                                .fields(List.of(
                                        new FieldDto("Национальность", getValueByPath(map, List.of(participant2.get("nationality"), "text"))),
                                        new FieldDto("Образование", getValueByPath(map, List.of(participant2.get("education"), "text"))),
                                        new FieldDto("Первый или повторный брак", getValueByPath(map, List.of(participant2.get("isFirstMarriage"), "text"))),
                                        new FieldDto("Фамилия после расторжения брака", getValueByPath(map, List.of(participant2.get("newLastName")))))
                                ).build(),

                        StateDto
                                .builder()
                                .groupName("Свидетельство о заключении брака")
                                .fields(List.of(
                                        new FieldDto("Номер записи актовой записи", getValueByPath(map, List.of(marriageCertificateNumber))),
                                        new FieldDto("Дата актовой записи", marriedData),
                                        new FieldDto("Отдел ЗАГС, составивший актовую запись", getValueByPath(map, List.of(marriageCertificateZags, "text"))))
                                ).build(),

                        StateDto
                                .builder()
                                .groupName("Дата и место расторжения брака")
                                .fields(List.of(
                                        new FieldDto("Орган ЗАГС", getValueByPath(map, List.of(divorceZags, "title"))),
                                        new FieldDto("Адрес ЗАГСа", getValueByPath(map, List.of(divorceZags, "attributeValues", "zags_address"))),
                                        new FieldDto("Дата и время регистрации", divorceDate))
                                ).build()
                        )
                ).storedValues(new HashMap<>()).build();
    }

    private String getStoredValue(Map<String, Object> context, String component, String path) {
        return Objects.isNull(component) ? EMPTY_VALUE : getValueByPath(context, List.of(component, "storedValues", path));
    }

    private String getValueByPath(Map<String, Object> context, List<String> path) {
        switch (path.size()) {
            case 0:     return EMPTY_VALUE;
            case 1:     return context.getOrDefault(path.get(0), EMPTY_VALUE).toString();
            default:
                if (context.containsKey(path.get(0))) {
                    if (EMPTY_VALUE.equals(context.get(path.get(0)).toString())) {
                        return EMPTY_VALUE;
                    }
                    return getValueByPath((Map<String, Object>)context.get(path.get(0)), path.subList(1, path.size()));
                }

                return EMPTY_VALUE;

        }
    }
}
