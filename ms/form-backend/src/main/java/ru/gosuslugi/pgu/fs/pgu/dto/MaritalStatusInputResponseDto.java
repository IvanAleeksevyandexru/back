package ru.gosuslugi.pgu.fs.pgu.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.gosuslugi.pgu.pgu_common.nsi.dto.NsiSuggestDictionaryItem;
/**
 * DTO для возврата сертификата о браке/разводе на фронт
 */
@Data
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class MaritalStatusInputResponseDto {

    // todo: поименование филдов конечно.. но завязка на фронт
    private String act_rec_date;
    private String act_rec_number;
    private NsiSuggestDictionaryItem act_rec_registrator;
    private String series;
    private String number;
    private String issueDate;
}
