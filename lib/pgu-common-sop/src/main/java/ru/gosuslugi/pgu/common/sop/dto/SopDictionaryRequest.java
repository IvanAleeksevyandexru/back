package ru.gosuslugi.pgu.common.sop.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Builder;
import lombok.Data;

/**
 * Формирует тело запроса к сервису облачных подсказок (СОП).
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@Data
@Builder
public class SopDictionaryRequest {

	/**
	 * Параметр расстояния Левенштейна, обозначает число допустимых ошибок(опечаток) в каждом слове.
	 *
	 * <ul>
	 *     <li>0 – нечеткий поиск отключен, используется {@link SopRequestQuery#getQuery()}</li>
	 *     <li>1-3 - нечёткий поиск от 1 до 3 исправлений в слове, чтобы считать его похожим на искомое из {@link SopRequestQuery#getQuery()}</li>
	 * </ul>
	 */
	private int levenshtein;

	/** Использование инверсии (ошибочной раскладки). */
	private boolean inversion;

	/** Фильтр для отбора значений по идентификаторам полей и их значениям. */
	private SopRequestFilter filter;

	/** Необязательный блок, задающий порядок сортивки результатов поиска. */
	private SopRequestSorter sort;

	/** Идентификатор справочника в ТСОП. */
	private String sourceUid;

	/** Элемент отвечающий за лимитирование выбранных элементов и пэйджинацию. */
	private SopPageLimit limit;

	/** Список идентификаторов полей справочника СОП и режим их возврата. */
	private SopRequestProjection projection;
}