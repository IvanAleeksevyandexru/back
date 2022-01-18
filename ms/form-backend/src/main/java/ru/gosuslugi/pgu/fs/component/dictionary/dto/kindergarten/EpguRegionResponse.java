package ru.gosuslugi.pgu.fs.component.dictionary.dto.kindergarten;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/** Ответ на рест метод /api/nsi/v1/epgu/region */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class EpguRegionResponse {

    private String infSysCode;

    private Boolean formPrefilling;

    private String senderCode;

    private String routingCode;
}