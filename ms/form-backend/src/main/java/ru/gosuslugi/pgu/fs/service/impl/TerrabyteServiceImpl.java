package ru.gosuslugi.pgu.fs.service.impl;

import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.JsonPath;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import ru.gosuslugi.pgu.common.esia.search.dto.UserPersonalData;
import ru.gosuslugi.pgu.dto.ApplicantAnswer;
import ru.gosuslugi.pgu.dto.ScenarioDto;
import ru.gosuslugi.pgu.dto.SignInfo;
import ru.gosuslugi.pgu.dto.descriptor.FieldComponent;
import ru.gosuslugi.pgu.dto.descriptor.ServiceDescriptor;
import ru.gosuslugi.pgu.dto.esep.SignedFileInfo;
import ru.gosuslugi.pgu.fs.common.service.ComponentService;
import ru.gosuslugi.pgu.fs.common.service.JsonProcessingService;
import ru.gosuslugi.pgu.fs.common.utils.AnswerUtil;
import ru.gosuslugi.pgu.fs.component.file.PhotoUploadComponent;
import ru.gosuslugi.pgu.fs.component.file.UploadFileComponent;
import ru.gosuslugi.pgu.fs.component.logic.AttachmentContentComponent;
import ru.gosuslugi.pgu.fs.service.TerrabyteService;
import ru.gosuslugi.pgu.terrabyte.client.TerrabyteClient;
import ru.gosuslugi.pgu.terrabyte.client.model.FileInfo;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class TerrabyteServiceImpl implements TerrabyteService {

    private final TerrabyteClient terrabyteClient;
    private final JsonProcessingService jsonProcessingService;
    private final ComponentService componentService;
    private final UploadFileComponent uploadFileComponent;
    private final UserPersonalData userPersonalData;

    public static final String EMPTY_FILES_NOT_ALLOWED = "Запрещена загрузка пустых файлов! Проверьте загружаемый файл";

    @Override
    public void checkTerrabyteForSpecificFile(Map<String, String> incorrectAnswerMap, Map.Entry<String, ApplicantAnswer> entry,
                                              FieldComponent fieldComponent, String valuePattern) {
        FileInfo userFile = getFileInfoFromUserAnswer(entry, fieldComponent, valuePattern);
        if (userFile == null) {
            incorrectAnswerMap.put(fieldComponent.getId(), "User answer does not have file");
            return;
        }

        FileInfo terrabyteImageInfo = terrabyteClient.getFileInfo(userFile, userPersonalData.getUserId(), userPersonalData.getToken());
        if (terrabyteImageInfo == null || !StringUtils.hasText(terrabyteImageInfo.getFileName()) || !StringUtils.hasText(terrabyteImageInfo.getMnemonic())) {
            incorrectAnswerMap.put(fieldComponent.getId(), "Order does not have file");
            return;
        }
        if (Objects.nonNull(terrabyteImageInfo.getFileSize()) && terrabyteImageInfo.getFileSize() <= 0) {
            incorrectAnswerMap.put(fieldComponent.getId(), EMPTY_FILES_NOT_ALLOWED);
        }
        if (!(terrabyteImageInfo.getFileName().equalsIgnoreCase(userFile.getFileName())) &&
                terrabyteImageInfo.getMnemonic().equalsIgnoreCase(userFile.getMnemonic())) {
            incorrectAnswerMap.put(fieldComponent.getId(), "User file is not uploaded");
        }
    }

    @Override
    public FileInfo getFileInfoFromUserAnswer(Map.Entry<String, ApplicantAnswer> userAnswerEntry, FieldComponent fieldComponent, String valuePattern) {
        return getFileInfoFromUserAnswer(Map.ofEntries(userAnswerEntry), fieldComponent, valuePattern);
    }

    @Override
    public FileInfo getFileInfoFromUserAnswer(Map<String, ApplicantAnswer> userAnswer, FieldComponent fieldComponent, String valuePattern) {
        DocumentContext dc = JsonPath.parse(jsonProcessingService.convertAnswersToJsonString(userAnswer));
        Map<String, Object> answer = jsonProcessingService.getFieldFromContext(fieldComponent.getId() + valuePattern, dc, Map.class);
        if (CollectionUtils.isEmpty(answer)) {
            return null;
        }
        if (!StringUtils.hasText((String) answer.get("mnemonic"))) {
            return null;
        }
        FileInfo fileInfo = new FileInfo();
        fileInfo.setMnemonic((String) answer.get("mnemonic"));
        fileInfo.setObjectId(Integer.parseInt(Objects.toString(answer.get("objectId"))));
        fileInfo.setObjectTypeId(Integer.parseInt(Objects.toString(answer.get("objectType"))));
        fileInfo.setFileName((String) answer.get("name"));
        return fileInfo;
    }

    @Override
    public List<FileInfo> getAllFilesInfoForOrderId(ScenarioDto scenarioDto) {
        return terrabyteClient.getAllFilesByOrderId(scenarioDto.getOrderId(), userPersonalData.getUserId(), userPersonalData.getToken());
    }

    @Override
    public void deleteRedundantFiles(ScenarioDto scenarioDto, ServiceDescriptor serviceDescriptor) {
        List<FileInfo> allFilesInfoForOrderId = getAllFilesInfoForOrderId(scenarioDto);
        List<String> fileMnemonics = (scenarioDto.getSignInfoMap().size() > 0) ?
                getSignedFilesMnemonics(scenarioDto) :
                getFileMnemonics(scenarioDto.getApplicantAnswers(), serviceDescriptor, scenarioDto.getServiceCode());
        allFilesInfoForOrderId.stream()
                .filter(dto -> !fileMnemonics.contains(dto.getMnemonic()))
                .forEach(attachment -> {
                    log.info("Deleting redundant file for service {} with orderId {} with mnemonic {}", scenarioDto.getServiceCode(), attachment.getObjectId(), attachment.getMnemonic());
                    terrabyteClient.deleteFile(attachment, userPersonalData.getUserId(), userPersonalData.getToken());
                });
    }

    /**
     * Метод получения мнемоник всех файлов случае сценария с подписанием
     * Так как при использовании УКЭП подписание производится всех файлов,
     * то в signInfo будет уже содержаться информации о всех файлах заявления.
     * Так как может быть файлы подписи от другого пользователя, который добавил делириум
     * информации о которых может не быть в обычных ответах
     * @param scenarioDto
     * @return
     */
    private List<String> getSignedFilesMnemonics(ScenarioDto scenarioDto) {
        SignInfo userSignInfo = scenarioDto.getSignInfoMap().get(scenarioDto.getOrderId());
        List<String> signedFiles = userSignInfo.getSignedFilesInfo().stream().map(SignedFileInfo::getMnemonic).collect(Collectors.toList());
        return signedFiles;
    }

    /**
     * Обычный метод получения информации о всех вложениях
     * Применим для случаев без использования УКЭП
     * @param answerMap
     * @param serviceDescriptor
     * @param serviceId
     * @return
     */
    private List<String> getFileMnemonics(Map<String, ApplicantAnswer> answerMap, ServiceDescriptor serviceDescriptor, String serviceId) {
        List<String> mnemonics = new ArrayList<>();
        for (Map.Entry<String, ApplicantAnswer> entry : answerMap.entrySet()) {
            Optional<FieldComponent> fieldComponent = componentService.getFieldComponent(serviceId, entry, serviceDescriptor);
            if(fieldComponent.isEmpty()) continue;

            switch (fieldComponent.get().getType()) {
                case FileUploadComponent:
                case OrderFileProcessingComponent:
                    mnemonics.addAll(uploadFileComponent.getFilesMnemonics(entry)); break;
                case PhotoUploadComponent: mnemonics.add(PhotoUploadComponent.getPhotoMnemonic(entry)); break;
                case AttachmentContent: mnemonics.addAll(AttachmentContentComponent.getAttachmentMnemonics(entry)); break;
            }

            if(fieldComponent.get().isCycled()) {
                List<Map<String, String>> cycledAnswerItems;
                // после клонирования черновика в делириуме список превращается в одиночный элемент
                if (entry.getValue().getValue().startsWith("{")) {
                    cycledAnswerItems = List.of(AnswerUtil.toStringMap(entry, true));
                } else {
                    cycledAnswerItems = AnswerUtil.toStringMapList(entry, true);
                }
                for (int i = 0; i < cycledAnswerItems.size(); i++) {
                    int arrayIndex = i;
                    Map<String, String> itemsStringMap = cycledAnswerItems.get(arrayIndex);
                    Map<String, ApplicantAnswer> itemsAnswerMap = itemsStringMap.entrySet().stream()
                            .map(e -> AnswerUtil.createRepeatableItemAnswerEntry(e.getKey(), e.getValue(), arrayIndex))
                            .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
                    mnemonics.addAll(getFileMnemonics(itemsAnswerMap, serviceDescriptor, serviceId));
                }
            }
        }
        return mnemonics;
    }
}
