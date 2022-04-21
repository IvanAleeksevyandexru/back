package ru.gosuslugi.pgu.pgu_common.payment.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.util.Date;

/**
 * Информация о платеже
 */
@Data
@EqualsAndHashCode
@JsonIgnoreProperties(ignoreUnknown = true)
public class PaymentInfo {
    String uin;
    String payDate;
    String title;
    BigDecimal amount;
    String link;
    @JsonProperty("invitation_address")
    String invitationAddress;
}
