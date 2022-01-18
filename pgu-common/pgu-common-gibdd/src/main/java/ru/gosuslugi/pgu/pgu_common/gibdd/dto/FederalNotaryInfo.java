package ru.gosuslugi.pgu.pgu_common.gibdd.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Данные о ТС из Федеральной нотариальной палаты
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class FederalNotaryInfo {
    /** Признак нахождения в залоге */
    private Boolean isPledged;
}
