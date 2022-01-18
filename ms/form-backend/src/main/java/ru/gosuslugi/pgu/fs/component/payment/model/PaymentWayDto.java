package ru.gosuslugi.pgu.fs.component.payment.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.gosuslugi.pgu.fs.component.payment.PaymentWayComponent;

import java.math.BigDecimal;

/** Способ оплаты для {@link PaymentWayComponent}. */
@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class PaymentWayDto {
    private String paymentType;
    private BigDecimal amount;
    private String programType;
}
