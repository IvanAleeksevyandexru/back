package ru.gosuslugi.pgu.fs.component.video;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.gosuslugi.pgu.dto.descriptor.types.ComponentType;
import ru.gosuslugi.pgu.fs.common.component.AbstractComponent;

@Component
@RequiredArgsConstructor
@Slf4j
public class IdentificationStreamComponent extends AbstractComponent<String> {
    @Override
    public ComponentType getType() {
        return ComponentType.IdentificationStreamComponent;
    }
}
