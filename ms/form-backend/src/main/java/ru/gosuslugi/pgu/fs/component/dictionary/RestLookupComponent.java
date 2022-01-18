package ru.gosuslugi.pgu.fs.component.dictionary;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import ru.gosuslugi.pgu.dto.descriptor.types.ComponentType;
import ru.gosuslugi.pgu.fs.common.component.AbstractComponent;

import java.util.Map;

import static ru.gosuslugi.pgu.dto.descriptor.types.ComponentType.RestLookup;

@Slf4j
@Component
@RequiredArgsConstructor
public class RestLookupComponent extends AbstractComponent<String> {
    @Override
    public ComponentType getType() {
        return RestLookup;
    }

    @Override
    protected void validateAfterSubmit(Map<String, String> incorrectAnswers, String key, String value) {
        if (StringUtils.isEmpty(value)) {
            incorrectAnswers.put(key, "Поле обязательно для заполнения");
        }
    }
}
