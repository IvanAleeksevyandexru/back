package ru.gosuslugi.pgu.common.sop.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Builder;
import lombok.Data;

/**
 * Элемент отвечающий за лимитирование и пэйджинацию.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@Data
@Builder(builderMethodName = "with")
public class SopPageLimit {

	/** Номер возвращаемой страницы. */
	private int pageNumber;

	/**
	 * Число возвращаемых строк.
	 * Не может превышать лимит заданный для справочника, а лимит справочника не может превышать ограничение системы
	 * (на конец 2020 года - 150, будет расширен до 300).
	 **/
	private int pageSize;
}