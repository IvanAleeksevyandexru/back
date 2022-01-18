package ru.gosuslugi.pgu.common.sop.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Builder;
import lombok.Data;

import java.util.List;

/**
 * Блок отвечающий за фильтрацию. Фильтрация задаёт атрибуты и их допустимые значения.
 * При этом поиск выполняется на равенство указанных значений.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@Data
@Builder(builderMethodName = "with")
public class SopRequestFilter {

	/**
	 * Блок отвечающий за фильтрацию – идентификатор столбцов и принимаемые ими значения.
	 * Для фильтрации по пустым значения передается “”. Допускается указание нескольких фильтров.
	 */
	private List<SopFiltersItem> filters;

	/** Запрос для поиска по части значения. */
	private SopRequestQuery query;
}