package ru.gosuslugi.pgu.fs.component.dictionary.dto.kindergarten;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/** Входные параметры для поиска информации по дет.садам. */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class KinderGartenInputParams {
    private int timeout;
    private String requestType;
    private String smevVersion;
    private String passCode;
    private long orderId;
}
