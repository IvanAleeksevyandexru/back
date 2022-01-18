package ru.gosuslugi.pgu.fs.service.validation;

import ru.gosuslugi.pgu.dto.ApplicantAnswer;
import ru.gosuslugi.pgu.dto.ScenarioDto;
import ru.gosuslugi.pgu.dto.descriptor.FieldComponent;
import ru.gosuslugi.pgu.fs.common.component.ComponentResponse;
import ru.gosuslugi.pgu.fs.service.validation.impl.DictType;

import java.util.Map;
import java.util.function.Supplier;

/**
 * Базовый интерфейс стратегии для осуществления проверок выбранного пользователем значения
 */
public interface DictionaryFilterValidationStrategy {

    /**
     * Метод возвращающий тип словаря в рамках которого реализуется стратегия
     *
     * @return тип словаря
     * @implSpec Должен быть переопределен в классах реализаций конкретных стратегий
     */
    default DictType getDictUrlType() {
        return DictType.dictionary;
    }

    /**
     * Метод описывающий валидацию выбранного пользователем значения в рамках интеграции с сервисами словарей
     *
     * @param incorrectAnswers отображение ошибок валидации
     * @param entry            отображение id компонента и выбранного пользователем значения
     * @param scenarioDto      передаваемая модель сценария услуги
     * @param fieldComponent   комопнент
     * @param supplier         метод возвращающий предзаполненное значение
     */
    void validateAfterSubmit(Map<String, String> incorrectAnswers,
                             Map.Entry<String, ApplicantAnswer> entry,
                             ScenarioDto scenarioDto,
                             FieldComponent fieldComponent,
                             Supplier<ComponentResponse<String>> supplier);
}
