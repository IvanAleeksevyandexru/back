package ru.gosuslugi.pgu.fs.component.payment.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class BillInfoComponentDto {

    String billId;
    String billNumber;
    String billName;
    String billDate;
    String amount;
    String originalAmount;

}
