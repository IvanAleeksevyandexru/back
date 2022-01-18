package ru.gosuslugi.pgu.fs.component.personinfo.model;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * Представляет собой возраст лица.
 * Содержит в себе полные года и месяцы.
 */
@Data
@AllArgsConstructor
public class PersonAge {
    private long years;
    private long months;

    public AgeType getAgeType() {
        return years >= 18 ? AgeType.MATURE : AgeType.YOUNG;
    }

    /**
     * Возвращает строковое представление возраста, например: 5 лет, почти год, 10 месяцев
     * @return возраст
     */
    public String getAgeAsText() {
        return years > 0 ? getYearsText() : getMonthsText();
    }

    private String getYearsText() {
        String result = " лет";
        long count = years % 100;
        if (count >= 5 && count <= 20) {
            result = " лет";
        } else {
            count = count % 10;
            if (count == 1) {
                result = " год";
            } else if (count >= 2 && count <= 4) {
                result = " года";
            }
        }
        return years + result;
    }

    private String getMonthsText() {
        if(months == 11) {
            return "почти год";
        } else if(months == 1){
            return "1 месяц";
        } else if(months > 1 && months < 5) {
            return months + " месяца";
        } else {
            return months + " месяцев";
        }
    }
}
