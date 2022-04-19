package ru.gosuslugi.pgu.pgu_common.payment.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Детали платежа
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class PaymentDetailsDto {
  private String uin;
  private String amount;
  private String amountWithoutDiscount;
  private String paymentPurpose;
  private String receiver;
  private Long billId;
}
