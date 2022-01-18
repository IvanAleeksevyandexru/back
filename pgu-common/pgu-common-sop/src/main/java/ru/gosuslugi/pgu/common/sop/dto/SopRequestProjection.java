package ru.gosuslugi.pgu.common.sop.dto;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Builder;
import lombok.Data;

/**
 * Список идентификаторов полей справочника СОП и режим их возврата.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@Data
@Builder(builderMethodName = "with")
public class SopRequestProjection {

	/** Режим возврата полей справочника. */
	private Mode mode;

	/** Список идентификаторов полей справочника, которые необходимо вернуть или исключить из выдачи согласно значению {@link #mode}. */
	private List<String> columnUids;
}