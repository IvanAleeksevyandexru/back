package ru.gosuslugi.pgu.fs.common.component;

import ru.gosuslugi.pgu.dto.ApplicantAnswer;
import ru.gosuslugi.pgu.dto.ScenarioDto;
import ru.gosuslugi.pgu.dto.descriptor.FieldComponent;
import ru.gosuslugi.pgu.dto.descriptor.ServiceDescriptor;
import ru.gosuslugi.pgu.dto.descriptor.types.ComponentType;
import ru.gosuslugi.pgu.fs.common.component.validation.ValidationRule;

import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Интерфейс компонентов-обработчиков. Каждый компонент регистрируется в ComponentRegistry, и может быть найден по
 * типу (enum ComponentType).
 * При создании своего компонента нужно учитывать следующее.
 * 1. Компоненты имею такую иерархию:
 *
 *    BaseComponent
 *          |
 *    AbstractComponent   ---               - базовый родительский класс для обычных компонентов
 *          |        |
 *          |     AbstractCycledComponent   - родительский класс для циклических компонентов
 *          |
 *    AbstractGenderComponent               - родительский класс для гендерных компонентов
 *
 * 2. В жизненном цикле приложение обращается к компонентам дважды в разных запросах:
 * - Из сервиса /getNextStep для получения начального значения полей компонента перед его отображением.
 *   Вызывается метод AbstractComponent.process(), который не нужно переопределять.
 *   В новом компоненте можно переопределить следующие методы (описаны в порядке вызова):
 *
 *      void preProcess(FieldComponent component, ScenarioDto scenarioDto, ServiceDescriptor serviceDescriptor)
 *      void preProcess(FieldComponent component, ScenarioDto scenarioDto)
 *      void preProcess(FieldComponent component)
 *                  - Используется для инициализации или предварительной настройки компонента, когда не требуется возвращать какое-то значение в value
 *
 *      ComponentResponse<InitialValueModel> getInitialValue(FieldComponent component, ScenarioDto scenarioDto, ServiceDescriptor serviceDescriptor)
 *      ComponentResponse<InitialValueModel> getInitialValue(FieldComponent component, ScenarioDto scenarioDto)
 *      ComponentResponse<InitialValueModel> getInitialValue(FieldComponent component)
 *                  - Возвращает начальное значение компонента, которое помещяется в fieldComponent.value.
 *                    Метод раньше назывался preSetComponentValue()
 *
 *      void preValidate(ComponentResponse<InitialValueModel> initialValue, FieldComponent component, ScenarioDto scenarioDto)
 *                  - Используется для валидации начального значения компонента. Например, проверка данных
 *                    из справочника или ЕСИА на соответствие требованиям компонента.
 *
 * ---------------------------------------------------------------------------------------------------------------------
 *
 * - Второй раз приложение обращается к компоненту после ввода пользователем данных. Вызывается метод для валидации ввода:
 *      validate(Map.Entry<String, ApplicantAnswer> entry, ScenarioDto scenarioDto, FieldComponent fieldComponent)
 *   В новом компоненте можно переопределить следующие методы (описаны в порядке вызова):
 *
 *      List<ValidationRule> getValidations()
 *                  - Можно указать список переиспользуемых валидаторов. Предпочтительно использовать именно их, чтобы
 *                    не изобретать велосипеды.
 *
 *      void validateAfterSubmit(Map<String,String> incorrectAnswers, Map.Entry<String, ApplicantAnswer> entry, ScenarioDto scenarioDto, FieldComponent fieldComponent)
 *      void validateAfterSubmit(Map<String,String> incorrectAnswers, Map.Entry<String, ApplicantAnswer> entry, FieldComponent fieldComponent)
 *      void validateAfterSubmit(Map<String,String> incorrectAnswers, String key, String value)
 *                  - вызывается, если вылидаторы из getValidations() отработали без ошибок
 *
 *      void preProcess(FieldComponent component, ScenarioDto scenarioDto, ServiceDescriptor serviceDescriptor)
 *      void preProcess(FieldComponent component, ScenarioDto scenarioDto)
 *      void preProcess(FieldComponent component)
 *                  - вызывается, если на предыдущих шагах ошибок не обнаружилось. Используется для постобработки данных, например,
 *                    для добавления новых атрибутов, если валидация прошла успешно.
 *
 *
 * @param <InitialValueModel> модель, которую возвращает метод getInitialValue()
 */
public interface BaseComponent<InitialValueModel> {

    ComponentType getType();

    /**
     * @return true для циклического компонента
     */
    default boolean isCycled() {
        return false;
    }

    /**
     * Цикл обработки {@code component}. Включает в себя обработку гендерности, обработку всех видов референсов
     * ({@code linkedValues}, {@code refs}, {@code placeholder}, {@code presetReference}), обработку получение getInitialValue
     */
    void process(FieldComponent component, ScenarioDto scenarioDto, ServiceDescriptor serviceDescriptor);

    void preProcess(FieldComponent component, ScenarioDto scenarioDto, ServiceDescriptor serviceDescriptor);

    default ComponentResponse<InitialValueModel> getInitialValue(FieldComponent component, ScenarioDto scenarioDto, ServiceDescriptor serviceDescriptor) {
        return getInitialValue(component, scenarioDto);
    }

    default ComponentResponse<InitialValueModel> getInitialValue(FieldComponent component, ScenarioDto scenarioDto) {
        return getInitialValue(component);
    }

    default ComponentResponse<InitialValueModel> getInitialValue(FieldComponent component) {
        return ComponentResponse.empty();
    }

    /**
     * Возвращает список валидаций, которые необходимо осуществлять в компоненте. По умолчанию реализации отсутствуют
     * @return список валидаций
     */
    default List<ValidationRule> getValidations() {
        return Collections.emptyList();
    }

    /**
     * Базовая реализация валидации компонентов. Валидации вызываются последовательно в соответствии с определениями в
     * методе {@link #getValidations()}. В сообщение попадает первая непройденная валидация компонента, последующие
     * валидации не отрабатывают.
     * @param entry ответ/выбор заявителя
     * @param scenarioDto сценарий
     * @param fieldComponent компонент
     * @return пары {@code <id_компонента, текст_ошибки>}, если валидация не пройдена, пустую {@code Map}, если
     * пройдена
     * @see #getValidations()
     */
    Map<String, String> validate(Map.Entry<String, ApplicantAnswer> entry,
            ScenarioDto scenarioDto,
            FieldComponent fieldComponent);

    List<List<Map<String, String>>> validateItemsUniqueness(Map.Entry<String, ApplicantAnswer> entry,
                                 ScenarioDto scenarioDto,
                                 FieldComponent fieldComponent);

    /**
     * Значение, которое сохраняется в черновик, если экран пропущен (вычисляется по skipConditions)
     */
    default String getDefaultAnswer(FieldComponent fieldComponent) {
        return fieldComponent.getValue();
    }

    /**
     * Используется для предварительной загрузки данных компонента без отображения на скрине.
     * Пример использования: Карта загружается внешней системой заранее за несколько экранов и помещается в кэш,
     * т.к. честная загрузка занимает большое время.
     */
    default void preloadComponent(FieldComponent component, ScenarioDto scenarioDto) {}

    /**
     * В этом методе можно определить должен ли компонент быть сохранен в cachedAnswers
     * @param component
     * @param scenarioDto
     * @return
     */
    default boolean shouldBeSavedInCachedAnswers(FieldComponent component, ScenarioDto scenarioDto) {return true;};
}
