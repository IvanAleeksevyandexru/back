package ru.gosuslugi.pgu.components.descriptor.types;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

/**
 * Период активности гражданина
 * используется в компоненте EmployeeHistoryList
 */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class EmployeeHistory {
    private String type;
    private String label;
    private EpguLibDate from;
    private EpguLibDate to;
    private String position;
    private String place;
    private String address;
    private Boolean checkboxToDate;

    @Data
    public static class EpguLibDate {
        private Integer year;
        private Integer month;
        private String monthCode;
    }
}
