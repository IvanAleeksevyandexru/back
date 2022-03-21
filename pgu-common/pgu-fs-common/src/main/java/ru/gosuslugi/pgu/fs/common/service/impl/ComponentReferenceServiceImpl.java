package ru.gosuslugi.pgu.fs.common.service.impl;

import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.JsonPath;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.context.request.RequestContextHolder;
import ru.gosuslugi.pgu.components.FieldComponentUtil;
import ru.gosuslugi.pgu.components.descriptor.ComponentField;
import ru.gosuslugi.pgu.components.descriptor.FieldGroup;
import ru.gosuslugi.pgu.components.descriptor.attr_factory.FieldComponentAttrsFactory;
import ru.gosuslugi.pgu.components.descriptor.placeholder.PlaceholderContext;
import ru.gosuslugi.pgu.components.descriptor.placeholder.Reference;
import ru.gosuslugi.pgu.components.descriptor.placeholder.SimplePlaceholder;
import ru.gosuslugi.pgu.components.descriptor.types.PresetRefAttrsType;
import ru.gosuslugi.pgu.components.dto.FieldDto;
import ru.gosuslugi.pgu.components.dto.FormDto;
import ru.gosuslugi.pgu.components.dto.StateDto;
import ru.gosuslugi.pgu.dto.ApplicantAnswer;
import ru.gosuslugi.pgu.dto.ScenarioDto;
import ru.gosuslugi.pgu.dto.cycled.CycledApplicantAnswer;
import ru.gosuslugi.pgu.dto.cycled.CycledApplicantAnswerItem;
import ru.gosuslugi.pgu.dto.descriptor.FieldComponent;
import ru.gosuslugi.pgu.dto.descriptor.placeholder.Placeholder;
import ru.gosuslugi.pgu.dto.descriptor.types.ComponentType;
import ru.gosuslugi.pgu.fs.common.service.ComponentReferenceService;
import ru.gosuslugi.pgu.fs.common.service.JsonProcessingService;
import ru.gosuslugi.pgu.fs.common.service.LinkedValuesService;
import ru.gosuslugi.pgu.fs.common.service.UserCookiesService;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static ru.gosuslugi.pgu.components.ComponentAttributes.HIDDEN_EMPTY_FIELDS;
import static ru.gosuslugi.pgu.components.ComponentAttributes.HIDDEN_EMPTY_GROUPS;

@Service
@Slf4j
@RequiredArgsConstructor
public class ComponentReferenceServiceImpl implements ComponentReferenceService {

    private static final Set<String> ACTIONS_OR_ANSWERS_SET = Set.of(FieldComponentUtil.ACTIONS_ATTR_KEY, FieldComponentUtil.ANSWERS_ATTR_KEY);
    private final JsonProcessingService jsonProcessingService;
    private final UserCookiesService userCookiesService;
    private final LinkedValuesService linkedValuesService;

    private final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd.MM.yyyy");
    private final Pattern urlPattern = Pattern.compile("'(https?|ftp)://(.*?)'");
    private final String TIMEZONE_ATTR_NAME = "timezone";
    private final String DEFAULT_TIMEZONE = "+3";

    /**
     * @param component компонент
     * @param scenarioDto сценарий
     * @see #processFieldGroups(FieldComponent, ScenarioDto)
     * @see #getStateDto(PlaceholderContext, FieldGroup, boolean, boolean, DocumentContext...)
     * @see #getValueByContext(String, Function, PlaceholderContext, DocumentContext...)
     */
    @Override
    public void processComponentRefs(FieldComponent component, ScenarioDto scenarioDto) {
        PlaceholderContext context = buildPlaceholderContext(component, scenarioDto);
        DocumentContext[] contextArray = getContexts(scenarioDto);
        component.setLabel(updateUrlsByContext(component.getLabel(), context, contextArray));
        component.setValue(updateUrlsByContext(component.getValue(), context, contextArray));
        component.setLabel(getValueByContext(component.getLabel(), Function.identity(), context, contextArray));
        component.setValue(getValueByContext(component.getValue(), Function.identity(), context, contextArray));

        val attrs = component.getAttrs();
        if (attrs == null) return;
        processAttrsAsMap(component, attrs, context, contextArray);

        replaceTextFields(attrs, contextArray);

        val clarifications = (Map<String, Map<String, Object>>)attrs.get("clarifications");
        if (clarifications == null) return;
        clarifications
                .values()
                .stream()
                .parallel()
                .forEach(clValue -> clarificationsRefProcess(clValue, context, contextArray));
    }

