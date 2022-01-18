package ru.gosuslugi.pgu.fs.component.time;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.gosuslugi.pgu.common.core.exception.JsonParsingException;
import ru.gosuslugi.pgu.dto.descriptor.types.ComponentType;
import ru.gosuslugi.pgu.fs.common.component.AbstractComponent;
import ru.gosuslugi.pgu.fs.component.time.dto.AnswerDto;

import java.util.Map;

import static java.util.Objects.isNull;

/**
 * Компонент выбора промежутка дат со временем
 * Дата и время указывается в локальном представлении
 */
@Slf4j
@Component
public class DatePeriod extends AbstractComponent<String> {

    @Override
    public ComponentType getType() {
        return ComponentType.DatePeriod;
    }

    @Override
    protected void validateAfterSubmit(Map<String, String> incorrectAnswers, String key, String value) {
        if (value == null || value.equals("")) {
            incorrectAnswers.put(key, "Даты начала и окончания должны быть заполнены");
            return;
        }
        try {
            AnswerDto answerDto = jsonProcessingService.fromJson(value, AnswerDto.class);
            if (isNull(answerDto.getStartDate()) || isNull(answerDto.getEndDate())) {
                incorrectAnswers.put(key, "Даты начала и окончания должны быть заполнены");
                return;
            }

            if (answerDto.getStartDate().isAfter(answerDto.getEndDate())) {
                incorrectAnswers.put(key, String.format("Дата начала должна быть раньше даты окончания (%s)", value));
            }
        } catch (JsonParsingException e) {
            incorrectAnswers.put(key, String.format("Даты начала и окончания должны иметь правильный формат даты (%s)", value));
        }
    }
}
