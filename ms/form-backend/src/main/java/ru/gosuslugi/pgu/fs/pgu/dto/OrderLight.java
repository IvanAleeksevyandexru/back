package ru.gosuslugi.pgu.fs.pgu.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

import java.util.Date;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class OrderLight {

    private Long orderId;

    private Long statusId;

    private String userSelectedRegion;

    private Date orderDate;
}
