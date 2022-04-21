package ru.gosuslugi.pgu.common.sop.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Builder;
import lombok.Data;

/**
 * Необязательный блок, отвечающий за сортировку результатов поиска.
 * Без этого блока результаты сортируются по умолчанию по релевантности – строки, максимально похожие на искомую фразу будут выше.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@Data
@Builder(builderMethodName = "with")
public class SopRequestSorter {

	/** Тип сортировки по убыванию или возрастанию. */
	private SortType sortType;

	/** Идентификатор столбца для сортировки результатов. */
	private String columnUid;
}