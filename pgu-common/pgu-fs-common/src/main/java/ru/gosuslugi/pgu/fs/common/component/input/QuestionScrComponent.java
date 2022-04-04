package ru.gosuslugi.pgu.fs.common.component.input;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jayway.jsonpath.JsonPath;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.minidev.json.JSONArray;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import ru.gosuslugi.pgu.common.core.json.JsonProcessingUtil;
import ru.gosuslugi.pgu.components.FieldComponentUtil;
import ru.gosuslugi.pgu.components.descriptor.attr_factory.FieldComponentAttrsFactory;
import ru.gosuslugi.pgu.components.descriptor.placeholder.PlaceholderContext;
import ru.gosuslugi.pgu.components.descriptor.types.Action;
import ru.gosuslugi.pgu.dto.ScenarioDto;
import ru.gosuslugi.pgu.dto.cycled.CycledApplicantAnswer;
import ru.gosuslugi.pgu.dto.cycled.CycledApplicantAnswerItem;
import ru.gosuslugi.pgu.dto.descriptor.CycledAttrs;
import ru.gosuslugi.pgu.dto.descriptor.FieldComponent;
import ru.gosuslugi.pgu.dto.descriptor.types.ComponentType;
import ru.gosuslugi.pgu.fs.common.component.AbstractComponent;
import ru.gosuslugi.pgu.fs.common.exception.FormBaseException;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
public class QuestionScrComponent extends AbstractComponent<String> {

    @Override
    public ComponentType getType() {
        return ComponentType.QuestionScr;
    }

    /**
     * Выполняет тиражирование action-ов для цикличных ответов.
     * Может перевычислить и перезаписать значения атрибутов actions и answers у компонента.
     *
     * @param component   компонент
     * @param scenarioDto DTO
     * @see Action
     */
    @Override
    protected void preProcess(FieldComponent component, ScenarioDto scenarioDto) {
        FieldComponentAttrsFactory attrsFactory = new FieldComponentAttrsFactory(component);
        PlaceholderContext context = componentReferenceService.buildPlaceholderContext(
                attrsFactory,
                component,
                scenarioDto
        );
        replicateActionsOrAnswers(component, scenarioDto, FieldComponentUtil.ACTIONS_ATTR_KEY, attrsFactory, context);
        replicateActionsOrAnswers(component, scenarioDto, FieldComponentUtil.ANSWERS_ATTR_KEY, attrsFactory, context);
    }

    @SuppressWarnings("unchecked")
    private void replicateActionsOrAnswers(
            FieldComponent component,
            ScenarioDto scenarioDto,
            String attributeName,
            FieldComponentAttrsFactory attrsFactory,
            PlaceholderContext context
    ) {
        List<Map<String, Object>> buf = new ArrayList<>();

        var replicationItemList = (List<Map<String, Object>>) Optional.ofNullable(component.getAttrs())
                .map(attrs -> attrs.get(attributeName))
                .orElse(Collections.emptyList());

        for (Map<String, Object> actionMap : replicationItemList) {
            final Action action = attrsFactory.createAction(actionMap);
            final CycledAttrs cycledAttrs = action.getAttrs();
            final String pathToArray = action.getPathToArray();

            if (!StringUtils.isEmpty(cycledAttrs.getCycledAnswerId())) {
                getCycledApplicantAnswerItemsByCycledAnswerId(scenarioDto, cycledAttrs.getCycledAnswerId()).stream()
                        .map(answerItem -> createActions(action, context, answerItem))
                        .forEach(buf::add);
            } else if (!StringUtils.isEmpty(pathToArray)) {
                parseArrayFromAnswers(scenarioDto, pathToArray)
                        .stream()
                        .map(child -> createActions(action, context, child))
                        .forEach(buf::add);
            } else {
                // Обновляем лейбл action-а плейсхолдерами
                actionMap.put(
                        "label",
                        componentReferenceService.getValueByContext(
                                (String) actionMap.get("label"),
                                context,
                                scenarioDto.getApplicantAnswers(),
                                scenarioDto.getCurrentValue()
                        )
                );
                buf.add(actionMap);
            }
        }
        component.getAttrs().put(attributeName, buf);
    }

    private List<CycledApplicantAnswerItem> getCycledApplicantAnswerItemsByCycledAnswerId(
            ScenarioDto scenarioDto,
            String cycledAnswerId
    ) {
        var cycledApplicantAnswersOptional = Optional.ofNullable(scenarioDto.getCycledApplicantAnswers());
        return cycledApplicantAnswersOptional.map(answers -> answers.getAnswerOrDefault(cycledAnswerId, null))
                .map(CycledApplicantAnswer::getItems)
                .orElse(Collections.emptyList());
    }

    @SuppressWarnings("unchecked")
    private List<Map<String, Object>> parseArrayFromAnswers(ScenarioDto scenarioDto, String pathToArray) {
        final String answersJson = jsonProcessingService.convertAnswersToJsonString(scenarioDto.getApplicantAnswers());
        final ObjectMapper objectMapper = JsonProcessingUtil.getObjectMapper();
        List<Map<String, Object>> answerMapList = new ArrayList<>();

        final Object array = JsonPath.parse(answersJson).read(pathToArray);

        if (array instanceof JSONArray) {
            return (List<Map<String, Object>>) array;
        } else if (array instanceof String) {
            try {
                objectMapper.readTree(array.toString()).elements().forEachRemaining(element -> answerMapList.add(
                        ((Map<String, Object>) objectMapper.convertValue(element, HashMap.class))
                ));
            } catch (JsonProcessingException e) {
                throw new FormBaseException("Ошибка парсинга json массива");
            }
        }

        return answerMapList;
    }

    private Map<String, Object> createActions(
            Action action,
            PlaceholderContext context,
            Map<String, Object> items
    ) {
        Map<String, Object> actionToAdd = new LinkedHashMap<>();
        actionToAdd.put("action", action.getAction());
        actionToAdd.put("label", componentReferenceService.getValueByContext(action.getLabel(), context, items));
        actionToAdd.put("value", jsonProcessingService.toJson(items));
        actionToAdd.put("type", action.getType());
        return actionToAdd;
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> createActions(
            Action action,
            PlaceholderContext context,
            CycledApplicantAnswerItem cycledApplicantAnswerItem
    ) {
        Map<String, Object> actionToAdd = new LinkedHashMap<>();
        actionToAdd.put("action", action.getAction());
        actionToAdd.put(
                "label",
                componentReferenceService.getValueByContext(
                        action.getLabel(),
                        context,
                        cycledApplicantAnswerItem.getItemAnswers()
                )
        );
        actionToAdd.put("value", cycledApplicantAnswerItem.getId());
        actionToAdd.put("type", action.getType());
        return actionToAdd;
    }
}
