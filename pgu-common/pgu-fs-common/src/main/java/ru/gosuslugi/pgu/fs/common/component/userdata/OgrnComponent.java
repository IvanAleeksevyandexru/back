package ru.gosuslugi.pgu.fs.common.component.userdata;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.gosuslugi.pgu.dto.descriptor.types.ComponentType;
import ru.gosuslugi.pgu.fs.common.component.AbstractComponent;
import ru.gosuslugi.pgu.fs.common.component.validation.EmptyOr;
import ru.gosuslugi.pgu.fs.common.component.validation.RequiredNotBlankValidation;
import ru.gosuslugi.pgu.fs.common.component.validation.ValidationRule;
import ru.gosuslugi.pgu.fs.common.component.validation.ValidationRuleImpl;
import ru.gosuslugi.pgu.fs.common.utils.AnswerUtil;
import ru.gosuslugi.pgu.fs.common.utils.OgrnUtil;

import java.util.List;

import static ru.gosuslugi.pgu.components.ValidationUtil.mapEntry;

/**
 * Класс содержит вспомогательные методы компонента ОГРН
 * - в последующем компонент должен быть отнаследован от StringInput
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class OgrnComponent extends AbstractComponent<String> {

    private static final String DEFAULT_ERROR_MESSAGE = "Некорректный ОГРН";

    @Override
    public ComponentType getType() {
        return ComponentType.OgrnInput;
    }

    @Override
    public List<ValidationRule> getValidations() {
        return List.of(
                new RequiredNotBlankValidation(getErrorText()),
                new EmptyOr(new ValidationRuleImpl((entry, fieldComponent) -> {
                    if (!OgrnUtil.checkOgrn(isOgrnip(), AnswerUtil.getValue(entry))) {
                        return mapEntry(entry.getKey(), getErrorText());
                    }
                    return null;
                }))
        );
    }

    public boolean isOgrnip() {
        return false;
    }

    /**
     * Получить текст ошибки по умолчанию
     *
     * @return
     */
    protected String getErrorText() {
        return DEFAULT_ERROR_MESSAGE;
    }

}
