package ru.gosuslugi.pgu.fs.pgu.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

import java.util.List;

/**
 * DTO для получения привелегий для юр.лица через сервисы ЕСИА
 */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class EmpowermentListDto {
    List<EmpowermentDto> elements;
}
