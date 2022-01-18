package ru.gosuslugi.pgu.fs.component.confirm;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.logging.log4j.util.Strings;
import org.springframework.stereotype.Component;
import ru.atc.carcass.security.rest.model.person.PersonDoc;
import ru.gosuslugi.pgu.common.esia.search.dto.UserPersonalData;
import ru.gosuslugi.pgu.components.BasicComponentUtil;
import ru.gosuslugi.pgu.components.dto.ErrorDto;
import ru.gosuslugi.pgu.components.dto.FieldDto;
import ru.gosuslugi.pgu.components.dto.StateDto;
import ru.gosuslugi.pgu.dto.descriptor.FieldComponent;
import ru.gosuslugi.pgu.dto.descriptor.types.ComponentType;
import ru.gosuslugi.pgu.fs.common.component.AbstractComponent;
import ru.gosuslugi.pgu.fs.common.component.ComponentResponse;
import ru.gosuslugi.pgu.fs.component.FormDto;
import ru.gosuslugi.pgu.fs.component.confirm.model.ConfirmPersonalOms;
import java.util.*;
import java.util.stream.Collectors;
import static ru.gosuslugi.pgu.components.ComponentAttributes.*;

/**
 * Компонент для отображения ОМС
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ConfirmPersonalPolicyComponent extends AbstractComponent<FormDto<ConfirmPersonalOms>> {
    private static final String RED_LINE_ICON = "red-line";
    private static final String ERRORS_ATTR = "errors";
    private static final String SERIES = "series";
    private static final String NUMBER = "number";
    private static final String UNITED_NUMBER = "unitedNumber";
    private static final String ISSUE_PLACE = "issuePlace";
    private static final String ISSUED_BY = "issuedBy";
    private static final String MEDICAL_ORG = "medicalOrg";

    private final UserPersonalData userPersonalData;

    @Override
    public ComponentType getType() {
        return ComponentType.ConfirmPersonalPolicy;
    }

    public ComponentResponse<FormDto<ConfirmPersonalOms>> getInitialValue(FieldComponent component) {
        return ComponentResponse.of(getConfirmPersonalOms(component));
    }

    public FormDto<ConfirmPersonalOms> getConfirmPersonalOms(FieldComponent component) {
        Map<String, FieldDto> fields = BasicComponentUtil.getFieldNameToFieldDtoMap(component);
        List<FieldDto> stateFields = new ArrayList<>();
        List<StateDto> states = new ArrayList<>();
        Set<ErrorDto> errors = new LinkedHashSet<>();
        ErrorDto errorDto = null;
        if (Objects.nonNull(component.getAttrs()) && component.getAttrs().get(ERRORS_ATTR) instanceof List) {
            List<HashMap> errorsListFromJson = (List<HashMap>) component.getAttrs().get(ERRORS_ATTR);
            if (errorsListFromJson.stream().findFirst().isPresent()) {
                HashMap<String, Object> map = errorsListFromJson.stream().findFirst().get();
                String title = String.valueOf(map.get(TITLE_ATTR));
                String desc = String.valueOf(map.get(DESC_ATTR));
                String type = String.valueOf(map.get(TYPE_ATTR));
                errorDto = new ErrorDto(RED_LINE_ICON, type, title, desc, null);
            }
        }
        Optional<PersonDoc> optionalConfirmPersonalOms = userPersonalData.getOmsDocument();
        ConfirmPersonalOms storedValues = new ConfirmPersonalOms();
        if (optionalConfirmPersonalOms.isPresent()) {
            PersonDoc confirmPersonalOms = optionalConfirmPersonalOms.get();
            if (fields.containsKey(SERIES)) {
                FieldDto fieldDto = fields.get(SERIES);
                if (fieldDto.isRequired() && org.springframework.util.StringUtils.isEmpty(confirmPersonalOms.getSeries())) {
                    errors.add(errorDto);
                }
                fieldDto.setValue(confirmPersonalOms.getSeries());
                stateFields.add(fieldDto);
                storedValues.setSeries(confirmPersonalOms.getSeries());
            }
            if (fields.containsKey(NUMBER)) {
                FieldDto fieldDto = fields.get(NUMBER);
                if (fieldDto.isRequired() && org.springframework.util.StringUtils.isEmpty(confirmPersonalOms.getNumber())) {
                    errors.add(errorDto);
                }
                fieldDto.setValue(confirmPersonalOms.getNumber());
                stateFields.add(fieldDto);
                storedValues.setNumber(confirmPersonalOms.getNumber());
            }
            if (fields.containsKey(UNITED_NUMBER)) {
                FieldDto fieldDto = fields.get(UNITED_NUMBER);
                if (fieldDto.isRequired() && org.springframework.util.StringUtils.isEmpty(confirmPersonalOms.getUnitedNumber())) {
                    errors.add(errorDto);
                }
                fieldDto.setValue(confirmPersonalOms.getUnitedNumber());
                stateFields.add(fieldDto);
                storedValues.setUnitedNumber(confirmPersonalOms.getUnitedNumber());
            }
            if (fields.containsKey(ISSUE_PLACE)) {
                FieldDto fieldDto = fields.get(ISSUE_PLACE);
                if (fieldDto.isRequired() && org.springframework.util.StringUtils.isEmpty(confirmPersonalOms.getIssuePlace())) {
                    errors.add(errorDto);
                }
                fieldDto.setValue(confirmPersonalOms.getIssuePlace());
                stateFields.add(fieldDto);
                storedValues.setIssuePlace(confirmPersonalOms.getIssuePlace());
            }
            if (fields.containsKey(ISSUED_BY)) {
                FieldDto fieldDto = fields.get(ISSUED_BY);
                if (fieldDto.isRequired() && org.springframework.util.StringUtils.isEmpty(confirmPersonalOms.getIssuedBy())) {
                    errors.add(errorDto);
                }
                fieldDto.setValue(confirmPersonalOms.getIssuedBy());
                stateFields.add(fieldDto);
                storedValues.setIssuedBy(confirmPersonalOms.getIssuedBy());
            }
            if (fields.containsKey(MEDICAL_ORG)) {
                FieldDto fieldDto = fields.get(MEDICAL_ORG);
                if (fieldDto.isRequired() && org.springframework.util.StringUtils.isEmpty(confirmPersonalOms.getMedicalOrg())) {
                    errors.add(errorDto);
                }
                fieldDto.setValue(confirmPersonalOms.getMedicalOrg());
                stateFields.add(fieldDto);
                storedValues.setMedicalOrg(confirmPersonalOms.getMedicalOrg());
            }
            stateFields = stateFields.stream()
                    .filter(fieldDto -> Strings.isNotBlank(fieldDto.getValue()))
                    .collect(Collectors.toList());
            if (CollectionUtils.isNotEmpty(stateFields)) {
                states.add(StateDto.builder()
                        .groupName("")
                        .fields(stateFields)
                        .build());
            }
        }
        return FormDto.<ConfirmPersonalOms>builder()
                .states(states)
                .storedValues(storedValues)
                .errors(errors)
                .build();
    }
}
