package ru.gosuslugi.pgu.fs.service;

import ru.gosuslugi.pgu.dto.ApplicantAnswer;
import ru.gosuslugi.pgu.dto.ScenarioDto;
import ru.gosuslugi.pgu.dto.descriptor.FieldComponent;
import ru.gosuslugi.pgu.dto.descriptor.ServiceDescriptor;
import ru.gosuslugi.pgu.terrabyte.client.model.FileInfo;

import java.util.List;
import java.util.Map;

public interface TerrabyteService {

    void checkTerrabyteForSpecificFile(Map<String, String> incorrectAnswerMap, Map.Entry<String, ApplicantAnswer> entry,
                                       FieldComponent fieldComponent, String valuePattern);

    FileInfo getFileInfoFromUserAnswer(Map.Entry<String, ApplicantAnswer> userAnswerEntry, FieldComponent fieldComponent, String valuePattern);

    FileInfo getFileInfoFromUserAnswer(Map<String, ApplicantAnswer> userAnswer, FieldComponent fieldComponent, String valuePattern);

    List<FileInfo> getAllFilesInfoForOrderId(ScenarioDto scenarioDto);

    void deleteRedundantFiles(ScenarioDto scenarioDto, ServiceDescriptor serviceDescriptor);

    boolean checkOrderHasFileComponents(ScenarioDto scenarioDto, ServiceDescriptor serviceDescriptor);
}
