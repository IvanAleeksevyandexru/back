package ru.gosuslugi.pgu.fs.component.gender;

import org.springframework.stereotype.Component;
import ru.gosuslugi.pgu.dto.descriptor.types.ComponentType;

import static ru.gosuslugi.pgu.dto.descriptor.types.ComponentType.GMultipleChoiceDictionary;
import static ru.gosuslugi.pgu.dto.descriptor.types.ComponentType.MultipleChoiceDictionary;

/**
 * Компонент "Список с возможностью множественного выбора"
 */
@Component
public class MultipleChoiceDictionaryGenderComponent extends AbstractDictionaryGenderComponent<String> {

    @Override
    public ComponentType getType() {
        return GMultipleChoiceDictionary;
    }

    @Override
    protected ComponentType getTargetComponentType() {
        return MultipleChoiceDictionary;
    }
}