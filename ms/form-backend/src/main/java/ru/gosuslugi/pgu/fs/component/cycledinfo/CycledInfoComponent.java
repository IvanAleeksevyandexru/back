package ru.gosuslugi.pgu.fs.component.cycledinfo;

import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.JsonPath;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.gosuslugi.pgu.components.FieldComponentUtil;
import ru.gosuslugi.pgu.components.descriptor.attr_factory.FieldComponentAttrsFactory;
import ru.gosuslugi.pgu.components.descriptor.placeholder.Reference;
import ru.gosuslugi.pgu.dto.ApplicantAnswer;
import ru.gosuslugi.pgu.dto.ScenarioDto;
import ru.gosuslugi.pgu.dto.cycled.CycledApplicantAnswer;
import ru.gosuslugi.pgu.dto.cycled.CycledApplicantAnswerItem;
import ru.gosuslugi.pgu.dto.descriptor.CycledAttrs;
import ru.gosuslugi.pgu.dto.descriptor.FieldComponent;
import ru.gosuslugi.pgu.dto.descriptor.ServiceDescriptor;
import ru.gosuslugi.pgu.dto.descriptor.types.ComponentType;
import ru.gosuslugi.pgu.fs.common.component.AbstractCycledComponent;
import ru.gosuslugi.pgu.fs.common.component.ComponentResponse;
import ru.gosuslugi.pgu.pgu_common.gibdd.util.DateUtils;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;
import static ru.gosuslugi.pgu.components.descriptor.placeholder.Reference.FIELD_ID_INDEX_IN_PATH;


@Slf4j
@Component
@RequiredArgsConstructor
public class CycledInfoComponent extends AbstractCycledComponent<List<Map<String, Object>>> {

    public static final String FORMAT_KEY = "format";
    public static final String FIELD_NAME_KEY = "fieldName";
    public static final String VALUE_KEY = "value";

    private static final DateTimeFormatter INPUT_DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSXXX");
    private static final DateTimeFormatter DATE_DD_MM_YYYY_FORMATTER = DateTimeFormatter.ofPattern("dd.MM.yyyy");

    @Override
    public ComponentType getType() {
        return ComponentType.CycledInfo;
    }

    @Override
    public ComponentResponse<List<Map<String, Object>>> getCycledInitialValue(FieldComponent component, Map<String, Object> externalData) {
        //получаем поля из джсон сценария, обявленные внутри CycledInfo компонента
        List<Map<Object, Object>> fieldObjects = FieldComponentUtil.getList(component, FieldComponentUtil.FIELDS_KEY, true);
        List<Map<String, Object>> fields = fieldObjects
            .stream()
            .map(FieldComponentUtil::toStringKeyMap)
            .collect(Collectors.toList());

        // Возвращаем оригинальные значения(значения, заполненные юзером)
        Map<String, ApplicantAnswer> applicantAnswers = externalData
            .entrySet()
            .stream()
            .filter(entry -> entry.getValue() instanceof ApplicantAnswer)
                //переводим value мапы из Object в ApplicantAnswer
            .map(entry -> new AbstractMap.SimpleEntry<String, ApplicantAnswer>(entry.getKey(), (ApplicantAnswer) entry.getValue()))
            .collect(HashMap::new, (m,v)-> m.put(v.getKey(), v.getValue()), HashMap::putAll);

        // создаем контекст поиска (все поля, заполненные юзером)
        DocumentContext applicantAnswersContext = JsonPath.parse(jsonProcessingService.convertAnswersToJsonString(applicantAnswers));

        // Ходим по полям и заполняем данныеи, если успешно (заоплняются только те поля что есть в джсон сценарии)
        for (Map<String, Object> field : fields) {
            Optional.of(field)
                .map(f -> f.get(FIELD_NAME_KEY))
                .filter(key -> key instanceof String)
                .map(key -> jsonProcessingService.getFieldFromContext(String.valueOf(key),applicantAnswersContext, Object.class) )
                .map(value -> field.containsKey(FORMAT_KEY) ? DateUtils.format(field.get(FORMAT_KEY), String.valueOf(value)) :value)
                .ifPresent(value -> field.put(VALUE_KEY, value));
        }
        return ComponentResponse.of(fields);
    }

    //переделать метод где мы екстернал дата зполняем не из есиа а из айтемансверс и отдаем эту мапу в гетсайкледвельюс где мы проверям их с заполненными полями
    @Override
    public ComponentResponse<List<Map<String, Object>>> getInitialValue(FieldComponent component, ScenarioDto scenarioDto, ServiceDescriptor serviceDescriptor) {

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
        return getCycledInitialValue(component, getExternalDataMap(applicantAnswer.getItemOrDefault(cycledIndexValue, new CycledApplicantAnswerItem())));
    }

    public Map<String, Object> getExternalDataMap(CycledApplicantAnswerItem answerItem) {
        return answerItem.getItemAnswers()
                .entrySet()
                .stream()
        .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue ));
    }

}
