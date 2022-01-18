package ru.gosuslugi.pgu.fs.component.payment.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

/** Значение приходящее из компонента типа ChildrenClubs. */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class ChildrenClubsGroup {
	private FinancialSourceBudget financialSourceBudget;
	private FinancialSource financialSource;
	private String name;
	private String groupGUID;
}