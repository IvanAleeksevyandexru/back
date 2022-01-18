package ru.gosuslugi.pgu.fs.component.confirm;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import ru.atc.carcass.security.rest.model.person.Person;
import ru.atc.carcass.security.rest.model.person.PersonDoc;
import ru.gosuslugi.pgu.common.core.date.util.DateUtil;
import ru.gosuslugi.pgu.common.core.json.JsonProcessingUtil;
import ru.gosuslugi.pgu.common.esia.search.dto.UserPersonalData;
import ru.gosuslugi.pgu.components.*;
import ru.gosuslugi.pgu.components.descriptor.types.DocInputField;
import ru.gosuslugi.pgu.components.dto.ErrorDto;
import ru.gosuslugi.pgu.components.dto.FieldDto;
import ru.gosuslugi.pgu.components.dto.StateDto;
import ru.gosuslugi.pgu.dto.ScenarioDto;
import ru.gosuslugi.pgu.dto.descriptor.FieldComponent;
import ru.gosuslugi.pgu.dto.descriptor.types.ComponentType;
import ru.gosuslugi.pgu.fs.common.component.AbstractComponent;
import ru.gosuslugi.pgu.fs.common.component.ComponentResponse;
import ru.gosuslugi.pgu.fs.component.FormDto;
import ru.gosuslugi.pgu.fs.component.confirm.model.ConfirmPersonalUserData;
import ru.gosuslugi.pgu.fs.component.confirm.model.ConfirmPersonalUserDataErrorType;
import ru.gosuslugi.pgu.fs.utils.PassportUtil;

import java.time.LocalDate;
import java.util.*;
import java.util.function.BiPredicate;
import java.util.function.Function;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static ru.gosuslugi.pgu.components.ComponentAttributes.*;
import static ru.gosuslugi.pgu.components.FieldComponentUtil.FIELDS_KEY;

