package ru.gosuslugi.pgu.fs.component.time.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class Timeslot {

    /**
     * ИД брони
     */
    private String slotId;

    /**
     * ID Организации
     */
    private String organizationId;

}
