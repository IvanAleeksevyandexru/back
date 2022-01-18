package ru.gosuslugi.pgu.pgu_common.gibdd.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Периоды владения
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class OwnerPeriod {
    /**
     * Тип владельца
     */
    private String ownerType;
    /**
     * Дата начала владения
     */
    private String dateStart;
    /**
     * Дата окончания владения
     */
    private String dateEnd;
}
