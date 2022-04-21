package ru.gosuslugi.pgu.common.sop.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Builder;
import lombok.Data;

import java.util.List;

/**
 * Полнотекстовый поиск. Если отключить {@link SopDictionaryRequest#getLevenshtein()}, то это поиск без опечаток. Т.е. он ищет по LIKE значе%
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@Data
@Builder(builderMethodName = "with")
public class SopRequestQuery {

	/**
	 * Признак поиска всех слов из {@link #query}.
	 * <ul>
	 *     <li>{@code false} – если нужно искать по всем словам из {@link #query}</li>
	 *     <li>{@code true} – если хотя бы одно слово из {@link #query} попадается, то оно будет включено в результат</li>
	 * </ul>
	 **/
	private boolean searchAnyWord;

	/** Текст поискового запроса – слово или фраза. */
	private String query;

	/** Список полей, в которых необходимо выполнять поиск слов, заданных в {@link #query}. */
	private List<String> columnUids;
}