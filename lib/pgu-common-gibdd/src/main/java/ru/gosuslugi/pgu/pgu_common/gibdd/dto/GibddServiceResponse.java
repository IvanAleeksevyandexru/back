package ru.gosuslugi.pgu.pgu_common.gibdd.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class GibddServiceResponse<T> {

    @Schema(description = "Полученные данные от сервиса")
    private T data;

    @Schema(description = "Результат выполнения запроса")
    private ExternalServiceCallResult externalServiceCallResult = ExternalServiceCallResult.SUCCESS;

    @Schema(description = "Текст ошибки")
    private String errorMessage;
}
