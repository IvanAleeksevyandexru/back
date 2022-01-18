package ru.gosuslugi.pgu.fs.common.service;

import com.jayway.jsonpath.DocumentContext;
import ru.gosuslugi.pgu.dto.ApplicantAnswer;
import ru.gosuslugi.pgu.dto.ScenarioDto;
import ru.gosuslugi.pgu.dto.descriptor.FieldComponent;
import ru.gosuslugi.pgu.components.descriptor.attr_factory.FieldComponentAttrsFactory;
import ru.gosuslugi.pgu.components.descriptor.placeholder.PlaceholderContext;
import ru.gosuslugi.pgu.components.dto.FormDto;

import java.util.Map;

public interface ComponentReferenceService extends ReferenceService {

    void processComponentRefs(FieldComponent component, ScenarioDto scenarioDto);

    /**
     * Выполняет формирование групп полей для вывода в интерфейсе
     * @param component компонент
     * @param scenarioDto DTO
     * @return билдер групп полей
     */
    FormDto.FormDtoBuilder processFieldGroups(FieldComponent component, ScenarioDto scenarioDto);

    /**
     * Создается контекст плайсхолдеров
     * @param attrsFactory фабрика атрибутов компонента
     * @return контекст
     */
    PlaceholderContext buildPlaceholderContext(FieldComponentAttrsFactory attrsFactory, FieldComponent component, ScenarioDto scenarioDto);

    /**
     * Осуществляет замену плейсхолдеров в передаваемом значении
     * @param value значение, в котором будет осуществлена замена плейсхолдеров
     * @param context контекст для замены
     * @param answers ответы пользователя
     * @return измененное значение
     */
    String getValueByContext(String value, PlaceholderContext context,  Map<String, ApplicantAnswer>... answers);
    PlaceholderContext buildPlaceholderContext(FieldComponent component, ScenarioDto scenarioDto);
    DocumentContext[] getContexts(ScenarioDto scenarioDto);
}
