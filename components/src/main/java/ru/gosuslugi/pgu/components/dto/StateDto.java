package ru.gosuslugi.pgu.components.dto;

import lombok.*;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class StateDto {
    /**
     * Наименование группы полей
     */
    private String groupName;

    @Singular
    private List<FieldDto> fields;

    /** Признак отделения группы горизонтальным разделителем */
    private Boolean needDivider;
}
