package ru.gosuslugi.pgu.fs.common.utils;

import ru.gosuslugi.pgu.dto.ApplicantAnswer;
import ru.gosuslugi.pgu.dto.ScenarioDto;
import ru.gosuslugi.pgu.dto.descriptor.FieldComponent;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public class ConditionalDisabledComponentUtil {

    private static final String REF_ATTRIBUTE_KEY = "ref";

    /**
     * Данное поле содержит ссылку на checkbox управляющий включением.отключением валидации.
     * Если компонент checkbox возвращает значение true то StringInput считается помеченным
     * как disabled и валидироаться не должен.
     */
    private static final String CHECKBOX_KEY = "relatedRel";

    /**
     * @param scenarioDto    - необходимо чтобы подтянуть значение установенное в checkbox.
     * @param fieldComponent - используется для определения checkboxId.
     * @return true если в соответствующем checkbox установлена галочка  запрещающая валидацию.
     */
    public static boolean isCheckBoxValidationDisabled(ScenarioDto scenarioDto, FieldComponent fieldComponent) {
        String checkboxId = getCheckboxId(fieldComponent);
        ApplicantAnswer checkboxValue = scenarioDto.getCurrentValue().get(checkboxId);
        if (checkboxValue == null) {
            boolean findRefInAnswers = (boolean) fieldComponent.getAttrs().getOrDefault("findRefInAnswers", false);
            if (findRefInAnswers) {
                checkboxValue = scenarioDto.getApplicantAnswerByFieldId(checkboxId);
            }
        }
        if (checkboxValue != null && checkboxValue.getVisited()) {
            boolean revertCheckbox = (boolean) fieldComponent.getAttrs().getOrDefault("revertCheckbox", false);
            return revertCheckbox != Boolean.parseBoolean(checkboxValue.getValue());
        }
        return false;
    }

    /**
     * Определяет checkboxId по значению указанному в поле {@value #CHECKBOX_KEY}.
     */
    private static String getCheckboxId(FieldComponent component) {
        Map<String, ?> attributes = component.getAttrs();
        Object o = attributes.get(REF_ATTRIBUTE_KEY);
        if (o instanceof List) {
            List<Map<String, ?>> references = (List<Map<String, ?>>) o;
            Optional<String> value = references.stream().map(map -> map.get(CHECKBOX_KEY))
                    .filter(v -> v != null).map(v -> (String) v).findFirst();
            return value.orElse("");
        }
        return "";
    }
}
