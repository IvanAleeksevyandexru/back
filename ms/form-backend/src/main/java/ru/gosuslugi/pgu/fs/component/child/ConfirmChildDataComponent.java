package ru.gosuslugi.pgu.fs.component.child;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import ru.atc.carcass.security.rest.model.person.Kids;
import ru.atc.carcass.security.rest.model.person.PersonDoc;
import ru.gosuslugi.pgu.common.core.date.util.DateUtil;
import ru.gosuslugi.pgu.common.esia.search.dto.UserPersonalData;
import ru.gosuslugi.pgu.components.BasicComponentUtil;
import ru.gosuslugi.pgu.components.ComponentAttributes;
import ru.gosuslugi.pgu.components.DateInputComponentUtil;
import ru.gosuslugi.pgu.components.FieldComponentUtil;
import ru.gosuslugi.pgu.components.RegExpUtil;
import ru.gosuslugi.pgu.components.dto.ErrorDto;
import ru.gosuslugi.pgu.components.dto.FieldDto;
import ru.gosuslugi.pgu.components.dto.StateDto;
import ru.gosuslugi.pgu.dto.ApplicantAnswer;
import ru.gosuslugi.pgu.dto.descriptor.FieldComponent;
import ru.gosuslugi.pgu.dto.descriptor.types.ComponentType;
import ru.gosuslugi.pgu.fs.common.component.AbstractCycledComponent;
import ru.gosuslugi.pgu.fs.common.component.ComponentResponse;
import ru.gosuslugi.pgu.fs.common.utils.AnswerUtil;
import ru.gosuslugi.pgu.fs.component.FormDto;
import ru.gosuslugi.pgu.fs.component.child.model.ChildData;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.BiPredicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static ru.gosuslugi.pgu.components.ComponentAttributes.CHILDREN_ID_ATTR;
import static ru.gosuslugi.pgu.components.ComponentAttributes.DESC_ATTR;
import static ru.gosuslugi.pgu.components.ComponentAttributes.MDCL_PLCY_ATTR;
import static ru.gosuslugi.pgu.components.ComponentAttributes.TITLE_ATTR;
import static ru.gosuslugi.pgu.components.ComponentAttributes.WARN_ATTR;
import static ru.gosuslugi.pgu.dto.descriptor.types.ComponentType.ConfirmChildData;
import static ru.gosuslugi.pgu.fs.component.child.ChildAttributes.CHILDREN_ACT_DATE_ATTR;
import static ru.gosuslugi.pgu.fs.component.child.ChildAttributes.CHILDREN_BIRTH_DATE_ATTR;
import static ru.gosuslugi.pgu.fs.component.child.ChildAttributes.CHILDREN_FIRST_NAME_ATTR;
import static ru.gosuslugi.pgu.fs.component.child.ChildAttributes.CHILDREN_GENDER_ATTR;
import static ru.gosuslugi.pgu.fs.component.child.ChildAttributes.CHILDREN_LAST_NAME_ATTR;
import static ru.gosuslugi.pgu.fs.component.child.ChildAttributes.CHILDREN_MIDDLE_NAME_ATTR;
import static ru.gosuslugi.pgu.fs.component.child.ChildAttributes.CHILDREN_OMS_NUMBER_ATTR;
import static ru.gosuslugi.pgu.fs.component.child.ChildAttributes.CHILDREN_OMS_SERIES_ATTR;
import static ru.gosuslugi.pgu.fs.component.child.ChildAttributes.CHILDREN_RF_BIRTH_CERTIFICATE_ACT_NUMBER_ATTR;
import static ru.gosuslugi.pgu.fs.component.child.ChildAttributes.CHILDREN_RF_BIRTH_CERTIFICATE_ISSUED_BY_ATTR;
import static ru.gosuslugi.pgu.fs.component.child.ChildAttributes.CHILDREN_RF_BIRTH_CERTIFICATE_ISSUE_DATE_ATTR;
import static ru.gosuslugi.pgu.fs.component.child.ChildAttributes.CHILDREN_RF_BIRTH_CERTIFICATE_NUMBER_ATTR;
import static ru.gosuslugi.pgu.fs.component.child.ChildAttributes.CHILDREN_RF_BIRTH_CERTIFICATE_SERIES_ATTR;
import static ru.gosuslugi.pgu.fs.component.child.ChildAttributes.CHILDREN_SNILS_ATTR;

