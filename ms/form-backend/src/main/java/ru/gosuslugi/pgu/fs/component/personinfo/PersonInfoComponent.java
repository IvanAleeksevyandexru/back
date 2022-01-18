package ru.gosuslugi.pgu.fs.component.personinfo;

import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.JsonPath;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import ru.gosuslugi.pgu.components.ComponentAttributes;
import ru.gosuslugi.pgu.components.FieldComponentUtil;
import ru.gosuslugi.pgu.components.descriptor.attr_factory.FieldComponentAttrsFactory;
import ru.gosuslugi.pgu.components.descriptor.placeholder.Reference;
import ru.gosuslugi.pgu.dto.cycled.CycledApplicantAnswer;
import ru.gosuslugi.pgu.dto.cycled.CycledApplicantAnswerItem;
import ru.gosuslugi.pgu.dto.ScenarioDto;
import ru.gosuslugi.pgu.dto.descriptor.CycledAttrs;
import ru.gosuslugi.pgu.dto.descriptor.FieldComponent;
import ru.gosuslugi.pgu.dto.descriptor.ServiceDescriptor;
import ru.gosuslugi.pgu.dto.descriptor.types.ComponentType;
import ru.gosuslugi.pgu.fs.common.component.AbstractCycledComponent;
import ru.gosuslugi.pgu.fs.common.component.ComponentResponse;
import ru.gosuslugi.pgu.fs.component.personinfo.model.AgeType;
import ru.gosuslugi.pgu.fs.component.personinfo.model.PersonAge;
import ru.gosuslugi.pgu.fs.component.personinfo.model.PersonInfoDto;

import java.util.Collections;
import java.util.Map;
import java.util.stream.Collectors;

import static ru.gosuslugi.pgu.components.descriptor.placeholder.Reference.FIELD_ID_INDEX_IN_PATH;

@Component
@RequiredArgsConstructor
public class PersonInfoComponent extends AbstractCycledComponent<PersonInfoDto> {

    private final PersonAgeService personAgeService;

    @Override
    public ComponentType getType() {
        return ComponentType.PersonInfo;
    }

    @Override
    public ComponentResponse<PersonInfoDto> getCycledInitialValue(FieldComponent component, Map<String, Object> externalData) {
        Map<String, String> fields = FieldComponentUtil.getFieldNames(component).stream().collect(Collectors.toMap(s -> s, s -> s));

        PersonInfoDto data = new PersonInfoDto();

        // установка атрибутов, не зависящих от возраста
        data.setGender(getAttributeValue(ComponentAttributes.CHILDREN_GENDER_ATTR, fields, externalData));
        data.setName(getAttributeValue(ComponentAttributes.CHILDREN_FIRST_NAME_ATTR, fields, externalData));

        String birthDate = getAttributeValue(ComponentAttributes.CHILDREN_BIRTH_DATE_ATTR, fields, externalData);

        if(birthDate != null) {
            // установка атрибутов, зависящих от возраста
            PersonAge personAge = personAgeService.createPersonAge(birthDate);

            data.setAgeType(personAge.getAgeType());
            data.setAgeText(personAge.getAgeAsText());

            String middleName = getAttributeValue(ComponentAttributes.CHILDREN_MIDDLE_NAME_ATTR, fields, externalData);
            addMiddleName(data, personAge.getAgeType(), middleName);
        }

        return ComponentResponse.of(data);
    }

    @Override
    public ComponentResponse<PersonInfoDto> getInitialValue(FieldComponent component, ScenarioDto scenarioDto, ServiceDescriptor serviceDescriptor) {

        FieldComponentAttrsFactory componentAttrsFactory = new FieldComponentAttrsFactory(component);

        // Цикличные атрибуты
        CycledAttrs cycledAttrs = componentAttrsFactory.getCycledAttrs(component.getAttrs());

        if(!cycledAttrs.isSet()) {
            return super.getInitialValue(component, scenarioDto, serviceDescriptor);
        }

        // Ссылка на цикличный компонент
        String cycledIndexComponentId = cycledAttrs.getCycledAnswerIndex().split("\\.")[FIELD_ID_INDEX_IN_PATH];
        Reference cycledIndexReference = componentAttrsFactory.getReference(cycledIndexComponentId, cycledAttrs.getCycledAnswerIndex(), Collections.emptyMap());

        // вычисляем значение индекса в цикличном компоненте
        DocumentContext currentValueContext = JsonPath.parse(jsonProcessingService.convertAnswersToJsonString(scenarioDto.getCurrentValue()));
        DocumentContext applicantAnswersContext = JsonPath.parse(jsonProcessingService.convertAnswersToJsonString(scenarioDto.getApplicantAnswers()));
        String cycledIndexValue = cycledIndexReference.getNext(currentValueContext, applicantAnswersContext);

        // вытаскиваем данные ЕСИА для указанных компонентов
        CycledApplicantAnswer applicantAnswer = scenarioDto.getCycledApplicantAnswers().getAnswerOrDefault(cycledAttrs.getCycledAnswerId(), new CycledApplicantAnswer());
        Map<String, Object> esiaData = applicantAnswer.getItemOrDefault(cycledIndexValue, new CycledApplicantAnswerItem()).getEsiaData();

        return getCycledInitialValue(component, esiaData);
    }

    /**
     * Возвращает значение атрибута, если оно присутствует в предоставляемых данных и перечислено в полях компонента
     * @param attributeName название атрибута
     * @param fields поля компонента
     * @param externalData внешние данные (ЕСИА)
     * @return значение атрибута
     */
    private String getAttributeValue(String attributeName, Map<String, String> fields, Map<String, Object> externalData) {
        if(externalData != null && externalData.containsKey(attributeName) && fields.containsKey(attributeName) && externalData.get(attributeName) != null) {
            return externalData.get(attributeName).toString();
        }
        return null;
    }

    /**
     * Добавляет отчество, если оно есть и это взрослое лицо ({@link AgeType#MATURE})
     * @param personInfo ДТО
     * @param ageType тип возраста
     * @param middleName отчество
     */
    private void addMiddleName(PersonInfoDto personInfo, AgeType ageType, String middleName) {
        if(AgeType.MATURE.equals(ageType) && middleName != null) {
            String officialName = personInfo.getName() + " " + middleName;
            personInfo.setName(officialName);
        }
    }
}
