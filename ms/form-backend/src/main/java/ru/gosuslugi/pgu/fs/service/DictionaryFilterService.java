package ru.gosuslugi.pgu.fs.service;

import ru.gosuslugi.pgu.components.ComponentAttributes;
import ru.gosuslugi.pgu.dto.ApplicantAnswer;
import ru.gosuslugi.pgu.dto.ScenarioDto;
import ru.gosuslugi.pgu.dto.descriptor.FieldComponent;
import ru.gosuslugi.pgu.fs.common.component.ComponentResponse;
import ru.gosuslugi.pgu.fs.utils.AttributeValueTypes;

import java.util.Map;
import java.util.function.Supplier;

/** Сервис для работы с {@link ComponentAttributes#DICTIONARY_FILTER_NAME_ATTR} в различных компонентах. */
public interface DictionaryFilterService {

    /**
     * Генерация мапы с предзаполнение значений из компонента с атрибутом {@link ComponentAttributes#DICTIONARY_FILTER_NAME_ATTR}.
     *
     * @param component компонент, в атрибутах которого есть {@link ComponentAttributes#DICTIONARY_FILTER_NAME_ATTR}
     * @param scenarioDto сценарий
     * @return мапа с вычисленными значениями в фильтре {@link ComponentAttributes#DICTIONARY_FILTER_NAME_ATTR}, вычисление значений типа {@link AttributeValueTypes#CALC}
     */
    Map<String, Object> getInitialValue(FieldComponent component, ScenarioDto scenarioDto);

    /**
     * Используется для предварительной загрузки данных компонента без отображения на скрине.
     * Пример использования: Карта загружается внешней системой заданее за несколько экранов и помещается в кэш,
     * т.к. честная загрузка занимат большое время.
     * @param component предзагружаемый компонент
     * @param scenarioDto сценарий
     * @param supplier поставщик ответа компонента, реализуемый в месте вызова метода
     */
    void preloadComponent(FieldComponent component, ScenarioDto scenarioDto, Supplier<ComponentResponse<String>> supplier);

    /**
     * Осуществляет валидацию после ввода пользователем данных и отправки их через rest api.
     * @param incorrectAnswers мапа некорректных ответов
     * @param entry запись из ответа заявителя
     * @param scenarioDto сценарий
     * @param fieldComponent компонент
     * @param supplier поставщик ответа компонента, реализуемый в месте вызова метода
     */
    void validateAfterSubmit(Map<String, String> incorrectAnswers, Map.Entry<String, ApplicantAnswer> entry, ScenarioDto scenarioDto, FieldComponent fieldComponent,
                             Supplier<ComponentResponse<String>> supplier);
}