/**
 * Компонент подтверждения персональных данных детей
 *
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ConfirmChildDataComponent extends AbstractCycledComponent<FormDto<ChildData>> {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd.MM.yyyy");
    private static final String FIELD_NOT_FOUND_DESC = "Нажмите \"Редактировать\" и укажите в профиле";
    private static final String FIELD_NOT_FOUND_TITLE = "Добавьте данные, чтобы продолжить";
    private static final Map<String, String> ERROR_DESC_FIELD_NAMES = Map.of(
            "Дата", "Дату",
            "Серия", "Серию",
            "Фамилия", "Фамилию"
    );

    private static final String RF_FIRST_NAME_PATTERN = "[а-яА-ЯёЁ\\sIVXLCDM'\\.)(\\\"\\-]{1,100}";
    private static final String RF_LAST_NAME_PATTERN = RF_FIRST_NAME_PATTERN;
    private static final String RF_MIDDLE_NAME_PATTERN = RF_FIRST_NAME_PATTERN;
    private static final String RF_BIRTH_CERTIFICATE_ISUED_BY_PATTERN = "[а-яА-ЯёЁ\\sIVXLCDM0-9'\\.№:/,();\\\"\\-]{1,255}";
    private static final String RF_BIRTH_CERTIFICATE_SERIES_PATTERN = "^[MDCLXIV]{1,6}\\-[а-яА-Я]{2}$";
    private static final String RF_BIRTH_CERTIFICATE_NUMBER_PATTERN = "^[0-9]{6,7}$";
    private static final String FIRST_NAME_NOT_VALID_TITLE = "Проверьте имя";
    private static final String FIRST_NAME_NOT_VALID_DESC = "Поле может содержать только русские буквы";
    private static final String LAST_NAME_NOT_VALID_TITLE = "Проверьте фамилию";
    private static final String LAST_NAME_NOT_VALID_DESC = "Поле может содержать только русские буквы";
    private static final String MIDDLE_NAME_NOT_VALID_TITLE = "Проверьте отчество";
    private static final String MIDDLE_NAME_NOT_VALID_DESC = "Поле может содержать только русские буквы";
    private static final String BIRTH_CERTIFICATE_ISUED_BY_NOT_VALID_TITLE = "Проверьте орган, выдавший свидетельство о рождении";
    private static final String BIRTH_CERTIFICATE_DATA_NOT_VALID_TITLE = "Проверьте указанные данные свидетельства о рождении";
    private static final String BIRTH_CERTIFICATE_TYPE_NOT_VALID_TITLE = "Проверьте тип свидетельства о рождении";
    private static final String BIRTH_CERTIFICATE_ISUED_BY_NOT_VALID_DESC = "Поле может содержать только русские буквы, цифры и символы: «.», «,», «;», «:», «-», «'», «\"», «(», «)», «/», «№»";
    private static final String BIRTH_CERTIFICATE_SERIES_NOT_VALID = "Проверьте серию свидетельства о рождении";
    private static final String BIRTH_CERTIFICATE_NUMBER_NOT_VALID = "Проверьте номер свидетельства о рождении";
    private static final String BIRTH_CERTIFICATE_ISSUED_DATE_NOT_VALID = "Проверьте дату выдачи свидетельства о рождении";
    private static final String BIRTH_CERTIFICATE_TYPE_NOT_VALID = "Проверьте верно ли указан/указан ли тип свидетельства о рождении";
    public static final String FOREIGN_BRTH_CERT_TYPE = "FID_BRTH_CERT";
    public static final String RF_BRTH_CERT_TYPE = "RF_BRTH_CERT";
    public static final String BRTH_CERT_TYPE = "BRTH_CERT";

    private final UserPersonalData userPersonalData;

    @Override
    public ComponentType getType() {
        return ConfirmChildData;
    }

    /**
     * Начальная инициализация полей из ЕСИА
     *
     * @param component описание компонента ConfirmChildData из JSON
     * @param externalData внешние данные (из ЕСИА)
     * @return ComponentResponse<FormDto<ChildData>>
     */
    @Override
    public ComponentResponse<FormDto<ChildData>> getCycledInitialValue(FieldComponent component, Map<String, Object> externalData) {
        List<StateDto> states = new ArrayList<>();
        ChildData childData = new ChildData();
        String childId = externalData.getOrDefault(CHILDREN_ID_ATTR, "").toString();

        if (!StringUtils.isEmpty(childId)) {
            Optional<Kids> kids = userPersonalData.getKids().stream()
                    .filter(kid -> childId.equals(kid.getId()))
                    .findFirst();
            if (kids.isPresent()) {
                externalData = kidsToExternalData(kids.get());
            }
        }

        Map<String, FieldDto> fieldsComponent = BasicComponentUtil.getComponentFields(component);
        FieldsComponent filledFields = new FieldsComponent();

        // Группа - Основные данные (ФИО, д.р., СНИЛС)
        fillBaseData(fieldsComponent, externalData, childData, filledFields);
        states.add(StateDto.builder()
                .groupName((Stream.of(childData.getLastName(), childData.getFirstName(), childData.getMiddleName())
                        .filter(Objects::nonNull)
                        .collect(Collectors.joining(" "))))
                .fields(filledFields.filledFields)
                .build());

        // Группа - Свидетельство о рождении
        if (fieldsComponent.containsKey(CHILDREN_RF_BIRTH_CERTIFICATE_SERIES_ATTR.name) && fieldsComponent.containsKey(CHILDREN_RF_BIRTH_CERTIFICATE_NUMBER_ATTR.name)) {
            if (StringUtils.hasText(childData.getDocType()) && childData.getDocType().equals(FOREIGN_BRTH_CERT_TYPE)) {
                FieldDto seriesFieldDto = fieldsComponent.get(CHILDREN_RF_BIRTH_CERTIFICATE_SERIES_ATTR.name);
                seriesFieldDto.setRequired(false);
            }
            filledFields.filledFields.clear();
            fillBirthCertificateData(fieldsComponent, externalData, childData, filledFields);
            if (!filledFields.filledFields.isEmpty()) {
                states.add(StateDto.builder()
                        .groupName("Свидетельство о рождении")
                        .fields(filledFields.filledFields)
                        .build());
            }
        }

        List<ErrorDto> errors = new ArrayList<>();
        // Обязательность заполнения
        ErrorDto errorRequired = getErrorDtoForEmptyRequired(filledFields.emptyRequiredFields);
        if (errorRequired!=null) errors.add(errorRequired);
        // Валидация полей
        errors.addAll( getValidationErrors(fieldsComponent.keySet(), childData, fieldsComponent) );

        // Предупреждения
        List<Map<String, String>> warns = (List<Map<String, String>>) component.getAttrs().get(WARN_ATTR);
        if (errors.isEmpty() && !CollectionUtils.isEmpty(warns)) {
            for (Map<String, String> it : warns) {
                errors.add(new ErrorDto(it.get(TITLE_ATTR), it.get(DESC_ATTR)));
            }
        }

        return ComponentResponse.of(FormDto.<ChildData>builder()
                .states(states)
                .storedValues(childData)
                .errors(errors)
                .build()
        );
    }

    private Map<String, Object> kidsToExternalData(Kids kid) {
        Map<String, Object> externalData = new HashMap<>();
        userPersonalData.fillKidsOms();
        externalData.put(CHILDREN_FIRST_NAME_ATTR.name, kid.getFirstName());
        externalData.put(CHILDREN_LAST_NAME_ATTR.name, kid.getLastName());
        externalData.put(CHILDREN_MIDDLE_NAME_ATTR.name, Optional.ofNullable(kid.getMiddleName()).orElse(""));
        externalData.put(CHILDREN_BIRTH_DATE_ATTR.name, DateUtil.toOffsetDateTimeString(kid.getBirthDate(), DateUtil.ESIA_DATE_FORMAT));
        externalData.put(CHILDREN_GENDER_ATTR.name, kid.getGender());
        externalData.put(CHILDREN_SNILS_ATTR.name, Optional.ofNullable(kid.getSnils()).orElse(""));
        Optional<PersonDoc> brthCertOptional = kid.getDocuments().getDocs().stream().filter(x -> x.getType().contains("BRTH_CERT")).findFirst();
        if (brthCertOptional.isPresent()) {
            PersonDoc birthCert = brthCertOptional.get();
            externalData.put(CHILDREN_RF_BIRTH_CERTIFICATE_SERIES_ATTR.name, birthCert.getSeries());
            externalData.put(CHILDREN_RF_BIRTH_CERTIFICATE_NUMBER_ATTR.name, birthCert.getNumber());
            externalData.put(CHILDREN_RF_BIRTH_CERTIFICATE_ACT_NUMBER_ATTR.name, birthCert.getActNo() == null ? "-" : birthCert.getActNo());
            externalData.put(CHILDREN_RF_BIRTH_CERTIFICATE_ISSUE_DATE_ATTR.name, DateUtil.toOffsetDateTime(birthCert.getIssueDate(), DateUtil.ESIA_DATE_FORMAT));
            externalData.put(CHILDREN_RF_BIRTH_CERTIFICATE_ISSUED_BY_ATTR.name, birthCert.getIssuedBy());
            externalData.put(CHILDREN_ACT_DATE_ATTR.name, birthCert.getActDate());
        }
        Optional<PersonDoc> omsOptional = kid.getDocuments().getDocs().stream().filter(x -> x.getType().equals(MDCL_PLCY_ATTR)).findFirst();
        if (omsOptional.isPresent()) {
            PersonDoc oms = omsOptional.get();
            externalData.put(CHILDREN_OMS_SERIES_ATTR.name, oms.getSeries());
            externalData.put(CHILDREN_OMS_NUMBER_ATTR.name, oms.getNumber());
        }
        return externalData;
    }

    /**
     * Заполняем поля childData и filledFields групппы "Основные данные"
     *
     * @param fieldsComponent описание полей компонента из JSON
     * @param externalData данные из ЕСИА
     * @param childData данные компонента
     * @param filledFields заполняемые поля
     */
    private void fillBaseData(Map<String, FieldDto> fieldsComponent, Map<String, Object> externalData, ChildData childData, FieldsComponent filledFields) {
        if (fieldsComponent.containsKey(CHILDREN_FIRST_NAME_ATTR.name)) {
            String valueFirstName = externalData.getOrDefault(CHILDREN_FIRST_NAME_ATTR.name, "").toString();
            fieldsComponent.get(CHILDREN_FIRST_NAME_ATTR.name).setValue(valueFirstName);
            childData.setFirstName(valueFirstName);
        }

        if (fieldsComponent.containsKey(CHILDREN_LAST_NAME_ATTR.name)) {
            String valueLastName = externalData.getOrDefault(CHILDREN_LAST_NAME_ATTR.name, "").toString();
            fieldsComponent.get(CHILDREN_LAST_NAME_ATTR.name).setValue(valueLastName);
            childData.setLastName(valueLastName);
        }

        if (fieldsComponent.containsKey(CHILDREN_MIDDLE_NAME_ATTR.name)) {
            String valueMiddleName = externalData.getOrDefault(CHILDREN_MIDDLE_NAME_ATTR.name, "").toString();
            fieldsComponent.get(CHILDREN_MIDDLE_NAME_ATTR.name).setValue(valueMiddleName);
            childData.setMiddleName(valueMiddleName);
        }

        if (fieldsComponent.containsKey(CHILDREN_BIRTH_DATE_ATTR.name)) {
            var valueDateBirth = externalData.getOrDefault(CHILDREN_BIRTH_DATE_ATTR.name, "").toString();
            fieldsComponent.get(CHILDREN_BIRTH_DATE_ATTR.name).setValue(valueDateBirth);
            try {
                var dateString = LocalDateTime.parse(
                        fieldsComponent.get(CHILDREN_BIRTH_DATE_ATTR.name).getValue(),
                        DateTimeFormatter.ISO_DATE_TIME).format(DATE_FORMATTER);
                setFieldValue(fieldsComponent.get(CHILDREN_BIRTH_DATE_ATTR.name), dateString, filledFields);
                childData.setBirthDate(dateString);
            } catch (DateTimeParseException e) {
                childData.setBirthDate("");
                setFieldValue(fieldsComponent.get(CHILDREN_BIRTH_DATE_ATTR.name), "", filledFields);
            }
        }

        if (fieldsComponent.containsKey(CHILDREN_GENDER_ATTR.name)) {
            String valueGender = externalData.getOrDefault(CHILDREN_GENDER_ATTR.name, "").toString();
            fieldsComponent.get(CHILDREN_GENDER_ATTR.name).setValue(valueGender);
            setFieldValue(fieldsComponent.get(CHILDREN_GENDER_ATTR.name), valueGender, filledFields);
            childData.setGender(valueGender);
        }

        if (fieldsComponent.containsKey(CHILDREN_SNILS_ATTR.name)) {
            String valueSnils = externalData.getOrDefault(CHILDREN_SNILS_ATTR.name, "").toString();
            fieldsComponent.get(CHILDREN_SNILS_ATTR.name).setValue(valueSnils);
            setFieldValue(fieldsComponent.get(CHILDREN_SNILS_ATTR.name), valueSnils, filledFields);
            childData.setSnils(valueSnils);
        }

        if (fieldsComponent.containsKey(CHILDREN_OMS_NUMBER_ATTR.name)) {
            String valueOmsNumber = externalData.getOrDefault(CHILDREN_OMS_NUMBER_ATTR.name, "").toString();
            fieldsComponent.get(CHILDREN_OMS_NUMBER_ATTR.name).setValue(valueOmsNumber);
            setFieldValue(fieldsComponent.get(CHILDREN_OMS_NUMBER_ATTR.name), valueOmsNumber, filledFields);
            childData.setOmsNumber(valueOmsNumber);
        }

        if (fieldsComponent.containsKey(CHILDREN_OMS_SERIES_ATTR.name)) {
            String valueOmsSeries = externalData.getOrDefault(CHILDREN_OMS_SERIES_ATTR.name, "").toString();
            fieldsComponent.get(CHILDREN_OMS_SERIES_ATTR.name).setValue(valueOmsSeries);
            setFieldValue(fieldsComponent.get(CHILDREN_OMS_SERIES_ATTR.name), valueOmsSeries, filledFields);
            childData.setOmsSeries(valueOmsSeries);
        }

        childData.setDocType(externalData.getOrDefault("type","").toString());
    }

    /**
     * Заполняем поля childData и filledFields группы "Свидетельство о рождении"
     *
     * @param fieldsComponent описание полей компонента из JSON
     * @param externalData данные из ЕСИА
     * @param childData данные компонента
     * @param filledFields заполняемые поля
     */
    private void fillBirthCertificateData(Map<String, FieldDto> fieldsComponent, Map<String, Object> externalData, ChildData childData, FieldsComponent filledFields) {
        String seriesCertificate = externalData.getOrDefault(CHILDREN_RF_BIRTH_CERTIFICATE_SERIES_ATTR.name, "").toString();
        childData.setRfBirthCertificateSeries(seriesCertificate);

        String numberCertificate = externalData.getOrDefault(CHILDREN_RF_BIRTH_CERTIFICATE_NUMBER_ATTR.name, "").toString();
        childData.setRfBirthCertificateNumber(numberCertificate);

        setFieldValueSeriesAndNumber(fieldsComponent.get(CHILDREN_RF_BIRTH_CERTIFICATE_SERIES_ATTR.name),
                fieldsComponent.get(CHILDREN_RF_BIRTH_CERTIFICATE_NUMBER_ATTR.name),
                seriesCertificate,
                numberCertificate,
                filledFields);

        if (fieldsComponent.containsKey(CHILDREN_RF_BIRTH_CERTIFICATE_ACT_NUMBER_ATTR.name)) {
            String valueActNumber = externalData.getOrDefault(CHILDREN_RF_BIRTH_CERTIFICATE_ACT_NUMBER_ATTR.name, "").toString();
            setFieldValue(fieldsComponent.get(CHILDREN_RF_BIRTH_CERTIFICATE_ACT_NUMBER_ATTR.name), valueActNumber, filledFields);
            childData.setRfBirthCertificateActNumber(valueActNumber);
        }

        if (fieldsComponent.containsKey(CHILDREN_RF_BIRTH_CERTIFICATE_ISSUE_DATE_ATTR.name)) {
            var valueBirthCertIssueDate = externalData.getOrDefault(CHILDREN_RF_BIRTH_CERTIFICATE_ISSUE_DATE_ATTR.name, "").toString();
            fieldsComponent.get(CHILDREN_RF_BIRTH_CERTIFICATE_ISSUE_DATE_ATTR.name).setValue(valueBirthCertIssueDate);
            try {
                var dateString = LocalDateTime.parse(
                        fieldsComponent.get(CHILDREN_RF_BIRTH_CERTIFICATE_ISSUE_DATE_ATTR.name).getValue(),
                        DateTimeFormatter.ISO_DATE_TIME).format(DATE_FORMATTER);
                setFieldValue(fieldsComponent.get(CHILDREN_RF_BIRTH_CERTIFICATE_ISSUE_DATE_ATTR.name), dateString, filledFields);
                childData.setRfBirthCertificateIssueDate(dateString);
            } catch (DateTimeParseException e) {
                childData.setRfBirthCertificateIssueDate("");
                setFieldValue(fieldsComponent.get(CHILDREN_RF_BIRTH_CERTIFICATE_ISSUE_DATE_ATTR.name), "", filledFields);
            }
        }

        if (fieldsComponent.containsKey(CHILDREN_RF_BIRTH_CERTIFICATE_ISSUED_BY_ATTR.name)) {
            String valueIssuedBy = externalData.getOrDefault(CHILDREN_RF_BIRTH_CERTIFICATE_ISSUED_BY_ATTR.name, "").toString();
            setFieldValue(fieldsComponent.get(CHILDREN_RF_BIRTH_CERTIFICATE_ISSUED_BY_ATTR.name), valueIssuedBy, filledFields);
            childData.setRfBirthCertificateIssuedBy(valueIssuedBy);
        }

        if (fieldsComponent.containsKey(CHILDREN_ACT_DATE_ATTR.name)) {
            String valueActDate = externalData.getOrDefault(CHILDREN_ACT_DATE_ATTR.name, "").toString();
            setFieldValue(fieldsComponent.get(CHILDREN_ACT_DATE_ATTR.name), valueActDate, filledFields);
            childData.setActDate(valueActDate);
        }
    }

    /**
     * Валидация ответа от front
     *
     * @param incorrectAnswers мапа для хранения ошибок валидации (ключ - экран, значение - сообщение об ошибке)
     * @param entry экран, ответ
     * @param fieldComponent экземпляр компонента валидации
     */
    @Override
    protected void validateAfterSubmit(Map<String,String> incorrectAnswers, Map.Entry<String, ApplicantAnswer> entry, FieldComponent fieldComponent) {
        Map<String, FieldDto> fieldsDefinition = BasicComponentUtil.getFieldNameToFieldDtoMap(fieldComponent);

        Map<Object, Object> answerMap = AnswerUtil.toMap(entry, true);
        if (!answerMap.containsKey("storedValues")) return;
        val valueMap = (Map<String, Object>)answerMap.get("storedValues");

        List<String> errors = new ArrayList<>();
        fieldsDefinition.forEach((name, field) -> {
            String value = valueMap.getOrDefault(name, "").toString();
            if ( field.isRequired() && !StringUtils.hasText(value) ) {
                errors.add(name);
            }
        });

        if (!errors.isEmpty()) {
            ErrorDto errorRequired = getErrorDtoForEmptyRequired(errors);
            if (errorRequired!=null) {
                String errorString = errorRequired.getTitle() + " " + errorRequired.getDesc();
                incorrectAnswers.put(entry.getKey(), errorString);
            }
        }

        try {
            Map<String, FieldDto> fieldsComponent = BasicComponentUtil.getComponentFields(fieldComponent);
            ChildData childData = objectMapper.convertValue(valueMap, ChildData.class);
            List<ErrorDto> errorsValidation = getValidationErrors(fieldsDefinition.keySet(), childData, fieldsComponent);
            errorsValidation.forEach(err -> incorrectAnswers.put(entry.getKey(), String.format("%s. %s", err.getTitle(), err.getDesc())));
        } catch (IllegalArgumentException e) {
            incorrectAnswers.put(entry.getKey(), "Ошибка процесса проверки на корректность заполнения полей.");
            log.warn(String.format("Ошибка преобразования во внутреннее представление: %s", valueMap));
        }
    }

    /**
     * Установка значения поля с проверкой необходимости заполнения
     *
     * @param fieldDTO Описание поля
     * @param value Устанавливаемое значение
     * @param fields Куда записываем
     */
    private void setFieldValue(FieldDto fieldDTO, String value, FieldsComponent fields) {
        if (!fieldDTO.isRequired() || StringUtils.hasText(value)) {
            fields.filledFields.add(new FieldDto(fieldDTO.getLabel(), value));
        } else {
            fields.emptyRequiredFields.add(fieldDTO.getLabel());
        }
    }

    /**
     * Установка значения составного поля "Серия и номер свидетельства о рождении" с проверкой необходимости заполнения
     *
     * @param seriesFieldDto описание поля "Серия свидетельства о рождении"
     * @param numberFieldDto описание поля "Номер свидетельства о рождении"
     * @param series серия свидетельства о рождении
     * @param number номер свидетельства о рождении
     * @param fields Куда записываем
     */
    private void setFieldValueSeriesAndNumber(FieldDto seriesFieldDto, FieldDto numberFieldDto, String series, String number, FieldsComponent fields) {
        if (StringUtils.hasText(series) || StringUtils.hasText(number)) {
            fields.filledFields.add(new FieldDto("Серия и номер", String.format("%s %s", series, number)));
        }
        if (seriesFieldDto.isRequired() && !StringUtils.hasText(series) && numberFieldDto.isRequired() && !StringUtils.hasText(number)) {
            fields.emptyRequiredFields.add("Серию и Номер свидетельства о рождении");
        } else {
            if (!StringUtils.hasText(series) && seriesFieldDto.isRequired()) {
                fields.emptyRequiredFields.add("Серию свидетельства о рождении");
            }
            if (!StringUtils.hasText(number) && numberFieldDto.isRequired()) {
                fields.emptyRequiredFields.add("Номер свидетельства о рождении");
            }
        }
    }

    /**
     * Получаем DTO ошибки для списка незаполнненых обязательных полей.
     *
     * @param fields список обязательных полей с не заполненным значением
     * @return DTO ошибки
     */
    private ErrorDto getErrorDtoForEmptyRequired(List<String> fields) {
        if (!fields.isEmpty()) {
            String desc = FIELD_NOT_FOUND_DESC;
            List<String> listEmptyField = null;
            if (fields.size() == 1) {
                desc = desc + " " + fields.get(0);
                for (Map.Entry<String, String> errDesc : ERROR_DESC_FIELD_NAMES.entrySet())
                    desc = desc.replace(errDesc.getKey(), errDesc.getValue());
            } else {
                desc = desc + ":";
                // делаем такую манипуляцию из-за чудес дизайна компонента
                listEmptyField = Arrays.asList((String.join(",&", fields) + ".").split("&"));
            }
            return new ErrorDto(ErrorDto.TYPE.ERROR, FIELD_NOT_FOUND_TITLE, desc, listEmptyField);
        }
        return null;
    }

    /**
     * Заполняем ошибки от проверки на обязательность заполнения и дополнительной валидации.
     *
     * @param namesField список имен всех полей компонента
     * @param childData проверяемые данные компонента
     * @return список сформированных DTO ошибок
     */
    private List<ErrorDto> getValidationErrors(Set<String> namesField, ChildData childData, Map<String, FieldDto> fieldsComponent) {
        List<ErrorDto> errors = new ArrayList<>();
        String docType = childData.getDocType();
        if (StringUtils.hasText(docType) && (docType.equals(RF_BRTH_CERT_TYPE) || docType.equals(BRTH_CERT_TYPE))) {
            errors.addAll(getValidationErrorsForRfBirthCert(namesField, childData, fieldsComponent));
        } else {
            errors.addAll(getValidationErrorsForForeignBirthCert(childData, fieldsComponent));
        }

        // Дата выдачи свидетельства о рождении меньше даты рождения, либо выдана позже текущей даты
        if (namesField.contains(CHILDREN_RF_BIRTH_CERTIFICATE_ISSUE_DATE_ATTR.name) && !StringUtils.isEmpty(childData.getRfBirthCertificateIssueDate())) {

            SimpleDateFormat inputFormat = new SimpleDateFormat("dd.MM.yyyy");
            SimpleDateFormat outputFormat = new SimpleDateFormat("yyyy-MM-dd");

            try {
                if (!StringUtils.isEmpty(childData.getBirthDate()) && !StringUtils.isEmpty(childData.getRfBirthCertificateIssueDate())) {
                    LocalDate certificateIssueDate = LocalDate.parse(outputFormat.format(inputFormat.parse(childData.getRfBirthCertificateIssueDate())));
                    LocalDate birthDate = LocalDate.parse(outputFormat.format(inputFormat.parse(childData.getBirthDate())));
                    long daysBetweenIssued = ChronoUnit.DAYS.between(certificateIssueDate, LocalDate.now());
                    long daysBetweenBirthdayAndIssuedDate = ChronoUnit.DAYS.between(birthDate, certificateIssueDate);

                    if (daysBetweenIssued < 0 || daysBetweenBirthdayAndIssuedDate < 0) {
                        errors.add(new ErrorDto(BIRTH_CERTIFICATE_DATA_NOT_VALID_TITLE, BIRTH_CERTIFICATE_ISSUED_DATE_NOT_VALID));
                    }
                }
            } catch (ParseException e) {
                log.error("Ошибка обработки даты указанной в свидетельстве о рождении:\t", e);
            }
        }

        if (!childData.getDocType().contains("BRTH_CERT")) { // на проде помимо RF_BRTH_CERT и FID_BRTH_CERT может быть ещё и просто "BRTH_CERT" тип.
            errors.add(new ErrorDto(BIRTH_CERTIFICATE_TYPE_NOT_VALID_TITLE, BIRTH_CERTIFICATE_TYPE_NOT_VALID));
        }

        if (fieldsComponent.containsKey(CHILDREN_BIRTH_DATE_ATTR.name)) {
            validateByJsonRule(fieldsComponent.get(CHILDREN_BIRTH_DATE_ATTR.name), errors);
        }
        if (fieldsComponent.containsKey(CHILDREN_SNILS_ATTR.name)) {
            validateByJsonRule(fieldsComponent.get(CHILDREN_SNILS_ATTR.name), errors);
        }
        if (fieldsComponent.containsKey(CHILDREN_OMS_NUMBER_ATTR.name)) {
            validateByJsonRule(fieldsComponent.get(CHILDREN_OMS_NUMBER_ATTR.name), errors);
        }
        if (fieldsComponent.containsKey(CHILDREN_OMS_SERIES_ATTR.name)) {
            validateByJsonRule(fieldsComponent.get(CHILDREN_OMS_SERIES_ATTR.name), errors);
        }

        return errors;
    }

    private void validateField(FieldDto field, List<ErrorDto> errors, String value, String errorMsg, String errorDesc) {
        Map<String, Object> attrs = field.getAttrs();
        if (!attrs.containsKey(FieldComponentUtil.VALIDATION_ARRAY_KEY)) {
            attrs.put(FieldComponentUtil.VALIDATION_ARRAY_KEY, List.of(Map.of(
                    ComponentAttributes.TYPE_ATTR, "RegExp",
                    ComponentAttributes.VALUE_ATTR, value,
                    ComponentAttributes.ERROR_MSG_ATTR, errorMsg,
                    ComponentAttributes.ERROR_DESC_ATTR, errorDesc
            )));
        }
        validateByJsonRule(field, errors);
    }

    private List<ErrorDto> getValidationErrorsForRfBirthCert(Set<String> namesField, ChildData childData, Map<String, FieldDto> fieldsComponent) {
        List<ErrorDto> errors = new ArrayList<>();

        BiPredicate<String, String> checkDataPredicate =
                (dataName, dataValue) -> namesField.contains(dataName) && !StringUtils.isEmpty(dataValue) && fieldsComponent.containsKey(dataName);

        // Имя не соответствует паттерну
        if (checkDataPredicate.test(CHILDREN_FIRST_NAME_ATTR.name, childData.getFirstName())) {
            validateField(fieldsComponent.get(CHILDREN_FIRST_NAME_ATTR.name), errors,
                    RF_FIRST_NAME_PATTERN, FIRST_NAME_NOT_VALID_TITLE, FIRST_NAME_NOT_VALID_DESC);
        }

        // Фамилия не соответствует паттерну
        if (checkDataPredicate.test(CHILDREN_LAST_NAME_ATTR.name, childData.getLastName())) {
            validateField(fieldsComponent.get(CHILDREN_LAST_NAME_ATTR.name), errors,
                    RF_LAST_NAME_PATTERN, LAST_NAME_NOT_VALID_TITLE, LAST_NAME_NOT_VALID_DESC);
        }

        // Отчество не соответствует паттерну
        if (checkDataPredicate.test(CHILDREN_MIDDLE_NAME_ATTR.name, childData.getMiddleName())) {
            validateField(fieldsComponent.get(CHILDREN_MIDDLE_NAME_ATTR.name), errors,
                    RF_MIDDLE_NAME_PATTERN, MIDDLE_NAME_NOT_VALID_TITLE, MIDDLE_NAME_NOT_VALID_DESC);
        }

        // Наименование органа, выдавшего свидетельство о рождении не соответствует паттерну
        if (checkDataPredicate.test(CHILDREN_RF_BIRTH_CERTIFICATE_ISSUED_BY_ATTR.name, childData.getRfBirthCertificateIssuedBy())) {
            validateByJsonRule(fieldsComponent.get(CHILDREN_RF_BIRTH_CERTIFICATE_ISSUED_BY_ATTR.name), childData.getRfBirthCertificateIssuedBy(), errors);
        }

        // Серия свидетельства о рождении не соответствует паттерну
        if (namesField.contains(CHILDREN_RF_BIRTH_CERTIFICATE_SERIES_ATTR.name)) {
            validateByJsonRule(fieldsComponent.get(CHILDREN_RF_BIRTH_CERTIFICATE_SERIES_ATTR.name), childData.getRfBirthCertificateSeries(), errors);
        }

        // Номер свидетельства о рождении не соответствует паттерну
        if (checkDataPredicate.test(CHILDREN_RF_BIRTH_CERTIFICATE_NUMBER_ATTR.name, childData.getRfBirthCertificateNumber())) {
            validateByJsonRule(fieldsComponent.get(CHILDREN_RF_BIRTH_CERTIFICATE_NUMBER_ATTR.name), childData.getRfBirthCertificateNumber(), errors);
        }

        return errors;
    }

    private List<ErrorDto> getValidationErrorsForForeignBirthCert(ChildData childData, Map<String, FieldDto> fieldsComponent) {
        List<ErrorDto> errors = new ArrayList<>();
        if (fieldsComponent.containsKey(CHILDREN_FIRST_NAME_ATTR.name)) {
            validateByJsonRule(fieldsComponent.get(CHILDREN_FIRST_NAME_ATTR.name), errors);
        }
        if (fieldsComponent.containsKey(CHILDREN_LAST_NAME_ATTR.name)) {
            validateByJsonRule(fieldsComponent.get(CHILDREN_LAST_NAME_ATTR.name), errors);
        }
        if (fieldsComponent.containsKey(CHILDREN_MIDDLE_NAME_ATTR.name)) {
            validateByJsonRule(fieldsComponent.get(CHILDREN_MIDDLE_NAME_ATTR.name), errors);
        }
        if (fieldsComponent.containsKey("foreignBirthCertificateSeries")) {
            validateByJsonRule(fieldsComponent.get("foreignBirthCertificateSeries"), childData.getRfBirthCertificateSeries(), errors);
        }
        if (fieldsComponent.containsKey("foreignBirthCertificateNumber")) {
            validateByJsonRule(fieldsComponent.get("foreignBirthCertificateNumber"), childData.getRfBirthCertificateNumber(), errors);
        }
        return errors;
    }

    /**
     * Валидация по правилам из json'a
     * @param fieldDto Валидируемый field
     * @param errors Куда пишем ошибку валидации
     */
    private void validateByJsonRule(FieldDto fieldDto, List<ErrorDto> errors) {
        validateByJsonRule(fieldDto, fieldDto.getValue(), errors);
    }

    private void validateByJsonRule(FieldDto fieldDto, String fieldValue, List<ErrorDto> errors) {
        if (fieldDto.getAttrs().containsKey(FieldComponentUtil.VALIDATION_ARRAY_KEY) && StringUtils.hasText(fieldValue)) {

            // RegExp
            errors.addAll(
                    ((List<Map<String, String>>) fieldDto.getAttrs().get(FieldComponentUtil.VALIDATION_ARRAY_KEY))
                            .stream()
                            .filter(v -> RegExpUtil.REG_EXP_TYPE.equalsIgnoreCase(v.get(ComponentAttributes.TYPE_ATTR)))
                            .filter(v -> !fieldValue.matches(v.get(ComponentAttributes.VALUE_ATTR)))
                            .map(v -> new ErrorDto(v.get(ComponentAttributes.ERROR_MSG_ATTR), v.get(ComponentAttributes.ERROR_DESC_ATTR)))
                            .collect(Collectors.toList())
            );

            // Dates
            var fieldComponent = new FieldComponent();
            fieldComponent.setAttrs(fieldDto.getAttrs());
            var validationList = DateInputComponentUtil.getValidationDateList(fieldComponent);
            if (!validationList.isEmpty()) {
                errors.addAll(
                        DateInputComponentUtil.validate(validationList, fieldComponent, fieldDto.getValue())
                                .stream()
                                .map(error -> new ErrorDto(error, ""))
                                .collect(Collectors.toList()));
            }
        }
    }

    /**
     * Класс для "фасовки" полей
     */
    private class FieldsComponent {
        /**
         * Имена полей обязательных для заполнения, но с отсутствующим значением. Выводится в ошибки
         */
        private final List<String> emptyRequiredFields = new ArrayList<>();
        /**
         * Поля для заполнения.
         */
        private final List<FieldDto> filledFields = new ArrayList<>();
    }
}
