package ru.gosuslugi.pgu.fs.component.medicine;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import ru.gosuslugi.pgu.common.core.json.JsonProcessingUtil;
import ru.gosuslugi.pgu.dto.descriptor.FieldComponent;
import ru.gosuslugi.pgu.dto.descriptor.types.ComponentType;
import ru.gosuslugi.pgu.fs.common.component.AbstractComponent;
import ru.gosuslugi.pgu.fs.common.component.ComponentResponse;

import java.util.Map;

@Component
@RequiredArgsConstructor
public class MedicalReferralsComponent extends AbstractComponent<String> {

    private final static String MEDICAL_INFO_KEY =  "medicalInfo";
    private final static String BUTTON_LABEL =  "buttonLabel";

    public ComponentType getType() {
        return ComponentType.MedicalReferrals;
    }

    @Override
    public ComponentResponse<String> getInitialValue(FieldComponent component) {
        var result = Map.of(
                MEDICAL_INFO_KEY, component.getArgument(MEDICAL_INFO_KEY),
                BUTTON_LABEL, component.getArgument(BUTTON_LABEL)
        );
        return ComponentResponse.of(JsonProcessingUtil.toJson(result));
    }
}