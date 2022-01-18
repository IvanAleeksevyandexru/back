package ru.gosuslugi.pgu.fs.component.gender;

import ru.gosuslugi.pgu.fs.component.AbstractGenderComponent;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static ru.gosuslugi.pgu.components.ComponentAttributes.DICTIONARY_NAME_ATTR;

public abstract class AbstractDictionaryGenderComponent<InitialValueModel> extends AbstractGenderComponent<InitialValueModel> {

    @Override
    public Map<String, Object> processAttrs(String gender, Map<String, Object> attrs) {
        Map<String, Object> result = new HashMap<>(attrs);
        if (attrs.get(DICTIONARY_NAME_ATTR) instanceof String)
            return result;
        List<String> dictionaryNames = (List<String>) attrs.get(DICTIONARY_NAME_ATTR);
        if (dictionaryNames != null && dictionaryNames.size() == 2) {
            result.replace(DICTIONARY_NAME_ATTR, gender.equals(MAN_GENDER) ? dictionaryNames.get(0) : dictionaryNames.get(1));
        }
        return result;
    }
}
