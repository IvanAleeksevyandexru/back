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
import ru.gosuslugi.pgu.fs.common.service.ComponentReferenceService;

import java.util.*;

@Slf4j
@Component
@RequiredArgsConstructor
public class QuestionScrComponent extends AbstractComponent<String> {

    private final ComponentReferenceService componentReferenceService;

    @Override
    public ComponentType getType() {
        return ComponentType.QuestionScr;
    }

    /**
     * Выполняет тиражирование action-ов для цикличных ответов
     * @param component компонент
     * @param scenarioDto DTO
     * @return предустановленное значение для компонента
     * @see Action
     */
    @Override
    protected void preProcess(FieldComponent component, ScenarioDto scenarioDto) {
        FieldComponentAttrsFactory attrsFactory = new FieldComponentAttrsFactory(component);
        var actionList = (List<Map<String, Object>>)Optional.ofNullable(component.getAttrs()).orElse(Collections.emptyMap())
                .getOrDefault(FieldComponentUtil.ACTIONS_ATTR_KEY, new ArrayList<>());

        PlaceholderContext context = componentReferenceService.buildPlaceholderContext(attrsFactory, component, scenarioDto);

        for(int actionIndex = 0; actionIndex < actionList.size(); actionIndex++) {
            var actionMap = actionList.get(actionIndex);
            Action action = attrsFactory.createAction(actionMap);
            CycledAttrs cycledAttrs = action.getAttrs();

            boolean isCycled = !StringUtils.isEmpty(cycledAttrs.getCycledAnswerId());
            if(isCycled) {
                // Удаляем из action-ов action, который нужно растиражировать на основе цикличного блока
                Action cycledAction = attrsFactory.createAction(actionList.remove(actionIndex--));

                // Цикличный блок с ответами
                var cycledApplicantAnswersOptional = Optional.ofNullable(scenarioDto.getCycledApplicantAnswers());
                var cycledApplicantAnswerItems = cycledApplicantAnswersOptional.map(answers -> answers.getAnswerOrDefault(cycledAttrs.getCycledAnswerId(), null)).map(CycledApplicantAnswer::getItems).orElse(Collections.emptyList());

                // Добавление новых action-ов на основе ответов в цикличном блоке
                for(CycledApplicantAnswerItem cycledApplicantAnswerItem : cycledApplicantAnswerItems) {
                    var answersMap = cycledApplicantAnswerItem.getItemAnswers();

                    // Добавление нового action-а
                    Map<String, Object> actionToAdd = new LinkedHashMap<>();
                    actionToAdd.put("action", cycledAction.getAction());
                    actionToAdd.put("label", componentReferenceService.getValueByContext(cycledAction.getLabel(), context, answersMap));
                    actionToAdd.put("value", cycledApplicantAnswerItem.getId());
                    actionToAdd.put("type", cycledAction.getType());
                    actionList.add(++actionIndex, actionToAdd);
                }
            } else {
                // Обновляем лейбл action-а плейсхолдерами
                actionMap.put("label", componentReferenceService.getValueByContext((String)actionMap.get("label"), context, scenarioDto.getApplicantAnswers(), scenarioDto.getCurrentValue()));
            }
        }

        // TODO: нужно вынести в отдельный метод. DRY
        var answersList = (List<Map<String, Object>>)Optional.ofNullable(component.getAttrs()).orElse(Collections.emptyMap())
                .getOrDefault(FieldComponentUtil.ANSWERS_ATTR_KEY, new ArrayList<>());

        for(int answerIndex = 0; answerIndex < answersList.size(); answerIndex++) {
            var answerMap = answersList.get(answerIndex);
            Action answerItem = attrsFactory.createAction(answerMap);
            CycledAttrs cycledAttrs = answerItem.getAttrs();

            boolean isCycled = !StringUtils.isEmpty(cycledAttrs.getCycledAnswerId());
            if(isCycled) {
                // Удаляем из action-ов action, который нужно растиражировать на основе цикличного блока
                Action cycledAnswer = attrsFactory.createAction(answersList.remove(answerIndex--));

                // Цикличный блок с ответами
                var cycledApplicantAnswersOptional = Optional.ofNullable(scenarioDto.getCycledApplicantAnswers());
                var cycledApplicantAnswerItems = cycledApplicantAnswersOptional.map(answers -> answers.getAnswerOrDefault(cycledAttrs.getCycledAnswerId(), null)).map(CycledApplicantAnswer::getItems).orElse(Collections.emptyList());

                // Добавление новых action-ов на основе ответов в цикличном блоке
                for(CycledApplicantAnswerItem cycledApplicantAnswerItem : cycledApplicantAnswerItems) {
                    var answersMap = cycledApplicantAnswerItem.getItemAnswers();

                    // Добавление нового action-а
                    Map<String, Object> actionToAdd = new LinkedHashMap<>();
                    actionToAdd.put("action", cycledAnswer.getAction());
                    actionToAdd.put("label", componentReferenceService.getValueByContext(cycledAnswer.getLabel(), context, answersMap));
                    actionToAdd.put("value", cycledApplicantAnswerItem.getId());
                    actionToAdd.put("type", cycledAnswer.getType());
                    answersList.add(++answerIndex, actionToAdd);
                }
            } else {
                // Обновляем лейбл action-а плейсхолдерами
                answerMap.put("label", componentReferenceService.getValueByContext((String)answerMap.get("label"), context, scenarioDto.getApplicantAnswers(), scenarioDto.getCurrentValue()));
            }
        }
    }
}
