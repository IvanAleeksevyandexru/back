package ru.gosuslugi.pgu.pgu_common.gibdd.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * Регистрационное действие
 */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class RegAction {

    /**
     * Наименование регистрационного действия
     */
    @Schema(description = "Наименование регистрационного действия")
    private String regActionName;

    /**
     * Дата регистрационного действия
     */
    @Schema(description = "Дата регистрационного действия")
    private String regDate;

    /**
     * Признак того что действие осуществлялось доверенным
     */
    @Schema(description = "Признак того что действие осуществлялось доверенным")
    private Boolean confidentSign;

    /**
     * Признак лизинга
     */
    @Schema(description = "Признак лизинга")
    private String leasingFlag;

    /**
     * Наименование подразделения
     */
    @Schema(description = "Наименование подразделения")
    private String regDepartment;

}
