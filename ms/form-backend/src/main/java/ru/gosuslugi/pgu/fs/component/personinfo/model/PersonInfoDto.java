package ru.gosuslugi.pgu.fs.component.personinfo.model;

import lombok.Data;

@Data
public class PersonInfoDto {
    /** Имя ребенка */
    private String name;

    /** Текстовое представление возраста, например, 6 лет */
    private String ageText;

    /** Тип возраста */
    private AgeType ageType;

    /** пол */
    private String gender;
}
