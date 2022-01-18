package ru.gosuslugi.pgu.fs.component;

import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import ru.gosuslugi.pgu.dto.ApplicantAnswer;
import ru.gosuslugi.pgu.dto.ScenarioDto;
import ru.gosuslugi.pgu.common.esia.search.dto.UserPersonalData;
import ru.gosuslugi.pgu.dto.descriptor.FieldComponent;
import ru.gosuslugi.pgu.dto.descriptor.ServiceDescriptor;
import ru.gosuslugi.pgu.dto.descriptor.types.ComponentType;
import ru.gosuslugi.pgu.fs.common.component.AbstractComponent;
import ru.gosuslugi.pgu.fs.common.component.ComponentRegistry;
import ru.gosuslugi.pgu.fs.common.component.ComponentResponse;
import ru.gosuslugi.pgu.fs.helper.GenderHelper;

import java.util.Map;
import java.util.Objects;

/**
 * Базовый класс для преобразования гендерных компонентов
 * @see GenderHelper
 * @see ComponentType
 */
@Slf4j
public abstract class AbstractGenderComponent<InitialValueModel> extends AbstractComponent<InitialValueModel> implements GenderHelper {

    @Lazy
    @Autowired
    @Setter
    private ComponentRegistry componentRegistry;

    @Autowired
    @Setter
    protected UserPersonalData userPersonalData;


    /**
     * Целевой тип к которому нужно привести гендерный компонент
     * @return целевой тип
     * @see #getTargetComponentType()
     */
    protected abstract ComponentType getTargetComponentType();

    @Override
    public void process(FieldComponent component, ScenarioDto scenarioDto, ServiceDescriptor serviceDescriptor) {
        processGender(component, scenarioDto);
        super.process(component, scenarioDto, serviceDescriptor);
    }

    /**
     * Метод преобразует компонент в указанный тип и заполняет соответствующие поля на основе гендерности
     * @param component компонент для преобзарования и заполнения значениями
     * @param scenarioDto сценарий
     */
    @Override
    public void processGender(FieldComponent component, ScenarioDto scenarioDto) {
        component.setType(getTargetComponentType());
        String gender = Objects.nonNull(userPersonalData.getUserId()) && Objects.nonNull(userPersonalData.getPerson()) ? userPersonalData.getPerson().getGender() : null;
        component.setAttrs(processAttrs(gender, component.getAttrs()));
        component.setLabel(getLabel(gender, component));
    }

    protected Map<String, Object> processAttrs(String gender, Map<String, Object> attrs) {
        return attrs;
    }

    /**
     * Осуществляет предустановку значений, используя алгоритм целевого компонента
     * @param component компонент
     * @param scenarioDto сценарий
     * @param serviceDescriptor описание сервиса
     * @return предустановленное значение компонента
     */
    @Override
    @SuppressWarnings("unchecked")
    public ComponentResponse<InitialValueModel> getInitialValue(FieldComponent component, ScenarioDto scenarioDto, ServiceDescriptor serviceDescriptor) {
        AbstractComponent<?> targetComponent = (AbstractComponent<?>) componentRegistry.getComponent(this.getTargetComponentType());
        return (ComponentResponse<InitialValueModel>) (Objects.nonNull(targetComponent) ?
                targetComponent.getInitialValue(component, scenarioDto, serviceDescriptor) :
                super.getInitialValue(component, scenarioDto, serviceDescriptor));
    }

    /**
     * Осуществляет валидацию, используя алгоритм целевого компонента
     * @param entry ответы заявителя
     * @param scenarioDto сценарий
     * @param fieldComponent компонент
     * @return ошибки валидации
     * @see #getTargetComponentType()
     */
    @Override
    public Map<String, String> validate(Map.Entry<String, ApplicantAnswer> entry, ScenarioDto scenarioDto, FieldComponent fieldComponent) {
        AbstractComponent<?> targetComponent = (AbstractComponent<?>) componentRegistry.getComponent(this.getTargetComponentType());
        return Objects.nonNull(targetComponent) ?
                targetComponent.validate(entry, scenarioDto, fieldComponent) :
                super.validate(entry, scenarioDto, fieldComponent);
    }
}
