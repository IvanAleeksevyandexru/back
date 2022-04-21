package ru.gosuslugi.pgu.fs.common.component.input;

import org.springframework.util.StringUtils;
import ru.gosuslugi.pgu.components.FieldComponentUtil;
import ru.gosuslugi.pgu.dto.ApplicantAnswer;
import ru.gosuslugi.pgu.dto.cycled.CycledApplicantAnswerItem;
import ru.gosuslugi.pgu.dto.descriptor.FieldComponent;
import ru.gosuslugi.pgu.fs.common.component.AbstractCycledComponent;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Реализует добавление данных в блок {@code esiaData} в цикличных компонентах
 * @param <InitialValueModel>
 */
public abstract class EsiaDataCycledComponent<InitialValueModel> extends AbstractCycledComponent<InitialValueModel> {

    @Override
    public void addToCycledItemEsiaData(FieldComponent fieldComponent, ApplicantAnswer applicantAnswer, CycledApplicantAnswerItem answerItem) {
        List<String> fieldNames = FieldComponentUtil.getFieldNames(fieldComponent);
        if (fieldNames.size() > 0 && applicantAnswer != null && !StringUtils.isEmpty(applicantAnswer.getValue())) {
            answerItem.getEsiaData().put(fieldNames.get(0), applicantAnswer.getValue());
            answerItem.getFieldToId().put(fieldNames.get(0), fieldComponent.getId());
        }
    }

    @Override
    public void removeFromCycledItemEsiaData(FieldComponent fieldComponent, CycledApplicantAnswerItem answerItem) {
        Map<String, String> currentFieldToIdMap = new HashMap<>(answerItem.getFieldToId());
        for (Map.Entry<String, String> entry : currentFieldToIdMap.entrySet()) {
            if (Objects.equals(entry.getValue(), fieldComponent.getId())) {
                answerItem.getEsiaData().remove(entry.getKey());
                answerItem.getFieldToId().remove(entry.getKey());
            }
        }
    }
}