    private void processAttrsAsMap(FieldComponent component, Map<String, Object> map, PlaceholderContext context, DocumentContext[] contextArray) {
        map.entrySet().stream()
                //пропускаем замену плейсхолдеров
                .filter(entry -> !"fieldGroups".equals(entry.getKey())) // во всех компонентах под списком "fieldGroups"
                .filter(entry -> ComponentType.QuestionScr != component.getType() && !ACTIONS_OR_ANSWERS_SET.contains(entry.getKey())) // во всех компонентах QuestionScr под "actions" или "answers" списками
                .parallel()
                .forEach(attrEntry -> {
                    Object element = attrEntry.getValue();
                    if (element instanceof String) {
                        attrEntry.setValue(getValueByContext((String) element, Function.identity(), context, contextArray));
                    } else if (element instanceof Map) {
                        processAttrsAsMap(component, (Map<String, Object>) element, context, contextArray);
                    } else if (element instanceof List) {
                        processAttrsAsList(component, (List) element, context, contextArray);
                    }
                });
    }

    private void processAttrsAsList(FieldComponent component, List list, PlaceholderContext context, DocumentContext[] contextArray) {
        if (!CollectionUtils.isEmpty(list)) {
            for (int i = 0; i < list.size(); i++) {
                Object element = list.get(i);
                if (element instanceof String) {
                    list.set(i, getValueByContext((String) element, Function.identity(), context, contextArray));
                } else if(element instanceof Map) {
                    processAttrsAsMap(component, (Map<String, Object>) element, context, contextArray);
                } else if (element instanceof List) {
                    processAttrsAsList(component, (List) element, context, contextArray);
                }
            }
        }
    }

    /**
     * @param attrs - атрибуты компонента
     * @param contextArray - контекст поиска значений
     * @see #replaceTextField(String, Map, DocumentContext[])
     */
    private void replaceTextFields(Map<String, Object> attrs, DocumentContext[] contextArray) {
        val pathsToNestedFieldsForReplace = (List<String>)attrs.get("replaceTextFields");
        if(pathsToNestedFieldsForReplace == null) {
            return;
        }
        pathsToNestedFieldsForReplace.forEach(nested -> replaceTextField(nested, attrs, contextArray));
    }

    /**
     * @param nested - путь до вложенного поля, где необходимо проверси замену
     * @param attrs - атрибуты компонента
     * @param contextArray - контекст поиска значений
     * @see #getValueByContext(String, Function, PlaceholderContext, DocumentContext...)
     */
    private void replaceTextField(String nested, Map<String, Object> attrs, DocumentContext[] contextArray) {
        var replaceFieldNode = attrs;
        String[] pathFields = nested.split("\\.");
        for (int i = 0; i < pathFields.length -1; i++) {
            String field = pathFields[i];
            if (replaceFieldNode.get(field) instanceof Map) {
                replaceFieldNode = (Map<String, Object>) replaceFieldNode.get(field);
            } else {
                return;
            }
        }
        String key = pathFields[pathFields.length-1];
        val value = getValueByContext(
                (String) replaceFieldNode.get(key),
                Function.identity(),
                PlaceholderContext.builder().placeholderMap(new HashMap<>()).build(),
                contextArray);
        replaceFieldNode.put(key, value);
    }

    /**
     * Процесс извлечения значений рефов для Clarification объектов
     * @param clValue - clarification объект
     * @param context - контекст плейсхолдеров
     * @param ctxArray - контекст поиска значений
     */
    private void clarificationsRefProcess(Map<String, Object> clValue,
                                          PlaceholderContext context,
                                          DocumentContext[] ctxArray
    ) {
        final String REFS = "refs";
        val refs = clValue.get(REFS);
        if (refs == null) return;

        DocumentContext[] clContextArray = {
                ctxArray[0],
                ctxArray[1],
                ctxArray[2],
                JsonPath.parse(jsonProcessingService.toJson(refs))
        };

        final String TITLE = "title";
        val title = getClarificationValue((String)clValue.get(TITLE), context, clContextArray);
        clValue.put(TITLE, title);
        final String TEXT = "text";
        val text = getClarificationValue((String)clValue.get(TEXT), context, clContextArray);
        clValue.put(TEXT, text);
    }

