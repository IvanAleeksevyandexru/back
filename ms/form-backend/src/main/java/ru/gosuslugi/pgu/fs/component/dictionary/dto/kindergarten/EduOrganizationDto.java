package ru.gosuslugi.pgu.fs.component.dictionary.dto.kindergarten;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/** Ответ, возвращаемый на UI с элементами из nsi-справочника и дополнительными значениями атрибутов. */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class EduOrganizationDto {
    private String code;
    private String priorityNumber;
    private String name;
}
