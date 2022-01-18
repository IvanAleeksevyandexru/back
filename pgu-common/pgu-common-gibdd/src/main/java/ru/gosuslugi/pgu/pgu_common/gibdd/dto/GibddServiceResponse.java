package ru.gosuslugi.pgu.pgu_common.gibdd.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class GibddServiceResponse<T> {
    private T data;
    private ExternalServiceCallResult externalServiceCallResult = ExternalServiceCallResult.SUCCESS;
    private String errorMessage;
}
