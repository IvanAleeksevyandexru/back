package ru.gosuslugi.pgu.fs.common.component;


import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import ru.gosuslugi.pgu.dto.descriptor.types.ComponentType;
import ru.gosuslugi.pgu.fs.common.exception.ConfigurationException;

import java.util.List;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

import static ru.gosuslugi.pgu.common.core.logger.LoggerUtil.*;

@Slf4j
@Component
public class ComponentRegistry {

    private final ConcurrentHashMap<ComponentType, BaseComponent<?>> componentRegistry = new ConcurrentHashMap<>();

    @Autowired
    public void setComponents(List<BaseComponent<?>> components) {
        components.forEach(this::register);
    }

    private void register(BaseComponent<?> component) {
        ComponentType componentType = component.getType();
        BaseComponent<?> oldcomponent = componentRegistry.putIfAbsent(componentType, component);
        if(Objects.nonNull(oldcomponent)){
            String errorMessage = String.format(
                    "Обработчик %s для компонента %s уже зарегистрирован: %s",
                    component.getClass().getCanonicalName(),
                    componentType,
                    oldcomponent.getClass().getCanonicalName());
            error(log, () -> errorMessage);
            throw new ConfigurationException(errorMessage);
        }
    }

    public BaseComponent<?> getComponent(ComponentType componentType){
        return componentRegistry.get(componentType);
    }
}
