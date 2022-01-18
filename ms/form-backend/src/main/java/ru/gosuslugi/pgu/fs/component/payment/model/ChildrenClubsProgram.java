package ru.gosuslugi.pgu.fs.component.payment.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

/** Значение приходящее из компонента типа ChildrenClubs. */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class ChildrenClubsProgram {
	private String typeOfBudget;
	private String regionName;
	private String name;
	private String fiasMunicipal;
	private String fiasRegion;
	private String municipalityName;
}