    /**
     * Замена плейсхолдеров на найденные значения
     * @param value - строка с плейсхолдером
     * @param context - контекст плейсхолдеров
     * @param ctxArray - контекст поиска значений
     * @return строка с вставленными значениями вместо плейсхолдеров
     */
    private String getClarificationValue(String value, PlaceholderContext context, DocumentContext[] ctxArray) {
        var text = updateUrlsByContext(value, context, ctxArray);
        return getValueByContext(text, Function.identity(), context, ctxArray);
    }

    private Map<String, String> getPresetRefsValues(FieldComponent component, ScenarioDto scenarioDto) {
        return PresetRefAttrsType.getAttrNames().stream()
                .filter(refName -> componentContains(component, "${" + refName + "}"))
                .collect(Collectors.toMap(refName -> refName, refName -> getPresetRefValue(refName, scenarioDto)));
    }

    private boolean componentContains(FieldComponent component, String stringForSearch) {
        String fieldGroups = Optional.ofNullable(component.getAttrs())
                .map(attrs -> attrs.get("fieldGroups"))
                .map(jsonProcessingService::toJson)
                .orElse("");
        return Stream.of(component.getLabel(), component.getValue(), fieldGroups).filter(Objects::nonNull)
                .anyMatch(s -> s.contains(stringForSearch));
    }

    /**
     * Вернуть значение предустановленного значения ссылки
     *
     * @param refAttrName   Название атрибута
     * @param scenarioDto   Сценарий
     * @return              Значение сопоставимое атрибуту
     */
    private String getPresetRefValue(String refAttrName, ScenarioDto scenarioDto) {
        PresetRefAttrsType ref = PresetRefAttrsType.get(refAttrName);
        switch (ref) {
            case REF_TO_ORDER_ID:
                return Optional.ofNullable(scenarioDto.getOrderId()).map(Object::toString).orElse("");
            case REF_TO_MASTER_ORDER_ID:
                return Optional.ofNullable(scenarioDto.getMasterOrderId()).map(Object::toString).orElse("");
            case REF_TO_CURRENT_DATE:
                return OffsetDateTime.now().format(dateFormatter);
        }
        return "";
    }

