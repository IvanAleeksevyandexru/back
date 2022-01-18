package ru.gosuslugi.pgu.fs.component.payment.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

/** Типы оплаты из компонента ChildrenClubs. */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class FinancialSource {
	@JsonProperty("pfdod_certificate")
	private Boolean pfdodCertificate;
	@JsonProperty("private")
	private Boolean privateSource;
	private Boolean paid;
	private Boolean none;
	private Boolean budget;
}