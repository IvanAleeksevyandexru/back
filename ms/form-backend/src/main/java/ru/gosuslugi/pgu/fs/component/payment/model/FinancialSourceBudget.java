package ru.gosuslugi.pgu.fs.component.payment.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.math.BigDecimal;

/** Размер оплаты для типов оплат. */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class FinancialSourceBudget{
	@JsonProperty("pfdod_certificate")
	private BigDecimal pfdodCertificate;
	@JsonProperty("private")
	private BigDecimal privateSource;
	private BigDecimal paid;
}