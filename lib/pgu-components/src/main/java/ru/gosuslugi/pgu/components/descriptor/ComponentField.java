package ru.gosuslugi.pgu.components.descriptor;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * Поле в компоненте
 */
@Data
@AllArgsConstructor
public class ComponentField {
    /** лейбл поля */
    private String label;

    /** значение поля */
    private String value;

    /** код поля (как хранить значение) */
    private String fieldName;

    /** Нужно ли форматировать числовое поле. Например значие '1000' преобразуется в '1 000' */
    private Boolean rank;
}
