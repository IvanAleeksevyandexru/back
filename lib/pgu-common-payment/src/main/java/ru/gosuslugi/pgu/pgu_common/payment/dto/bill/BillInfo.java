package ru.gosuslugi.pgu.pgu_common.payment.dto.bill;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class BillInfo {
    String billId;
    String billNumber;
    String billName;
    String billDate;
    Boolean isPaid;
    String amount;
    List<BillInfoAttr> addAttrs;
}
