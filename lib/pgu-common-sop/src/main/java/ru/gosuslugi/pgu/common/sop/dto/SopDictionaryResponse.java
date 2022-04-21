package ru.gosuslugi.pgu.common.sop.dto;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

/**
 * Ответ от внешнего справочника СОП.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@Data
public class SopDictionaryResponse {

	/** Идентификатор справочника в СОП. */
	private String sourceUid;

	private boolean lastPage;

	/** Список элементов, отобранных в ЕСНСИ (система, в которой хранятся справочники). */
	private List<SopResponseDataItem> data;

	/** Список ошибок с кодами и текстом ошибки из ЕСНСИ – системы. */
	private List<SopDictionaryErrorInfo> errors;
}