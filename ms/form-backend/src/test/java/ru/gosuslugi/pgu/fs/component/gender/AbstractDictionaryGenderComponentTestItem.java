package ru.gosuslugi.pgu.fs.component.gender;

import ru.gosuslugi.pgu.dto.descriptor.types.ComponentType;

/**
 * Класс для проверки метода processAttrs() в классе AbstractDictionaryGenderComponent
 * @see AbstractDictionaryGenderComponent#processAttrs(java.lang.String, java.util.Map)
 */
public class AbstractDictionaryGenderComponentTestItem extends  AbstractDictionaryGenderComponent{

    public AbstractDictionaryGenderComponentTestItem() {
    }

    @Override
    protected ComponentType getTargetComponentType() {
        return null;
    }

    @Override
    public ComponentType getType() {
        return null;
    }
}