    @Override
    public FormDto.FormDtoBuilder processFieldGroups(FieldComponent component, ScenarioDto scenarioDto) {
        // получаем рефы и группы
        FieldComponentAttrsFactory attrsFactory = new FieldComponentAttrsFactory(component);
        List<FieldGroup> fieldGroups = attrsFactory.getComponentFieldGroups();
        boolean hiddenEmptyGroups = attrsFactory.getBooleanAttr(HIDDEN_EMPTY_GROUPS, true);
        boolean hiddenEmptyFields = attrsFactory.getBooleanAttr(HIDDEN_EMPTY_FIELDS, false);

        // парсинг контекста ответов
        DocumentContext[] contextArray = getContexts(scenarioDto);

        // Обработка всех групп, заданных в json-е
        FormDto.FormDtoBuilder formDtoBuilder = FormDto.builder();
        for(FieldGroup fg : fieldGroups) {
            // Обработка цикличных ответов
            String cycledAnswerId = fg.getAttrs().getCycledAnswerId();
            boolean isCycledGroup = !StringUtils.isEmpty(cycledAnswerId);
            if(isCycledGroup) {
                // Цикличные вопросы для обработки, по умолчанию все цикличные ответы пользователя
                var cycledApplicantAnswersOptional = Optional.ofNullable(scenarioDto.getCycledApplicantAnswers());
                var cycledApplicantAnswerItems = cycledApplicantAnswersOptional.map(answers -> answers.getAnswerOrDefault(cycledAnswerId, null)).map(CycledApplicantAnswer::getItems).orElse(Collections.emptyList());

                // Если в json-е задан индекс, то обрабатывать будем только соответствующий ему ответ
                if(!StringUtils.isEmpty(fg.getAttrs().getCycledAnswerIndex())) {
                    // ищем значение индекса по рефу
                    Reference reference = new Reference();
                    reference.setPath(fg.getAttrs().getCycledAnswerIndex());
                    String indexValue = reference.getNext(contextArray);

                    // Для обработки оставляем только один ответ
                    var answerItemOptional = cycledApplicantAnswersOptional.map(answers -> answers.getAnswerOrDefault(cycledAnswerId, null)).map(answer -> answer.getItemOrDefault(indexValue, null));
                    cycledApplicantAnswerItems = answerItemOptional.map(List::of).orElse(Collections.emptyList());
                }

                // Обработка цикличных ответов
                for(CycledApplicantAnswerItem cycledApplicantAnswerItem : cycledApplicantAnswerItems) {
                    var answersMap = cycledApplicantAnswerItem.getItemAnswers();
                    DocumentContext cycledApplicationContext = JsonPath.parse(jsonProcessingService.convertAnswersToJsonString(answersMap));

                    // обработка и добавление новой группы полей
                    linkedValuesService.fillLinkedValues(component, scenarioDto, cycledApplicationContext);
                    PlaceholderContext context = buildPlaceholderContext(attrsFactory, component, scenarioDto);
                    getStateDto(context, fg, hiddenEmptyGroups, hiddenEmptyFields, cycledApplicationContext).ifPresent(formDtoBuilder::state);
                }
            } else {
                // обработка и добавление новой группы полей (нецикличные ответы)
                linkedValuesService.fillLinkedValues(component, scenarioDto);
                PlaceholderContext context = buildPlaceholderContext(attrsFactory, component, scenarioDto);
                getStateDto(context, fg, hiddenEmptyGroups, hiddenEmptyFields, contextArray).ifPresent(formDtoBuilder::state);
            }
        }
        return formDtoBuilder;
    }

    /**
     * Возвращает группу полей
     * @param context контекст для обработки
     * @param fieldGroup группа полей из json-а
     * @param hiddenEmptyGroups показывать или скрывать группы с полностью пустыми значениями
     * @param hiddenEmptyFields показывать или скрывать поля с пустым значением
     * @param documentContexts контексты для поиска значений рефов
     * @return группу полей
     */
    private Optional<StateDto> getStateDto(PlaceholderContext context, FieldGroup fieldGroup, boolean hiddenEmptyGroups, boolean hiddenEmptyFields,
                                           DocumentContext... documentContexts) {
        StateDto.StateDtoBuilder stateDtoBuilder = StateDto.builder();
        // обработка полей внутри группы
        for(ComponentField field : fieldGroup.getFields()) {
            String label = getValueByContext(field.getLabel(), Function.identity(), context, documentContexts);
            String value = getValueByContext(field.getValue(), Function.identity(), context, documentContexts);
            boolean isLabelHasText = StringUtils.hasText(label);
            boolean isValueHasText = StringUtils.hasText(value);
            if (!isLabelHasText && !isValueHasText || !isValueHasText && hiddenEmptyFields) {
                continue; // полностью отсутствующий field FE не выводит на экран либо нет только значения и выставлен флаг скрытия таких значений
            }
            if (!isValueHasText) {
                value = null; // null в value FE превращает в дефис
            } else if (!isLabelHasText) {
                label = null; // null в label требуется установить, чтобы FE вывел value без label
            }
            FieldDto fieldDto = FieldDto.builder().label(label).value(value).rank(field.getRank()).build();
            stateDtoBuilder.field(fieldDto);
        }
        if (stateDtoBuilder.build().getFields().isEmpty()) {
            return Optional.empty();
        }
        if (hiddenEmptyGroups) {
            boolean isEmptyGroup = stateDtoBuilder.build().getFields().stream().map(FieldDto::getValue).allMatch(Objects::isNull);
            if (isEmptyGroup) {
                return Optional.empty();
            }
        }

        // обработка наименования группы
        String groupName = fieldGroup.getGroupName();
        groupName = getValueByContext(groupName, Function.identity(), context, documentContexts);
        stateDtoBuilder.groupName(groupName);
        stateDtoBuilder.needDivider(fieldGroup.getNeedDivider());
        return Optional.of(stateDtoBuilder.build());
    }

