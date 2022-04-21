package ru.gosuslugi.pgu.pgu_common.payment.dto.bill;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class ImportBillNewResponse {
    Integer errorCode;
    String requestId;
    String errorMessage;
}
