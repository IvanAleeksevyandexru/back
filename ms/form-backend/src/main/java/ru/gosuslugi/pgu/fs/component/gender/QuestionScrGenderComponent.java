package ru.gosuslugi.pgu.fs.component.gender;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.gosuslugi.pgu.components.FieldComponentUtil;
import ru.gosuslugi.pgu.dto.descriptor.types.ComponentType;
import ru.gosuslugi.pgu.fs.component.AbstractGenderComponent;

import java.util.*;

import static org.apache.http.util.TextUtils.isBlank;
import static ru.gosuslugi.pgu.dto.descriptor.types.ComponentType.GQuestionScr;
import static ru.gosuslugi.pgu.dto.descriptor.types.ComponentType.QuestionScr;

@Slf4j
@Component
@RequiredArgsConstructor
public class QuestionScrGenderComponent extends AbstractGenderComponent<String> {

    @Override
    public ComponentType getType() {
        return GQuestionScr;
    }

    @Override
    protected ComponentType getTargetComponentType() {
        return QuestionScr;
    }

    @Override
    protected Map<String, Object> processAttrs(String gender, Map<String, Object> attrs) {
        if (isBlank(gender)) {
            return attrs;
        }
        var notNullableAttrs = Optional.ofNullable(attrs).orElse(new HashMap<>());
        Map<String, Object> result = new HashMap<>(notNullableAttrs);
        result.putAll(convertToGenderValue(FieldComponentUtil.ACTIONS_ATTR_KEY, notNullableAttrs, gender));
        result.putAll(convertToGenderValue(FieldComponentUtil.ANSWERS_ATTR_KEY, notNullableAttrs, gender));

        return result;
    }

    private Map<String, Object> convertToGenderValue(String attrKey, Map<String, Object> notNullableAttrs, String gender) {
        Map<String, Object> result = new HashMap<>(notNullableAttrs);
        var list = (ArrayList<LinkedHashMap<String, Object>>) notNullableAttrs.getOrDefault(
                attrKey, new ArrayList<>());
        for (LinkedHashMap<String, Object> action : list) {
            var value = action.get(FieldComponentUtil.LABEL_ATTR);
            if (action.get(FieldComponentUtil.LABEL_ATTR) instanceof List) {
                var actionList = (List) action.get(FieldComponentUtil.LABEL_ATTR);
                value = String.valueOf(actionList.get(0));
                if (!gender.equals("M")) {
                    value = String.valueOf(actionList.get(1));
                }
            }
            action.put(FieldComponentUtil.LABEL_ATTR, value);
        }
        result.put(attrKey, list);
        return result;
    }
}
