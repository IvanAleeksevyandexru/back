package ru.gosuslugi.pgu.fs.pgu.dto;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;

import java.time.LocalDate;
import java.util.Objects;

/**
 * DTO для получения сертификата о браке/разводе из ЛК
 */
@Data
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class MaritalResponseItem {
    private String issuedBy;
    private String type;
    private String actDate;
    private String actNo;
    private String series;
    private String number;
    private String issueDate;

    public MaritalResponseItem(String actNo, String actDate, String issuedBy, String series, String number, String issueDate) {
        this.actNo = actNo;
        this.actDate = actDate;
        this.issuedBy = issuedBy;
        this.series = series;
        this.number = number;
        this.issueDate = issueDate;
    }

    public MaritalResponseItem(String actNo, LocalDate parseDate, String issuedBy,String series, String number, String issueDate) {
        this.actNo = actNo;
        if(Objects.nonNull(parseDate)) {
            this.actDate =parseDate.toString();
        }
        this.issuedBy = issuedBy;
        this.series = series;
        this.number = number;
        this.issueDate = issueDate;
    }
}