/**
 * Компонент показывает персональные данные пользователя из ЕСИА
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class ConfirmPersonalUserDataComponent extends AbstractComponent<FormDto<ConfirmPersonalUserData>> {

    private static final ObjectMapper objectMapper = JsonProcessingUtil.getObjectMapper();
    private final UserPersonalData userPersonalData;

    private static final String SNILS_FIELD_LABEL = "СНИЛС";
    private static final String INN_FIELD_LABEL = "ИНН";
    private static final String ISSUE_DATE_FIELD_LABEL = "Дата выдачи";
    private static final String ISSUE_BY_FIELD_LABEL = "Кем выдан";
    private static final String ISSUE_BY_ID_FIELD_LABEL = "Код подразделения";
    private static final String CITIZENSHIP_FIELD_LABEL = "Гражданство";
    private static final String BIRTH_PLACE_FIELD_LABEL = "Место рождения";
    private static final String EXPIRY_DATE_FIELD_LABEL = "Срок действия загранпаспорта";
    private static final String LAST_NAME_FIELD_LABEL = "Фамилия";
    private static final String FIRST_NAME_FIELD_LABEL = "Имя";

    private static final String PASSPORT_NOT_FOUND_TITLE = "Нет паспортных данных";
    private static final String PASSPORT_NOT_FOUND_DESC = "Вы не указали паспортные данные в профиле. Добавьте их, чтобы продолжить заполнять заявление";
    private static final String FRGN_PASSPORT_EXPIRE_DATE_TITLE = "Истёк срок действия загранпаспорта";
    private static final String FRGN_PASSPORT_EXPIRE_DATE_DESC = "Укажите в профиле актуальные данные по документу, удостоверяющему личность, чтобы продолжить заполнять заявление";
    private static final String RF_PASSPORT_ISSUE_DATE_TITLE = "Проверьте дату выдачи паспорта";
    private static final String RF_PASSPORT_ISSUE_DATE_DESC = "Паспорт выдаётся с 14 лет. У вас указана дата выдачи раньше этого срока. Нажмите “Редактировать” и проверьте данные, чтобы продолжить заполнять заявление";
    private static final String BIRTH_PLACE_NOT_VALID_TITLE = "Проверьте место рождения";
    private static final String BIRTH_PLACE_NOT_VALID_DESC = "Поле может содержать только русские буквы, цифры и символы: «.», «,», «;», «:», «-», «'», «\"», «(», «)», «/», «№»";
    private static final String FIELD_NOT_FOUND_TITLE = "Добавьте данные, чтобы продолжить";
    private static final String FIELD_NOT_FOUND_DESC = "Нажмите \"Редактировать\" и укажите в профиле";
    private static final String RED_LINE_ICON = "red-line";
    private static final String YELLOW_LINE_ICON = "yellow-line";
    private static final Map<String, String> ERROR_DESC_FIELD_NAMES = Map.of(
            "дата рождения", "дату рождения",
            "серия паспорта", "серию паспорта",
            "дата выдачи паспорта", "дату выдачи паспорта",
            "фамилия", "фамилию"
    );

    private static final String BIRTH_PLACE_PATTERN = "[а-яА-ЯёЁ\\d\\s().\",№:;\\-/']{1,255}";
    private static final String INVALID_CHARS_PATTERN = "[^a-zA-Zа-яА-ЯёЁ\\d\\s\\[\\]()?\\.\",#№:;\\-\\+/'*<>&\\\\]";
    private static final Pattern SPECIAL_CHAR_PATTERN = Pattern.compile("[-().,;:'\"/№]+");

    private final static Map<String, Function<ConfirmPersonalUserData, String>> methodMap = new HashMap<>() {{
        // BaseData
        put(BIRTH_DATE_ATTR, ConfirmPersonalUserData::getBirthDate);
        put(ORG_INN_ATTR, ConfirmPersonalUserData::getInn);
        put(GENDER_ATTR, ConfirmPersonalUserData::getGender);
        put(GENDER_FULL_ATTR, ConfirmPersonalUserData::getGenderFull);
        put(OMS_NUMBER_ATTR, ConfirmPersonalUserData::getOmsNumber);
        put(OMS_SERIES_ATTR, ConfirmPersonalUserData::getOmsSeries);
        put(SNILS, ConfirmPersonalUserData::getSnils);
        put(LAST_NAME_ATTR, ConfirmPersonalUserData::getLastName);
        put(FIRST_NAME_ATTR, ConfirmPersonalUserData::getFirstName);
        put(MIDDLE_NAME_ATTR, ConfirmPersonalUserData::getMiddleName);
        put(DOC_TYPE, ConfirmPersonalUserData::getDocType);
        // RFPassportData
        put(RF_PASSPORT_SERIES_ATTR, ConfirmPersonalUserData::getRfPasportSeries);
        put(RF_PASSPORT_NUMBER_ATTR, ConfirmPersonalUserData::getRfPasportNumber);
        put(RF_PASSPORT_ISSUE_DATE_ATTR, ConfirmPersonalUserData::getRfPasportIssueDate);
        put(RF_PASSPORT_ISSUED_BY_ATTR, ConfirmPersonalUserData::getRfPasportIssuedBy);
        put(RF_PASSPORT_ISSUED_BY_ID_ATTR, ConfirmPersonalUserData::getRfPasportIssuedById);
        put(RF_PASSPORT_ISSUED_BY_ID_FORMATTED_ATTR, ConfirmPersonalUserData::getRfPasportIssuedByIdFormatted);
        put(BIRTH_PLACE_ATTR, ConfirmPersonalUserData::getBirthPlace);
        put(CITIZENSHIP_ATTR, ConfirmPersonalUserData::getCitizenship);
        put(CITIZENSHIP_CODE_ATTR, ConfirmPersonalUserData::getCitizenshipCode);
        //FRGNPassportData
        put(FRGN_PASSPORT_PASSPORT_SERIES_ATTR, ConfirmPersonalUserData::getFrgnPasportSeries);
        put(FRGN_PASSPORT_NUMBER_ATTR, ConfirmPersonalUserData::getFrgnPasportNumber);
        put(FRGN_PASSPORT_ISSUE_DATE_ATTR, ConfirmPersonalUserData::getFrgnPasportIssueDate);
        put(FRGN_PASSPORT_ISSUED_BY_ATTR, ConfirmPersonalUserData::getFrgnPasportIssuedBy);
        put(FRGN_PASSPORT_EXPIRY_DATE_ATTR, ConfirmPersonalUserData::getFrgnPasportExpiryDate);
        put(FRGN_PASSPORT_LAST_NAME_ATTR, ConfirmPersonalUserData::getFrgnPasportLastName);
        put(FRGN_PASSPORT_FIRST_NAME_ATTR, ConfirmPersonalUserData::getFrgnPasportFirstName);
        //ForeignPassportData
        put(FOREIGN_PASSPORT_SERIES_ATTR, ConfirmPersonalUserData::getForeignPasportSeries);
        put(FOREIGN_PASSPORT_NUMBER_ATTR, ConfirmPersonalUserData::getForeignPasportNumber);
        put(FOREIGN_PASSPORT_ISSUE_DATE_ATTR, ConfirmPersonalUserData::getForeignPasportIssueDate);
        put(FOREIGN_PASSPORT_ISSUED_BY_ATTR, ConfirmPersonalUserData::getForeignPasportIssuedBy);
    }};

    /**
     * Набор полей для валидации спецсимволов.
     * @see <a href="https://jira.egovdev.ru/browse/EPGUCORE-74347">Валидация полей со спецсимволами</a>
      */
    private static final Set<String> SPECIAL_CHAR_VALIDATION_FIELDSET = Set.of(
            FIRST_NAME_ATTR,
            LAST_NAME_ATTR,
            BIRTH_PLACE_ATTR,
            FOREIGN_PASSPORT_ISSUED_BY_ATTR,
            RF_PASSPORT_SERIES_ATTR,
            RF_PASSPORT_NUMBER_ATTR,
            RF_PASSPORT_ISSUED_BY_ATTR);

    @Override
    public ComponentType getType() {
        return ComponentType.ConfirmPersonalUserData;
    }

    @Override
    public ComponentResponse<FormDto<ConfirmPersonalUserData>> getInitialValue(FieldComponent component) {
        Set<String> fields = BasicComponentUtil.getPreSetFields(component);
        Boolean checkFRGN = Boolean.valueOf((String) component.getAttrs().get(FRGN_PASSPORT_CHECK_ATTR));
        Boolean skipCheckRf = Boolean.valueOf((String) component.getAttrs().get(RF_PASSPORT_SKIP_CHECK_ATTR));
        List<Map<String, String>> warns = (List<Map<String, String>>) component.getAttrs().get(WARN_ATTR);
        FormDto<ConfirmPersonalUserData> form = prepareForm(userPersonalData, fields, checkFRGN, skipCheckRf, warns);
        return ComponentResponse.of(form);
    }

    @Override
    protected void preValidate(ComponentResponse<FormDto<ConfirmPersonalUserData>> initialValue, FieldComponent component, ScenarioDto scenarioDto) {
        if (Objects.nonNull(component.getAttrs()) && Objects.nonNull(component.getAttrs().get(FIELDS_KEY))) {
            List<DocInputField> fields = objectMapper.convertValue(component.getAttrs().get(FIELDS_KEY), new TypeReference<>() {});
            FormDto<ConfirmPersonalUserData> formDto = initialValue.get();
            List<ErrorDto> errors = validate(formDto.getStoredValues(), fields);
            //подготовка к валидации по RegExp из джсона, если она есть, иначе по дефолтной с бэкенда
            Map<String, FieldDto> fieldsComponent = BasicComponentUtil.getComponentFields(component);
            ConfirmPersonalUserData confirmPersonalUserData = formDto.getStoredValues();
            errors.addAll( getValidationErrors(fieldsComponent.keySet(), confirmPersonalUserData, fieldsComponent) );

            List<ErrorDto> formErrors = formDto.getErrors() == null ? new ArrayList<>() : new ArrayList<>(formDto.getErrors());
            formErrors.addAll(errors);
            formDto.setErrors(formErrors);
        }
    }

    private List<ErrorDto> getValidationErrors(Set<String> namesField, ConfirmPersonalUserData confirmPersonalUserData, Map<String, FieldDto> fieldsComponent) {
        List<ErrorDto> errors = new ArrayList<>();

        if (StringUtils.hasText(confirmPersonalUserData.getDocType()) && confirmPersonalUserData.getDocType().equals(RF_PASSPORT_ATTR)) {
            errors.addAll(getValidationErrorsForRfPassport(confirmPersonalUserData, fieldsComponent));
        }
        //загран паспорт
        if (StringUtils.hasText(confirmPersonalUserData.getDocType()) && confirmPersonalUserData.getDocType().equals(FRGN_PASSPORT_ATTR)) {
                errors.addAll(getValidationErrorsForFrgnPassport(confirmPersonalUserData, fieldsComponent));
        }
        // паспорт ИГ
        if (StringUtils.hasText(confirmPersonalUserData.getDocType()) && confirmPersonalUserData.getDocType().equals(FID_DOC_ATTR)) {
             errors.addAll(getValidationErrorsForForiegnPassport(confirmPersonalUserData, fieldsComponent));
        }
            //основные общие параметры
            if (fieldsComponent.containsKey(FIRST_NAME_ATTR)) {
                validateByJsonRule(fieldsComponent.get(FIRST_NAME_ATTR), errors, confirmPersonalUserData.getFirstName());
            }

            if (fieldsComponent.containsKey(LAST_NAME_ATTR)) {
                validateByJsonRule(fieldsComponent.get(LAST_NAME_ATTR), errors, confirmPersonalUserData.getLastName());
            }

            if (fieldsComponent.containsKey(MIDDLE_NAME_ATTR)) {
                validateByJsonRule(fieldsComponent.get(MIDDLE_NAME_ATTR), errors, confirmPersonalUserData.getMiddleName());
            }

            if (fieldsComponent.containsKey(BIRTH_DATE_ATTR)) {
                validateByJsonRule(fieldsComponent.get(BIRTH_DATE_ATTR), errors, confirmPersonalUserData.getBirthDate());
            }

            if (fieldsComponent.containsKey(ORG_INN_ATTR)) {
                validateByJsonRule(fieldsComponent.get(ORG_INN_ATTR), errors, confirmPersonalUserData.getInn());
            }

            if (fieldsComponent.containsKey(OMS_NUMBER_ATTR)) {
                validateByJsonRule(fieldsComponent.get(OMS_NUMBER_ATTR), errors, confirmPersonalUserData.getOmsNumber());
            }

            BiPredicate<String, String> checkDataPredicate =
                    (dataName, dataValue) -> namesField.contains(dataName) && !StringUtils.isEmpty(dataValue) && fieldsComponent.containsKey(dataName);
            if (checkDataPredicate.test(BIRTH_PLACE_ATTR, confirmPersonalUserData.getBirthPlace())) {
                validateField(fieldsComponent.get(BIRTH_PLACE_ATTR), errors,
                        BIRTH_PLACE_PATTERN, BIRTH_PLACE_NOT_VALID_TITLE, BIRTH_PLACE_NOT_VALID_DESC, confirmPersonalUserData.getBirthPlace());
            }

            if (fieldsComponent.containsKey(SNILS)) {
                validateByJsonRule(fieldsComponent.get(SNILS), errors, confirmPersonalUserData.getSnils());
            }

            if (fieldsComponent.containsKey(CITIZENSHIP_ATTR)) {
                validateByJsonRule(fieldsComponent.get(CITIZENSHIP_ATTR), errors, confirmPersonalUserData.getCitizenship());
            }

        return errors;
    }

    private List<ErrorDto> getValidationErrorsForRfPassport(ConfirmPersonalUserData confirmPersonalUserData, Map<String, FieldDto> fieldsComponent) {
        List<ErrorDto> errors = new ArrayList<>();

        if (fieldsComponent.containsKey(RF_PASSPORT_SERIES_ATTR)) {
            validateByJsonRule(fieldsComponent.get(RF_PASSPORT_SERIES_ATTR), errors, confirmPersonalUserData.getRfPasportSeries());
        }

        if (fieldsComponent.containsKey(RF_PASSPORT_NUMBER_ATTR)) {
            validateByJsonRule(fieldsComponent.get(RF_PASSPORT_NUMBER_ATTR), errors, confirmPersonalUserData.getRfPasportNumber());
        }

        if (fieldsComponent.containsKey(RF_PASSPORT_ISSUE_DATE_ATTR)) {
            validateByJsonRule(fieldsComponent.get(RF_PASSPORT_ISSUE_DATE_ATTR), errors, confirmPersonalUserData.getRfPasportIssueDate());
        }

        if (fieldsComponent.containsKey(RF_PASSPORT_ISSUED_BY_ATTR)) {
            validateByJsonRule(fieldsComponent.get(RF_PASSPORT_ISSUED_BY_ATTR), errors, confirmPersonalUserData.getRfPasportIssuedBy());
        }

        if (fieldsComponent.containsKey(RF_PASSPORT_ISSUED_BY_ID_ATTR)) {
            validateByJsonRule(fieldsComponent.get(RF_PASSPORT_ISSUED_BY_ID_ATTR), errors, confirmPersonalUserData.getRfPasportIssuedBy());
        }

        return errors;
    }

    private List<ErrorDto> getValidationErrorsForFrgnPassport(ConfirmPersonalUserData confirmPersonalUserData, Map<String, FieldDto> fieldsComponent) {
        List<ErrorDto> errors = new ArrayList<>();

        if (fieldsComponent.containsKey(FRGN_PASSPORT_PASSPORT_SERIES_ATTR)) {
            validateByJsonRule(fieldsComponent.get(FRGN_PASSPORT_PASSPORT_SERIES_ATTR), errors, confirmPersonalUserData.getFrgnPasportSeries());
        }

        if (fieldsComponent.containsKey(FRGN_PASSPORT_NUMBER_ATTR)) {
            validateByJsonRule(fieldsComponent.get(FRGN_PASSPORT_NUMBER_ATTR), errors, confirmPersonalUserData.getFrgnPasportNumber());
        }

        if (fieldsComponent.containsKey(FRGN_PASSPORT_ISSUE_DATE_ATTR)) {
            validateByJsonRule(fieldsComponent.get(FRGN_PASSPORT_ISSUE_DATE_ATTR), errors, confirmPersonalUserData.getFrgnPasportIssueDate());
        }

        if (fieldsComponent.containsKey(FRGN_PASSPORT_ISSUED_BY_ATTR)) {
            validateByJsonRule(fieldsComponent.get(FRGN_PASSPORT_ISSUED_BY_ATTR), errors, confirmPersonalUserData.getFrgnPasportIssuedBy());
        }

        if (fieldsComponent.containsKey(FRGN_PASSPORT_EXPIRY_DATE_ATTR)) {
            validateByJsonRule(fieldsComponent.get(FRGN_PASSPORT_EXPIRY_DATE_ATTR), errors, confirmPersonalUserData.getFrgnPasportExpiryDate());
        }

        return errors;
    }

    private List<ErrorDto> getValidationErrorsForForiegnPassport(ConfirmPersonalUserData confirmPersonalUserData, Map<String, FieldDto> fieldsComponent) {
        List<ErrorDto> errors = new ArrayList<>();
        if (fieldsComponent.containsKey(FOREIGN_PASSPORT_SERIES_ATTR)) {
            validateByJsonRule(fieldsComponent.get(FOREIGN_PASSPORT_SERIES_ATTR), errors, confirmPersonalUserData.getForeignPasportSeries());
        }

        if (fieldsComponent.containsKey(FOREIGN_PASSPORT_NUMBER_ATTR)) {
            validateByJsonRule(fieldsComponent.get(FOREIGN_PASSPORT_NUMBER_ATTR), errors, confirmPersonalUserData.getForeignPasportNumber());
        }

        if (fieldsComponent.containsKey(FOREIGN_PASSPORT_ISSUE_DATE_ATTR)) {
            validateByJsonRule(fieldsComponent.get(FOREIGN_PASSPORT_ISSUE_DATE_ATTR), errors, confirmPersonalUserData.getForeignPasportIssueDate());
        }

        if (fieldsComponent.containsKey(FOREIGN_PASSPORT_ISSUED_BY_ATTR)) {
            validateByJsonRule(fieldsComponent.get(FOREIGN_PASSPORT_ISSUED_BY_ATTR), errors, confirmPersonalUserData.getForeignPasportIssuedBy());
        }
        return errors;
    }

    private void validateField(FieldDto field, List<ErrorDto> errors, String value, String errorMsg, String errorDesc,String stringToCheck) {
        Map<String, Object> attrs = field.getAttrs();
        if (!attrs.containsKey(FieldComponentUtil.VALIDATION_ARRAY_KEY)) {//нужен заполненный value у FieldDto
            attrs.put(FieldComponentUtil.VALIDATION_ARRAY_KEY, List.of(Map.of(
                    ComponentAttributes.TYPE_ATTR, "RegExp",
                    ComponentAttributes.VALUE_ATTR, value,
                    ComponentAttributes.ERROR_MSG_ATTR, errorMsg,
                    ComponentAttributes.ERROR_DESC_ATTR, errorDesc
            )));
        }
        validateByJsonRule(field, errors, stringToCheck);
    }

    private void validateByJsonRule(FieldDto fieldDto, List<ErrorDto> errors, String stringToCheck) {
        if (fieldDto.getAttrs().containsKey(FieldComponentUtil.VALIDATION_ARRAY_KEY) && StringUtils.hasText(stringToCheck)) {

            // RegExp
            errors.addAll(
                    ((List<Map<String, String>>) fieldDto.getAttrs().get(FieldComponentUtil.VALIDATION_ARRAY_KEY))
                            .stream()
                            //.map(v->)
                            .filter(v -> RegExpUtil.REG_EXP_TYPE.equalsIgnoreCase(v.get(ComponentAttributes.TYPE_ATTR)))
                            .filter(v -> !stringToCheck.matches(v.get(ComponentAttributes.VALUE_ATTR)))
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

    public static FormDto<ConfirmPersonalUserData> prepareForm(UserPersonalData personalData, Set<String> fields) {
        return prepareForm(personalData, fields, false, false, null);
    }

    public static FormDto<ConfirmPersonalUserData> prepareForm(UserPersonalData personalData, Set<String> fields, Boolean checkFRGN, Boolean skipCheckRf, List<Map<String, String>> warns) {
        FormDto.FormDtoBuilder<ConfirmPersonalUserData> builder = FormDto.builder();
        List<String> emptyRequiredFields = new ArrayList<>();
        ConfirmPersonalUserData storedValues = new ConfirmPersonalUserData();
        Person personInfo = personalData.getPerson();

        // заполняем общими данными
        fillBaseData(personalData, fields, builder, storedValues, emptyRequiredFields);

        ConfirmPersonalUserDataErrorType confirmPersonalUserDataErrorType = null;
        // если нужно заполняем паспорт РФ
        if (fields.contains(RF_PASSPORT_SERIES_ATTR)) {
            Optional<PersonDoc> rfPassport = getRfPasport(personalData);
            if (rfPassport.isPresent() && !skipCheckRf) {
                fillRFPassportData(personInfo, rfPassport.get(), fields, builder, storedValues, emptyRequiredFields);
            } else {
                // если нет паспорта РФ возможно стоит вывести загранпаспорт
                if (checkFRGN) {
                    Optional<PersonDoc> frgnPassport = getFRGNPasport(personalData);
                    if (frgnPassport.isPresent()) {
                        fillFRGNPassportData(personInfo, frgnPassport.get(), fields, builder, storedValues, emptyRequiredFields);
                        LocalDate expiryDate = DateUtil.fromEsiaFormat(frgnPassport.get().getExpiryDate());
                        if (expiryDate.isBefore(LocalDate.now()) || expiryDate.isEqual(LocalDate.now())) {
                            confirmPersonalUserDataErrorType = ConfirmPersonalUserDataErrorType.FRGN_PASSPORT_EXPIRE_DATE;
                        }
                    } else {
                        confirmPersonalUserDataErrorType = ConfirmPersonalUserDataErrorType.PASSPORT_MISSING;
                    }
                } else {
                    confirmPersonalUserDataErrorType = ConfirmPersonalUserDataErrorType.PASSPORT_MISSING;
                }
            }
        }

        // если нужно заполняем паспорт ИГ
        if (fields.contains(FOREIGN_PASSPORT_SERIES_ATTR)) {
            Optional<PersonDoc> passport = getForeignPassport(personalData);
            if (passport.isPresent()) {
                fillForeignPassportData(personInfo, passport.get(), fields, builder, storedValues, emptyRequiredFields);
            } else {
                confirmPersonalUserDataErrorType = ConfirmPersonalUserDataErrorType.PASSPORT_MISSING;
            }
        }

        Boolean hasErrors = fillErrorData(builder, storedValues, confirmPersonalUserDataErrorType, emptyRequiredFields, fields);
        // заполняем варнинги
        if (!hasErrors && !CollectionUtils.isEmpty(warns)) {
            for (Map<String, String> it : warns) {
                builder.error(new ErrorDto(YELLOW_LINE_ICON, WARN_ATTR, it.get(TITLE_ATTR), it.get(DESC_ATTR), null));
            }
        }
        return builder.storedValues(storedValues).build();
    }

    private static Optional<PersonDoc> getRfPasport(UserPersonalData personalData) {
        return personalData.getDocs().stream()
                .filter(x -> (x.getType().equals(RF_PASSPORT_ATTR)))
                .max(Comparator.comparing(PersonDoc::getVrfStu,
                        (s1, s2) -> s1.equals(VERIFIED_ATTR) ?
                                Integer.MAX_VALUE :
                                s2.equals(VERIFIED_ATTR) ? Integer.MIN_VALUE : 0));
    }

    private static Optional<PersonDoc> getFRGNPasport(UserPersonalData personalData) {
        return personalData.getDocs().stream()
                .filter(x -> (FRGN_PASSPORT_ATTR.equals(x.getType()) && x.getVrfStu().equals(VERIFIED_ATTR)))
                .findFirst();
    }

    private static Optional<PersonDoc> getForeignPassport(UserPersonalData personalData) {
        return personalData.getDocs().stream()
                .filter(x -> (FID_DOC_ATTR.equals(x.getType()) && x.getVrfStu().equals(VERIFIED_ATTR)))
                .findFirst();
    }

    private static void appendSnilsField(Person personInfo, ConfirmPersonalUserData storedValues, List<String> emptyRequiredFields, List<FieldDto> stateFields) {
        if (!StringUtils.isEmpty(personInfo.getSnils())) {
            fillValue(SNILS_FIELD_LABEL, personInfo.getSnils(), stateFields, emptyRequiredFields);
            storedValues.setSnils(personInfo.getSnils());
        } else {
            emptyRequiredFields.add(SNILS_FIELD_LABEL);
        }
    }

    private static void fillBaseData(UserPersonalData personalData, Set<String> fields,
                                     FormDto.FormDtoBuilder<ConfirmPersonalUserData> builder,
                                     ConfirmPersonalUserData storedValues,
                                     List<String> emptyRequiredFields) {
        Person personInfo = personalData.getPerson();
        List<FieldDto> stateFields = new ArrayList<>();
        storedValues.setFirstName(personInfo.getFirstName());
        storedValues.setLastName(personInfo.getLastName());
        storedValues.setMiddleName(Objects.toString(personInfo.getMiddleName(), ""));

        if (fields.contains(BIRTH_DATE_ATTR)) {
            fillValue("Дата рождения", personInfo.getBirthDate(), stateFields, emptyRequiredFields);
            storedValues.setBirthDate(personInfo.getBirthDate());
        }

        if (fields.contains(GENDER_ATTR)) {
            stateFields.add(new FieldDto("Пол", personInfo.getGender()));
        }

        if (fields.contains(GENDER_FULL_ATTR)) {
            stateFields.add(new FieldDto("Пол", parseFullGender(personInfo.getGender())));
        }
        storedValues.setGender(personInfo.getGender());
        storedValues.setGenderFull(parseFullGender(personInfo.getGender()));
        storedValues.setCitizenship(personInfo.getCitizenship());
        storedValues.setCitizenshipCode(personInfo.getCitizenshipCode());

        if (fields.contains(ORG_INN_ATTR)) {
            fillValue(INN_FIELD_LABEL, personInfo.getInn(), stateFields, emptyRequiredFields);
            storedValues.setInn(personInfo.getInn());
        }

        if (fields.contains(OMS_NUMBER_ATTR)) {
            fillOms(personalData, storedValues, stateFields, emptyRequiredFields);
        }

        // если нужно заполняем СНИЛС
        if (!fields.contains(RF_PASSPORT_SERIES_ATTR) && !fields.contains(FOREIGN_PASSPORT_SERIES_ATTR) && fields.contains(SNILS)) {
            appendSnilsField(personInfo, storedValues, emptyRequiredFields, stateFields);
        }

        builder.state(
                StateDto.builder()
                        .groupName(Stream.of(personInfo.getLastName(), personInfo.getFirstName(), personInfo.getMiddleName())
                                .filter(Objects::nonNull)
                                .collect(Collectors.joining(" ")))
                        .fields(stateFields)
                        .build());
    }

    private static String parseFullGender(String gender) {
        return "F".equals(gender) ? "Женский" : "Мужской";
    }

    /**
     * Заполняем полис ОМС. Есть полисы двух видов старого образца (серия + номер) и нового (только номер)
     */
    private static void fillOms(UserPersonalData personalData, ConfirmPersonalUserData storedValues, List<FieldDto> stateFields, List<String> emptyRequiredFields) {
        Optional<PersonDoc> oms = personalData.getDocs().stream()
                .filter(x -> (MDCL_PLCY_ATTR.equals(x.getType())))
                .findFirst();
        if (oms.isPresent()) {
            storedValues.setOmsSeries(oms.get().getSeries());
            storedValues.setOmsNumber(oms.get().getNumber());
            if (StringUtils.isEmpty(storedValues.getOmsNumber())) {
                emptyRequiredFields.add("номер полиса ОМС");
            } else if (StringUtils.isEmpty(storedValues.getOmsSeries())) {
                stateFields.add(new FieldDto("Номер полиса ОМС", storedValues.getOmsNumber()));
            } else {
                stateFields.add(new FieldDto("Серия и номер полиса ОМС", storedValues.getOmsSeries() + " " + storedValues.getOmsNumber()));
            }
        } else {
            emptyRequiredFields.add("полис ОМС");
        }
    }

    private static void fillRFPassportData(Person personInfo, PersonDoc passport, Set<String> fields,
                                           FormDto.FormDtoBuilder<ConfirmPersonalUserData> builder,
                                           ConfirmPersonalUserData storedValues,
                                           List<String> emptyRequiredFields) {
        List<FieldDto> stateFields = new ArrayList<>();
        storedValues.setDocType(passport.getType());
        storedValues.setRfPasportSeries(passport.getSeries());
        storedValues.setRfPasportNumber(passport.getNumber());

        if (fields.contains(RF_PASSPORT_SERIES_ATTR)) {
            fillPassportSeriesAndNumber(passport, stateFields, emptyRequiredFields);
        }

        if (fields.contains(RF_PASSPORT_ISSUE_DATE_ATTR)) {
            fillValue(ISSUE_DATE_FIELD_LABEL, "паспорта", passport.getIssueDate(), stateFields, emptyRequiredFields);
            storedValues.setRfPasportIssueDate(passport.getIssueDate());
        }

        if (fields.contains(RF_PASSPORT_ISSUED_BY_ATTR)) {
            String clearedIssuedBy = getClearedValue(passport.getIssuedBy());
            fillValue(ISSUE_BY_FIELD_LABEL, "паспорт", clearedIssuedBy, stateFields, emptyRequiredFields);
            storedValues.setRfPasportIssuedBy(clearedIssuedBy);
        }

        if (fields.contains(RF_PASSPORT_ISSUED_BY_ID_ATTR)) {
            // требуется показывать "Код подразделения" в формате XXX-XXX
            String displayIssueId = PassportUtil.formatIssueId(passport.getIssueId());
            fillValue(ISSUE_BY_ID_FIELD_LABEL, displayIssueId, stateFields, emptyRequiredFields);
            storedValues.setRfPasportIssuedById(passport.getIssueId());
            storedValues.setRfPasportIssuedByIdFormatted(displayIssueId);
        }

        if (fields.contains(BIRTH_PLACE_ATTR)) {
            String clearedBirthPlace = getClearedValue(personInfo.getBirthPlace());
            fillValue(BIRTH_PLACE_FIELD_LABEL, clearedBirthPlace, stateFields, emptyRequiredFields);
            storedValues.setBirthPlace(clearedBirthPlace);
        }

        if (fields.contains(CITIZENSHIP_ATTR)) {
            stateFields.add(new FieldDto(CITIZENSHIP_FIELD_LABEL, personInfo.getCitizenship()));
        }

        // если нужно заполняем СНИЛС
        if (!fields.contains(FOREIGN_PASSPORT_SERIES_ATTR) && fields.contains(SNILS)) {
            appendSnilsField(personInfo, storedValues, emptyRequiredFields, stateFields);
        }

        builder.state(
                StateDto.builder()
                        .groupName("Паспорт гражданина РФ")
                        .fields(stateFields)
                        .build());
    }

    private static void fillForeignPassportData(Person personInfo, PersonDoc passport, Set<String> fields,
                                                FormDto.FormDtoBuilder<ConfirmPersonalUserData> builder,
                                                ConfirmPersonalUserData storedValues,
                                                List<String> emptyRequiredFields) {
        List<FieldDto> stateFields = new ArrayList<>();
        String foreignPassportSeries = (passport.getSeries() == null) ? "" : passport.getSeries();
        storedValues.setDocType(passport.getType());
        storedValues.setForeignPasportSeries(foreignPassportSeries);
        storedValues.setForeignPasportNumber(passport.getNumber());

        if (fields.contains(FOREIGN_PASSPORT_SERIES_ATTR)) {
            fillPassportSeriesAndNumber(passport, stateFields, emptyRequiredFields);
        }

        if (fields.contains(FOREIGN_PASSPORT_ISSUE_DATE_ATTR)) {
            fillValue(ISSUE_DATE_FIELD_LABEL, "паспорта", passport.getIssueDate(), stateFields, emptyRequiredFields);
            storedValues.setForeignPasportIssueDate(passport.getIssueDate());
        }

        if (fields.contains(FOREIGN_PASSPORT_ISSUED_BY_ATTR)) {
            String clearedIssuedBy = getClearedValue(passport.getIssuedBy());
            fillValue(ISSUE_BY_FIELD_LABEL, "паспорт", clearedIssuedBy, stateFields, emptyRequiredFields);
            storedValues.setForeignPasportIssuedBy(clearedIssuedBy);
        }

        if (fields.contains(BIRTH_PLACE_ATTR)) {
            String clearedBirthPlace = getClearedValue(personInfo.getBirthPlace());
            fillValue(BIRTH_PLACE_FIELD_LABEL, clearedBirthPlace, stateFields, emptyRequiredFields);
            storedValues.setBirthPlace(clearedBirthPlace);
        }

        if (fields.contains(CITIZENSHIP_ATTR)) {
            stateFields.add(new FieldDto(CITIZENSHIP_FIELD_LABEL, personInfo.getCitizenship()));
        }

        // если нужно заполняем СНИЛС
        if (fields.contains(SNILS)) {
            appendSnilsField(personInfo, storedValues, emptyRequiredFields, stateFields);
        }

        builder.state(
                StateDto.builder()
                        .groupName("Документ, удостоверяющий личность")
                        .fields(stateFields)
                        .build());
    }

    private static void fillFRGNPassportData(Person personInfo, PersonDoc passport, Set<String> fields,
                                             FormDto.FormDtoBuilder<ConfirmPersonalUserData> builder,
                                             ConfirmPersonalUserData storedValues,
                                             List<String> emptyRequiredFields) {
        List<FieldDto> stateFields = new ArrayList<>();
        storedValues.setDocType(passport.getType());
        storedValues.setFrgnPasportSeries(passport.getSeries());
        storedValues.setFrgnPasportNumber(passport.getNumber());

        fillPassportSeriesAndNumber(passport, stateFields, emptyRequiredFields);

        fillValue(ISSUE_DATE_FIELD_LABEL, passport.getIssueDate(), stateFields, emptyRequiredFields);
        storedValues.setFrgnPasportIssueDate(passport.getIssueDate());

        String clearedIssuedBy = getClearedValue(passport.getIssuedBy());
        fillValue(ISSUE_BY_FIELD_LABEL, clearedIssuedBy, stateFields, emptyRequiredFields);
        storedValues.setFrgnPasportIssuedBy(clearedIssuedBy);

        fillValue(EXPIRY_DATE_FIELD_LABEL, passport.getExpiryDate(), stateFields, emptyRequiredFields);
        storedValues.setFrgnPasportExpiryDate(passport.getExpiryDate());

        String clearedLastName = getClearedValue(passport.getLastName());
        stateFields.add(new FieldDto(LAST_NAME_FIELD_LABEL, clearedLastName));
        storedValues.setFrgnPasportLastName(clearedLastName);

        String clearedFirstName = getClearedValue(passport.getFirstName());
        stateFields.add(new FieldDto(FIRST_NAME_FIELD_LABEL, clearedFirstName));
        storedValues.setFrgnPasportFirstName(clearedFirstName);

        if (fields.contains(BIRTH_PLACE_ATTR)) {
            String clearedBirthPlace = getClearedValue(personInfo.getBirthPlace());
            fillValue(BIRTH_PLACE_FIELD_LABEL, clearedBirthPlace, stateFields, emptyRequiredFields);
            storedValues.setBirthPlace(clearedBirthPlace);
        }

        if (fields.contains(CITIZENSHIP_ATTR)) {
            stateFields.add(new FieldDto(CITIZENSHIP_FIELD_LABEL, personInfo.getCitizenship()));
        }

        // если нужно заполняем СНИЛС
        if (!fields.contains(FOREIGN_PASSPORT_SERIES_ATTR) && fields.contains(SNILS)) {
            appendSnilsField(personInfo, storedValues, emptyRequiredFields, stateFields);
        }

        builder.state(
                StateDto.builder()
                        .groupName("Заграничный паспорт гражданина РФ")
                        .fields(stateFields)
                        .build());
    }

    private static void fillPassportSeriesAndNumber(PersonDoc passport, List<FieldDto> stateFields, List<String> emptyRequiredFields) {
        if (!passport.getType().equals(FID_DOC_ATTR)) {                             // для всех паспортов кроме паспорта иностранного гражданина
            if (!StringUtils.isEmpty(passport.getSeries()) && !StringUtils.isEmpty(passport.getNumber())) {
                stateFields.add(new FieldDto("Серия и номер", passport.getSeries() + " " + passport.getNumber()));
            } else {
                if (StringUtils.isEmpty(passport.getSeries())) {
                    emptyRequiredFields.add("серия паспорта");
                }
                if (StringUtils.isEmpty(passport.getNumber())) {
                    emptyRequiredFields.add("номер паспорта");
                }
            }
        } else {                                                                  // для паспорта иностранного гражданина серия - необязательна
            if (!StringUtils.isEmpty(passport.getNumber())) {
                var series = (StringUtils.isEmpty(passport.getSeries())) ? "" : passport.getSeries() + " ";
                stateFields.add(new FieldDto("Серия и номер", series + passport.getNumber()));
            } else {
                emptyRequiredFields.add("номер паспорта");
            }
        }
    }

    private static void fillValue(String label, String value, List<FieldDto> stateFields, List<String> emptyRequiredFields) {
        fillValue(label, "", value, stateFields, emptyRequiredFields);
    }

    private static void fillValue(String label, String labelPostfix, String value, List<FieldDto> stateFields, List<String> emptyRequiredFields) {
        if (StringUtils.hasText(value)) {
            stateFields.add(new FieldDto(label, value));
        } else {
            String fieldName = label.equals(SNILS_FIELD_LABEL) || label.equals(INN_FIELD_LABEL) ? label : label.toLowerCase();
            emptyRequiredFields.add((fieldName + " " + labelPostfix).trim());
        }
    }

    /**
     * Заполняем ошибки
     *
     * @param builder             Билдер
     * @param storedValues        Сохраненные значения
     * @param errorType           Тип ошибки
     * @param emptyRequiredFields Список обязательных полей с не заполненным значением
     * @param requiredFields      Набор обязательных полей
     * @return Признак - есть ошибки/нет
     */
    private static Boolean fillErrorData(FormDto.FormDtoBuilder<ConfirmPersonalUserData> builder, ConfirmPersonalUserData storedValues,
                                         ConfirmPersonalUserDataErrorType errorType, List<String> emptyRequiredFields,
                                         Set<String> requiredFields) {
        List<ErrorDto> errors = new ArrayList<>();
        // условно вазимоисключающие ошибки - нет паспорта vs загран просрочен
        if (errorType == ConfirmPersonalUserDataErrorType.PASSPORT_MISSING) {
            errors.add(new ErrorDto(RED_LINE_ICON, ERROR_ATTR, PASSPORT_NOT_FOUND_TITLE, PASSPORT_NOT_FOUND_DESC, null));
        } else if (errorType == ConfirmPersonalUserDataErrorType.FRGN_PASSPORT_EXPIRE_DATE) {
            errors.add(new ErrorDto(RED_LINE_ICON, ERROR_ATTR, FRGN_PASSPORT_EXPIRE_DATE_TITLE, FRGN_PASSPORT_EXPIRE_DATE_DESC, null));
        }

        // не заполненные обязательные поля
        if (!emptyRequiredFields.isEmpty()) {
            String desc = FIELD_NOT_FOUND_DESC;
            List<String> fields = null;
            if (emptyRequiredFields.size() == 1) {
                // нужно просклонять заголовок филда
                desc = desc + " " + ERROR_DESC_FIELD_NAMES.getOrDefault(emptyRequiredFields.get(0), emptyRequiredFields.get(0));
            } else {
                desc = desc + ":";
                // делаем такую манипуляцию из-за чудес дизайна компонента
                fields = Arrays.asList((String.join(",&", emptyRequiredFields) + ".").split("&"));
            }
            errors.add(new ErrorDto(RED_LINE_ICON, ERROR_ATTR, FIELD_NOT_FOUND_TITLE, desc, fields));
        }

        // паспорт выдан до 14 лет
        if (requiredFields.contains(RF_PASSPORT_ISSUE_DATE_ATTR) && StringUtils.hasText(storedValues.getRfPasportIssueDate())
                && requiredFields.contains(BIRTH_DATE_ATTR) && StringUtils.hasText(storedValues.getBirthDate())) {
            LocalDate issueDate = DateUtil.fromEsiaFormat(storedValues.getRfPasportIssueDate());
            LocalDate birthDate = DateUtil.fromEsiaFormat(storedValues.getBirthDate());
            if (birthDate.plusYears(14).compareTo(issueDate) > 0) {
                errors.add(new ErrorDto(RED_LINE_ICON, ERROR_ATTR, RF_PASSPORT_ISSUE_DATE_TITLE, RF_PASSPORT_ISSUE_DATE_DESC, null));
            }
        }

        if (!CollectionUtils.isEmpty(errors)) {
            builder.errors(errors);
            return true;
        }

        return false;
    }

    /**
     * осуществляет дополнительную валидацию по спецсимволам без букв по заданному списку полей.
     * @param storedValues Сохраненные значения
     * @param fields поля компонента для отображения
     * @return список валидационных ошибок
     */
    private static List<ErrorDto> validate(ConfirmPersonalUserData storedValues, List<DocInputField> fields) {
        Map<String, String> validationErrors = new HashMap<>();
        fields.stream()
                .filter(field -> Objects.nonNull(field) && SPECIAL_CHAR_VALIDATION_FIELDSET.contains(field.getFieldName()))
                .forEach(field -> {
                    Function<ConfirmPersonalUserData, String> userDataValidationFunction = methodMap.get(field.getFieldName());
                    if (userDataValidationFunction == null) {
                        log.error(String.format("methodMap does not contain %s fieldName", field.getFieldName()));
                    }
                    String stringToCheck = userDataValidationFunction.apply(storedValues);
                    if (!StringUtils.isEmpty(stringToCheck) && SPECIAL_CHAR_PATTERN.matcher(stringToCheck).matches()) {
                        validationErrors.put("Проверьте паспортные данные ", "Нажмите «Редактировать» и заполните все поля, точно как в паспорте");
                    }
                });
        return validationErrors.entrySet().stream()
                .map(entry -> new ErrorDto(RED_LINE_ICON, ERROR_ATTR, entry.getKey(), entry.getValue(), null))
                .collect(Collectors.toList());

    }

    private static String getClearedValue(String initialValue) {
        return StringUtils.isEmpty(initialValue) ? initialValue : initialValue.replaceAll(INVALID_CHARS_PATTERN, "").trim();
    }

}
