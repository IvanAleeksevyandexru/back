package ru.gosuslugi.pgu.fs.component.time;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.gosuslugi.pgu.common.esia.search.dto.UserPersonalData;
import ru.gosuslugi.pgu.dto.ScenarioDto;
import ru.gosuslugi.pgu.dto.descriptor.FieldComponent;
import ru.gosuslugi.pgu.dto.descriptor.types.ComponentType;
import ru.gosuslugi.pgu.fs.common.descriptor.MainDescriptorService;
import ru.gosuslugi.pgu.fs.utils.CalculatedAttributesHelper;
import ru.gosuslugi.pgu.fs.utils.ParseAttrValuesHelper;

import java.util.Map;

import static org.springframework.util.StringUtils.hasText;
import static ru.gosuslugi.pgu.components.ComponentAttributes.DEPARTMENT_ATTR;

@Slf4j
@Component
public class TimeSlotWithComputableDepartment extends TimeSlotServiceComponent {

    public static final String CALCULATED_DEPARTMENT_ARG = "calculatedDepartment";

    public TimeSlotWithComputableDepartment(ParseAttrValuesHelper parseAttrValuesHelper,
                                            UserPersonalData personalData,
                                            CalculatedAttributesHelper calculatedAttributesHelper,
                                            MainDescriptorService mainDescriptorService) {
        super(parseAttrValuesHelper, personalData, calculatedAttributesHelper, mainDescriptorService);
    }

    @Override
    public ComponentType getType() {
        return ComponentType.TimeSlotWithComputableDepartment;
    }

    @Override
    protected String getDepartment(FieldComponent component, ScenarioDto scenarioDto, Map<String, Object> presetValues) {
        String calculatedRef = component.getArguments().get(CALCULATED_DEPARTMENT_ARG);
        if (hasText(calculatedRef)) {
            Map<String, String> departmentAttr = (Map<String, String>) component.getAttrs().get(DEPARTMENT_ATTR);
            String prevValue = departmentAttr.get("value");
            if (!calculatedRef.equals(prevValue)) {
                departmentAttr.put("value", calculatedRef);
                log.debug("Смена значения параметра department {} -> {}", prevValue, calculatedRef);
            }
        }

        return (String) presetValues.getOrDefault(DEPARTMENT_ATTR, resolveAttribute(DEPARTMENT_ATTR, component, scenarioDto));
    }
}
