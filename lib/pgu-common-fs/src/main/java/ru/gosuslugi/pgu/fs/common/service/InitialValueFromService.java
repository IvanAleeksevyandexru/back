package ru.gosuslugi.pgu.fs.common.service;

import ru.gosuslugi.pgu.dto.ScenarioDto;
import ru.gosuslugi.pgu.dto.descriptor.FieldComponent;

import java.util.Map;

/**
 * Сервис для вычисления инициализационных значений для компонентов для предустановки из другий полей
 */
public interface InitialValueFromService {

    /**
     * Ключ структуры в attrs компонента
     * Пример: {"id":"sn1aresend","type":"SnilsInput","label":"СНИЛС","attrs":{"preset_from": {"type": "REF","value": "sn1a.snils"} ...
     * Пример: {"id": "add1", "type": "StringInput", "required": true, "label": "Идентификационный номер (VIN)", "attrs": {"preset_from": {"type": "calc","value": "$q1.value == 'Нет' ? $pd4.value.regAddr.fullAddress : $fai17.value.text"},...
     */

    String PRESET_ATTRIBUTE_NAME = "preset_from";

    /**
     * Вычисляет значение для предустановки из другий полей
     * @param component компонент
     * @param scenarioDto dto ценария
     * @return значение или null
     */
    String getValue(FieldComponent component, ScenarioDto scenarioDto);

    String getValue(FieldComponent component, ScenarioDto scenarioDto, Map<String, Object> presetStructureMap);
}
