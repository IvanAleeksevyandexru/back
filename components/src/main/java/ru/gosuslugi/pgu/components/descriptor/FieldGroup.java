package ru.gosuslugi.pgu.components.descriptor;

import lombok.Data;
import ru.gosuslugi.pgu.dto.descriptor.CycledAttrs;

import java.util.List;

/**
 * Группа полей
 */
@Data
public class FieldGroup {
    /** Заголовок группы полей */
    private String groupName;

    /** Список полей */
    private List<ComponentField> fields;

    /** Атрибуты для цикличной обработки группы полей */
    private CycledAttrs attrs;

    /** Признак отделения группы горизонтальным разделителем */
    private Boolean needDivider;
}
