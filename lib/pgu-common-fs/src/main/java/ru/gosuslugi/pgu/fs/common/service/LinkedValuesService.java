package ru.gosuslugi.pgu.fs.common.service;

import com.jayway.jsonpath.DocumentContext;
import ru.gosuslugi.pgu.components.descriptor.attr_factory.AttrsFactory;
import ru.gosuslugi.pgu.dto.DisplayRequest;
import ru.gosuslugi.pgu.dto.ScenarioDto;
import ru.gosuslugi.pgu.dto.descriptor.FieldComponent;
import ru.gosuslugi.pgu.dto.descriptor.LinkedValue;

public interface LinkedValuesService {

    /**
     * Заполнение добавочных оргументов в LinkedValues компонента
     * @param fieldComponent
     * @param scenarioDto
     * @param externalContexts внешние контексты
     */
    void fillLinkedValues(FieldComponent fieldComponent, ScenarioDto scenarioDto, DocumentContext... externalContexts);

    /**
     * Заполнение добавочных оргументов в LinkedValues экрана
     * @param displayRequest
     * @param scenarioDto
     */
    void fillLinkedValues(DisplayRequest displayRequest, ScenarioDto scenarioDto);

    /**
     * Возвращает значение конкретного linkedValue
     * @param linkedValue - выражение
     * @param scenarioDto - сценарий
     * @param attrsFactory - фактория для обработки реф ссылок
     * @return вычисленное значение
     * @param externalContexts внешние контексты
     */
    String getValue(LinkedValue linkedValue, ScenarioDto scenarioDto, AttrsFactory attrsFactory, DocumentContext... externalContexts);

}
