package ru.gosuslugi.pgu.fs.component.dictionary.dto.kindergarten;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.gosuslugi.pgu.terrabyte.client.model.FileInfo;

import java.util.Map;

/** Объект для преобразования в json для передачи в конвертер. */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class KinderGartenConverterDto {
    /** Данные из сервиса "Терабайт" по orderId. */
    private Map<String, FileInfo> terrabyteFiles;

    /** Данные из ответа nsi-сервиса. */
    private KinderGartenNsiResponse nsiResponse;

    /** Данные из ответа nsi-сервиса по региону. */
    private EpguRegionResponse nsiRegion;

}
