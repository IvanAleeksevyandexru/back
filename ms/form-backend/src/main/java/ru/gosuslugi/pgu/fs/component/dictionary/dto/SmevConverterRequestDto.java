package ru.gosuslugi.pgu.fs.component.dictionary.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotNull;

@Data
@Schema(description = "Запрос в СМЭВ для получения nsi-справочника")
public class SmevConverterRequestDto {

    @JsonCreator
    public SmevConverterRequestDto(@JsonProperty("data") String data,
                                   @JsonProperty("serviceId") String serviceId,
                                   @JsonProperty("templateName") String templateName) {
        this.data = data;
        this.serviceId = serviceId;
        this.templateName = templateName;
    }

    @Schema(description = "Запрос в СМЭВ в формате XML", required = true)
    @NotNull
    private String data;

    @Schema(description = "Код услуги", required = true)
    @NotNull
    private String serviceId;

    @Schema(description = "Наименование json-шаблона", required = true)
    @NotNull
    private final String templateName;

    @Schema(description = "Дополнительные данные в формате json")
    private String extData;

}

