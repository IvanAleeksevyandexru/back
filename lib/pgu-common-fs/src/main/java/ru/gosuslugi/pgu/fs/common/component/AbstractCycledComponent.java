package ru.gosuslugi.pgu.fs.common.component;

import ru.gosuslugi.pgu.dto.ApplicantAnswer;
import ru.gosuslugi.pgu.components.BasicComponentUtil;
import ru.gosuslugi.pgu.dto.cycled.CycledApplicantAnswerItem;
import ru.gosuslugi.pgu.dto.descriptor.FieldComponent;

import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static org.springframework.util.StringUtils.hasText;
import static org.springframework.util.StringUtils.isEmpty;

public abstract class AbstractCycledComponent<InitialValueModel> extends AbstractComponent<InitialValueModel> {

    @Override
    public boolean isCycled() {
        return true;
    }

    public ComponentResponse<InitialValueModel> getCycledInitialValue(FieldComponent component, Map<String, Object> externalData) {
        return ComponentResponse.empty();
    }

    protected void preProcessCycledComponent(FieldComponent component, Map<String, Object> externalData) {
    }

    public void processCycledComponent(FieldComponent fieldComponent, CycledApplicantAnswerItem answerItem) {
        Map<String, Object> externalData = getExternalDataMap(answerItem);
        preProcessCycledComponent(fieldComponent, externalData);
        ComponentResponse<InitialValueModel> initialValue = getCycledInitialValue(fieldComponent, externalData);
        String initialValueString = jsonProcessingService.componentDtoToString(initialValue);
        if (hasText(initialValueString)) {
            fieldComponent.setValue(initialValueString);
        }
        if (isEmpty(initialValueString)) {
            Set<String> fieldNames = BasicComponentUtil.getPreSetFields(fieldComponent);
            Optional valueBox = fieldNames.stream().filter(externalData::containsKey).map(externalData::get).findAny();
            if(valueBox.isPresent()) {
                fieldComponent.setValue((String) valueBox.get());
            }
        }
    }

    public Map<String, Object> getExternalDataMap(CycledApplicantAnswerItem answerItem) {
        return answerItem.getEsiaData();
    }

    public void addToCycledItemEsiaData(FieldComponent fieldComponent, ApplicantAnswer applicantAnswer, CycledApplicantAnswerItem answerItem) {
    }

    public void removeFromCycledItemEsiaData(FieldComponent fieldComponent, CycledApplicantAnswerItem answerItem) {
    }

}
