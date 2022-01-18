package ru.gosuslugi.pgu.pgu_common.gibdd.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
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
    private String regActionName;
    /**
     * Дата регистрационного действия
     */
    private String regDate;
    /**
     * Признак того что действие осуществлялось доверенным
     */
    private Boolean confidentSign;
    /**
     * Признак лизинга
     */
    private String leasingFlag;
    /**
     * Наименование подразделения
     */
    private String regDepartment;
}