    private String updateUrlsByContext(String value, PlaceholderContext context, DocumentContext... documentContexts) {
        if (StringUtils.isEmpty(value)) {
            return "";
        }
        Matcher urlMatcher = urlPattern.matcher(value);
        while (urlMatcher.find()) {
            String url = urlMatcher.group();
            String updatedUrl = getValueByContext(url, (replaceValue) -> URLEncoder.encode(replaceValue, StandardCharsets.UTF_8), context, documentContexts);
            value = value.replace(url, updatedUrl);
        }
        return value;
    }

    @Override
    public String getValueByContext(String value, PlaceholderContext context,  Map<String, ApplicantAnswer>... answers) {
        String result = value;
        for(var answersMap : answers) {
            DocumentContext answerContext = JsonPath.parse(jsonProcessingService.convertAnswersToJsonString(answersMap));
            result = getValueByContext(result, Function.identity(), context, answerContext);
        }
        return result;
    }

    /**
     * Создается контекст плайсхолдеров
     * @param attrsFactory  фабрика атрибутов компонента
     * @param component     компонент
     * @param scenarioDto   сценарий
     * @return контекст
     */
    @Override
    public PlaceholderContext buildPlaceholderContext(FieldComponentAttrsFactory attrsFactory, FieldComponent component, ScenarioDto scenarioDto) {
        // для рефов с конвертером типа дата нужна таймзона в которую нужно форматировать
        Map<String, Object> additionalAttrs = new HashMap<>();

        if (RequestContextHolder.getRequestAttributes() != null) {
            additionalAttrs.put(TIMEZONE_ATTR_NAME, userCookiesService.getUserTimezone());
        } else {
            additionalAttrs.put(TIMEZONE_ATTR_NAME, DEFAULT_TIMEZONE);
        }

        PlaceholderContext placeholderContext = buildPlaceholderContext(attrsFactory, additionalAttrs);
        Map<String, Placeholder> paramsMap = new HashMap<>();
        attrsFactory.getComponentArguments().forEach((key, value) -> {
            var attribute = new SimplePlaceholder();
            attribute.setKey(key);
            attribute.setValue(Optional.ofNullable(value).orElse(""));
            paramsMap.put(key, attribute);
        });
        getPresetRefsValues(component, scenarioDto).forEach((key, value) -> {
            var attribute = new SimplePlaceholder();
            attribute.setKey(key);
            attribute.setValue(value);
            paramsMap.put(key, attribute);
        });

        placeholderContext.getPlaceholderMap().putAll(paramsMap);

        return placeholderContext;
    }

    @Override
    public PlaceholderContext buildPlaceholderContext(FieldComponent component, ScenarioDto scenarioDto) {
        return buildPlaceholderContext(new FieldComponentAttrsFactory(component), component, scenarioDto);
    }

    /**
     * Создание списка контекстов
     * @param scenarioDto сценарий исполнения услуги
     * @return массив контекстов для поиска
     */
    @Override
    public DocumentContext[] getContexts(ScenarioDto scenarioDto) {
        DocumentContext orderContext = scenarioDto.getOrderId() == null
                ? JsonPath.parse("{\"orderId\": \"\"}")
                : JsonPath.parse(String.format("{\"orderId\": \"%s\"}", scenarioDto.getOrderId()));
        DocumentContext currentValueContext = JsonPath.parse(jsonProcessingService.convertAnswersToJsonString(scenarioDto.getCurrentValue()));
        DocumentContext applicantAnswersContext = JsonPath.parse(jsonProcessingService.convertAnswersToJsonString(scenarioDto.getApplicantAnswers()));
        DocumentContext serviceInfoContext = JsonPath.parse(jsonProcessingService.toJson(scenarioDto.getServiceInfo()));
        return new DocumentContext[]{currentValueContext, applicantAnswersContext, orderContext, serviceInfoContext};
    }
}
