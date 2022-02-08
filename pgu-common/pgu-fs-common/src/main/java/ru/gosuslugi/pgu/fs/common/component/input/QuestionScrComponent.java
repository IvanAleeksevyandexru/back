package ru.gosuslugi.pgu.fs.common.component.input;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import ru.gosuslugi.pgu.dto.cycled.CycledApplicantAnswer;
import ru.gosuslugi.pgu.dto.cycled.CycledApplicantAnswerItem;
import ru.gosuslugi.pgu.dto.ScenarioDto;
import ru.gosuslugi.pgu.components.FieldComponentUtil;
import ru.gosuslugi.pgu.dto.descriptor.CycledAttrs;
import ru.gosuslugi.pgu.dto.descriptor.FieldComponent;
import ru.gosuslugi.pgu.components.descriptor.attr_factory.FieldComponentAttrsFactory;
import ru.gosuslugi.pgu.components.descriptor.placeholder.PlaceholderContext;
import ru.gosuslugi.pgu.components.descriptor.types.Action;
import ru.gosuslugi.pgu.dto.descriptor.types.ComponentType;
import ru.gosuslugi.pgu.fs.common.component.AbstractComponent;

import java.util.*;

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
        PlaceholderContext context = componentReferenceService.buildPlaceholderContext(attrsFactory, component, scenarioDto);
        replicateActionsOrAnswers(component, scenarioDto, FieldComponentUtil.ACTIONS_ATTR_KEY, attrsFactory, context);
        replicateActionsOrAnswers(component, scenarioDto, FieldComponentUtil.ANSWERS_ATTR_KEY, attrsFactory, context);
    }

    private void replicateActionsOrAnswers(FieldComponent component, ScenarioDto scenarioDto, String attributeName, FieldComponentAttrsFactory attrsFactory, PlaceholderContext context) {
        List<Map<String, Object>> buf = new ArrayList<>();

        @SuppressWarnings("unchecked")
        var replicationItemList = (List<Map<String, Object>>) Optional.ofNullable(component.getAttrs())
                .orElse(Collections.emptyMap())
                .getOrDefault(attributeName, new ArrayList<>());

        for (Map<String, Object> actionMap : replicationItemList) {
            Action action = attrsFactory.createAction(actionMap);
            CycledAttrs cycledAttrs = action.getAttrs();

            if (!StringUtils.isEmpty(cycledAttrs.getCycledAnswerId())) {
                var cycledApplicantAnswerItems = getCycledApplicantAnswerItemsByCycledAnswerId(scenarioDto, cycledAttrs.getCycledAnswerId());
                for (CycledApplicantAnswerItem cycledApplicantAnswerItem : cycledApplicantAnswerItems) {
                    buf.add(createActions(action, context, cycledApplicantAnswerItem));
                }
            } else {
                // Обновляем лейбл action-а плейсхолдерами
                actionMap.put("label", componentReferenceService.getValueByContext((String) actionMap.get("label"), context, scenarioDto.getApplicantAnswers(), scenarioDto.getCurrentValue()));
                buf.add(actionMap);
            }
        }
        component.getAttrs().put(attributeName, buf);
    }

    private List<CycledApplicantAnswerItem> getCycledApplicantAnswerItemsByCycledAnswerId(ScenarioDto scenarioDto, String cycledAnswerId) {
        var cycledApplicantAnswersOptional = Optional.ofNullable(scenarioDto.getCycledApplicantAnswers());
        return cycledApplicantAnswersOptional.map(answers -> answers.getAnswerOrDefault(cycledAnswerId, null))
                .map(CycledApplicantAnswer::getItems)
                .orElse(Collections.emptyList());
    }

    private Map<String, Object> createActions(Action cycledAction, PlaceholderContext context, CycledApplicantAnswerItem cycledApplicantAnswerItem) {
        var answersMap = cycledApplicantAnswerItem.getItemAnswers();
        Map<String, Object> actionToAdd = new LinkedHashMap<>();
        actionToAdd.put("action", cycledAction.getAction());
        actionToAdd.put("label", componentReferenceService.getValueByContext(cycledAction.getLabel(), context, answersMap));
        actionToAdd.put("value", cycledApplicantAnswerItem.getId());
        actionToAdd.put("type", cycledAction.getType());
        return actionToAdd;
    }

}
