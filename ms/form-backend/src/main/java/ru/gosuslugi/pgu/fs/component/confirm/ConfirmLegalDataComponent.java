package ru.gosuslugi.pgu.fs.component.confirm;

import com.fasterxml.jackson.core.type.TypeReference;
import lombok.RequiredArgsConstructor;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.logging.log4j.util.Strings;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import ru.atc.carcass.security.rest.model.orgs.Org;
import ru.atc.carcass.security.rest.model.person.Person;
import ru.gosuslugi.pgu.common.core.json.JsonProcessingUtil;
import ru.gosuslugi.pgu.common.esia.search.dto.UserOrgData;
import ru.gosuslugi.pgu.components.BasicComponentUtil;
import ru.gosuslugi.pgu.components.dto.ErrorDto;
import ru.gosuslugi.pgu.components.dto.FieldDto;
import ru.gosuslugi.pgu.components.dto.StateDto;
import ru.gosuslugi.pgu.dto.ApplicantAnswer;
import ru.gosuslugi.pgu.dto.ScenarioDto;
import ru.gosuslugi.pgu.dto.descriptor.FieldComponent;
import ru.gosuslugi.pgu.dto.descriptor.types.ComponentType;
import ru.gosuslugi.pgu.fs.common.component.AbstractComponent;
import ru.gosuslugi.pgu.fs.common.component.ComponentResponse;
import ru.gosuslugi.pgu.fs.common.exception.FormBaseWorkflowException;
import ru.gosuslugi.pgu.fs.component.FormDto;
import ru.gosuslugi.pgu.fs.component.confirm.model.ConfirmLegalData;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static ru.gosuslugi.pgu.components.ComponentAttributes.ERROR_ATTR;
import static ru.gosuslugi.pgu.components.ComponentAttributes.ORG_CHIEF_BIRTH_DATE_ATTR;
import static ru.gosuslugi.pgu.components.ComponentAttributes.ORG_CHIEF_FIRST_NAME_ATTR;
import static ru.gosuslugi.pgu.components.ComponentAttributes.ORG_CHIEF_LAST_NAME_ATTR;
import static ru.gosuslugi.pgu.components.ComponentAttributes.ORG_CHIEF_MIDDLE_NAME_ATTR;
import static ru.gosuslugi.pgu.components.ComponentAttributes.ORG_CHIEF_POSITION_ATTR;
import static ru.gosuslugi.pgu.components.ComponentAttributes.ORG_FULL_NAME_ATTR;
import static ru.gosuslugi.pgu.components.ComponentAttributes.ORG_INN_ATTR;
import static ru.gosuslugi.pgu.components.ComponentAttributes.ORG_KPP_ATTR;
import static ru.gosuslugi.pgu.components.ComponentAttributes.ORG_OGRN_ATTR;
import static ru.gosuslugi.pgu.components.ComponentAttributes.ORG_SHORT_NAME_ATTR;

/**
 * Компонент показывает юридические данные организации или ИП из ЕСИА
 */
@Component
@RequiredArgsConstructor
public class ConfirmLegalDataComponent extends AbstractComponent<FormDto<ConfirmLegalData>> {

    public static final String USER_TYPE_ERROR = "Войдите как сотрудник организации или ИП в личном кабинете";
    private static final String UNABLE_TO_GET_CHIEF_DATA = "Невозможно получить данные руководителя";
    private static final String DISCLAIMER_DESCRIPTION = "Это можно сделать в налоговой";
    private static final String FIELD_NOT_FOUND_TITLE_START = "Добавьте ";
    private static final String FIELD_NOT_FOUND_TITLE_END = ", чтобы продолжить ";
    private static final String OGRN_FIELD_LABEL = "ОГРН";
    private static final String INN_FIELD_LABEL = "ИНН";
    private static final String KPP_FIELD_LABEL = "КПП";
    private static final String RED_LINE_ICON = "red-line";

    private final UserOrgData userOrgData;

    @Override
    public ComponentType getType() {
        return ComponentType.ConfirmLegalData;
    }

    @Override
    public ComponentResponse<FormDto<ConfirmLegalData>> getInitialValue(FieldComponent component) {
        return ComponentResponse.of(getLegalFormData(component));
    }

