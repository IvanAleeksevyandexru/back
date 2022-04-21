package ru.gosuslugi.pgu.pgu_common.gibdd.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import io.swagger.v3.oas.annotations.media.Schema;
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
    @Schema(description = "Тип владельца")
    private String ownerType;

    /**
     * Дата начала владения
     */
    @Schema(description = "Дата начала владения")
    private String dateStart;

    /**
     * Дата окончания владения
     */
    @Schema(description = "Дата окончания владения")
    private String dateEnd;

}
