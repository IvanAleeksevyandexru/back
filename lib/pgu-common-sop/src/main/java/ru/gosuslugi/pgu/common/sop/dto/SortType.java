package ru.gosuslugi.pgu.common.sop.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonFormat(shape = JsonFormat.Shape.STRING)
@JsonIgnoreProperties(ignoreUnknown = true)
/**
 * Тип сортировки – DESC или ASC.
 */
public enum SortType {
    ASC,
    DESC
}