    private FormDto<ConfirmLegalData> getLegalFormData(FieldComponent component) {
        Org org = userOrgData.getOrg();
        if (Objects.isNull(org)) {
            throw new FormBaseWorkflowException(USER_TYPE_ERROR);
        }
        Map<String, FieldDto> fields = BasicComponentUtil.getFieldNameToFieldDtoMap(component);
        ConfirmLegalData storedValues = new ConfirmLegalData();
        List<StateDto> states = new ArrayList<>();
        List<FieldDto> orgInfoFields = new ArrayList<>();

        List<ErrorDto> errors = new ArrayList<>();
        ErrorDto errorDto;

        if (fields.containsKey(ORG_FULL_NAME_ATTR)) {
            storedValues.setFullName(org.getFullName());
        }
        if (fields.containsKey(ORG_SHORT_NAME_ATTR)) {
            FieldDto fieldDto = fields.get(ORG_SHORT_NAME_ATTR);
            fieldDto.setValue(org.getShortName());
            orgInfoFields.add(fieldDto);
            storedValues.setShortName(org.getShortName());
        }
        if (fields.containsKey(ORG_OGRN_ATTR)) {
            FieldDto fieldDto = fields.get(ORG_OGRN_ATTR);
            if (fieldDto.isRequired() && StringUtils.isEmpty(org.getOgrn())) {
                errorDto = new ErrorDto(
                        RED_LINE_ICON,
                        ERROR_ATTR,
                        FIELD_NOT_FOUND_TITLE_START + OGRN_FIELD_LABEL + FIELD_NOT_FOUND_TITLE_END,
                        DISCLAIMER_DESCRIPTION,
                        null
                );
                errors.add(errorDto);
            }
            fieldDto.setValue(org.getOgrn());
            orgInfoFields.add(fieldDto);
            storedValues.setOgrn(org.getOgrn());
        }
        if (fields.containsKey(ORG_INN_ATTR)) {
            FieldDto fieldDto = fields.get(ORG_INN_ATTR);
            if (fieldDto.isRequired() && StringUtils.isEmpty(org.getInn())) {
                errorDto = new ErrorDto(
                        RED_LINE_ICON,
                        ERROR_ATTR,
                        FIELD_NOT_FOUND_TITLE_START + INN_FIELD_LABEL + FIELD_NOT_FOUND_TITLE_END,
                        DISCLAIMER_DESCRIPTION,
                        null
                );
                errors.add(errorDto);
            }
            fieldDto.setValue(org.getInn());
            orgInfoFields.add(fieldDto);
            storedValues.setInn(org.getInn());
        }
        if (fields.containsKey(ORG_KPP_ATTR)) {
            FieldDto fieldDto = fields.get(ORG_KPP_ATTR);
            if (fieldDto.isRequired() && StringUtils.isEmpty(org.getKpp())) {
                errorDto = new ErrorDto(
                        RED_LINE_ICON,
                        ERROR_ATTR,
                        FIELD_NOT_FOUND_TITLE_START + KPP_FIELD_LABEL + FIELD_NOT_FOUND_TITLE_END,
                        DISCLAIMER_DESCRIPTION,
                        null
                );
                errors.add(errorDto);
            }
            fieldDto.setValue(org.getKpp());
            orgInfoFields.add(fieldDto);
            storedValues.setKpp(org.getKpp());
        }
        orgInfoFields = orgInfoFields.stream()
                .filter(fieldDto -> Strings.isNotBlank(fieldDto.getValue()))
                .collect(Collectors.toList());
        if (CollectionUtils.isNotEmpty(orgInfoFields)) {
            states.add(StateDto.builder()
                    .groupName(storedValues.getFullName())
                    .fields(orgInfoFields)
                    .build());
        }

        List<FieldDto> chiefFields = new ArrayList<>();

        setChiefInitData(storedValues, fields, chiefFields);

        chiefFields = chiefFields.stream()
                .filter(fieldDto -> Strings.isNotBlank(fieldDto.getValue()))
                .collect(Collectors.toList());

        if (CollectionUtils.isNotEmpty(chiefFields)) {
            states.add(StateDto.builder()
                    .groupName("Данные о руководителе")
                    .fields(chiefFields)
                    .build());
        }
        return FormDto.<ConfirmLegalData>builder()
                .states(states)
                .storedValues(storedValues)
                .errors(errors)
                .build();
    }

    private void setChiefInitData(
            ConfirmLegalData storedValues,
            Map<String, FieldDto> fields,
            List<FieldDto> chiefFields
    ) {
        Person chief = userOrgData.getChief();

        if (Objects.isNull(chief)) {
            throw new FormBaseWorkflowException(UNABLE_TO_GET_CHIEF_DATA);
        }

        if (fields.containsKey(ORG_CHIEF_FIRST_NAME_ATTR)) {
            storedValues.setChiefFirstName(chief.getFirstName());
        }
        if (fields.containsKey(ORG_CHIEF_LAST_NAME_ATTR)) {
            storedValues.setChiefLastName(chief.getLastName());
        }
        if (fields.containsKey(ORG_CHIEF_MIDDLE_NAME_ATTR)) {
            storedValues.setChiefMiddleName(chief.getMiddleName());
        }

        String chiefFio = Stream.of(
                        storedValues.getChiefLastName(),
                        storedValues.getChiefFirstName(),
                        storedValues.getChiefMiddleName()
                )
                .filter(Objects::nonNull)
                .collect(Collectors.joining(" "));

        if (Strings.isNotBlank(chiefFio)) {
            chiefFields.add(new FieldDto("", chiefFio));
        }

        if (fields.containsKey(ORG_CHIEF_BIRTH_DATE_ATTR)) {
            FieldDto fieldDto = fields.get(ORG_CHIEF_BIRTH_DATE_ATTR);
            fieldDto.setValue(chief.getBirthDate());
            chiefFields.add(fieldDto);
            storedValues.setChiefBirthDate(chief.getBirthDate());
        }

        if (fields.containsKey(ORG_CHIEF_POSITION_ATTR)) {
            FieldDto fieldDto = fields.get(ORG_CHIEF_POSITION_ATTR);
            fieldDto.setValue(chief.getPosition());
            chiefFields.add(fieldDto);
            storedValues.setChiefPosition(chief.getPosition());
        }
    }

    @Override
    protected void postProcess(
            Map.Entry<String, ApplicantAnswer> entry,
            ScenarioDto scenarioDto,
            FieldComponent fieldComponent
    ) {
        ApplicantAnswer answer = entry.getValue();
        FormDto<ConfirmLegalData> formDto = JsonProcessingUtil.fromJson(answer.getValue(), new TypeReference<>() {});
        ConfirmLegalData storedValues = formDto.getStoredValues();
        storedValues.setChiefOid(
                Optional.ofNullable(userOrgData.getChief())
                        .map(Person::getUserId)
                        .orElseThrow(() -> new FormBaseWorkflowException(UNABLE_TO_GET_CHIEF_DATA))
        );
        answer.setValue(JsonProcessingUtil.toJson(formDto));
    }
}
