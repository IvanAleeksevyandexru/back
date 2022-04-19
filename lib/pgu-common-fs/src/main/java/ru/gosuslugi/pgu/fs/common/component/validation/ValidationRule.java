package ru.gosuslugi.pgu.fs.common.component.validation;

import org.apache.commons.lang.NotImplementedException;
import ru.gosuslugi.pgu.dto.ApplicantAnswer;
import ru.gosuslugi.pgu.dto.ScenarioDto;
import ru.gosuslugi.pgu.dto.descriptor.FieldComponent;
import ru.gosuslugi.pgu.fs.common.component.BaseComponent;

import java.util.Map;

/**
 * Интерфейс валидации компонентов
 * @see BaseComponent#validate(Map.Entry, ScenarioDto, FieldComponent)
 */
public interface ValidationRule {
    /**
     * Метод осуществляет валидацию компонентов
     * @param entry ответ/выбор заявителя
     * @param fieldComponent компонент
     * @param scenarioDto черновик заявления
     * @return {@code null}, если валидация пройдена успешно, {@code <id_компонента, текст_ошибки>} в случае,
     * если валидация не была пройдена
     */
    @Deprecated // По возможности не использовать этот метод, а заменять обращения к scenarioDto на linkedValues
    default Map.Entry<String, String> validate(Map.Entry<String, ApplicantAnswer> entry, FieldComponent fieldComponent, ScenarioDto scenarioDto) {
        return validate(entry, fieldComponent);
    }

    /**
     * Метод осуществляет валидацию компонентов
     * @param entry ответ/выбор заявителя
     * @param fieldComponent компонент
     * @return {@code null}, если валидация пройдена успешно, {@code <id_компонента, текст_ошибки>} в случае,
     * если валидация не была пройдена
     */
    default Map.Entry<String, String> validate(Map.Entry<String, ApplicantAnswer> entry, FieldComponent fieldComponent) {
        throw new NotImplementedException();
    }
}
