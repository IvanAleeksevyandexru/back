package ru.gosuslugi.pgu.fs.common.component.input;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.gosuslugi.pgu.dto.descriptor.types.ComponentType;
import ru.gosuslugi.pgu.fs.common.component.AbstractComponent;

/**
 * Helper class for HtmlString component
 * that inserts references during preset process
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class HtmlStringComponent extends AbstractComponent<String> {
    @Override
    public ComponentType getType() {
        return ComponentType.HtmlString;
    }
}
