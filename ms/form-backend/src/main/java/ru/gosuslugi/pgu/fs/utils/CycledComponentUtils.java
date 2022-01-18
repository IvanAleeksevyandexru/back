package ru.gosuslugi.pgu.fs.utils;

import lombok.experimental.UtilityClass;
import org.springframework.util.StringUtils;
import ru.gosuslugi.pgu.dto.ScenarioDto;
import ru.gosuslugi.pgu.dto.cycled.CycledApplicantAnswer;
import ru.gosuslugi.pgu.dto.descriptor.FieldComponent;

/** Вспомогательный класс для методов в компонентах, связанных с циклами. */
@UtilityClass
public class CycledComponentUtils {

    /**
     * Определение индекса для мнемоники файла
     */
    public String getCurrentIndexForComponentId(FieldComponent field, ScenarioDto scenarioDto){
        if (field.isComponentInCycle()) {
            CycledApplicantAnswer currentAnswer = scenarioDto.getCycledApplicantAnswers().getCurrentAnswer();
            String currentIndex = currentAnswer.getCurrentItemId();
            if (StringUtils.hasText(currentIndex)) {
                return currentIndex;
            }
        }
        return "0";
    }
}
