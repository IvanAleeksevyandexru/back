package ru.gosuslugi.pgu.fs.component.dictionary.dto.kindergarten;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import ru.gosuslugi.pgu.dto.kindergarten.KinderGartenStatusMessage;

/** Выходной результат генерации xml для СМЭВ по дет.садам. */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class KinderGartenOutputParams {
    private String xmlRequest;
    private RequestType requestType;
    private String userSelectedRegion;
    private EpguRegionResponse regionResponse;
    private KinderGartenStatusMessage message;
}
