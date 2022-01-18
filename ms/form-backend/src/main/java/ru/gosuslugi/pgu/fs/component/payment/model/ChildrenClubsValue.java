package ru.gosuslugi.pgu.fs.component.payment.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

/** Значение приходящее из компонента типа ChildrenClubs. */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class ChildrenClubsValue {
	private String datasource;
	private ChildrenClubsProgram program;
	private ChildrenClubsGroup group;
}