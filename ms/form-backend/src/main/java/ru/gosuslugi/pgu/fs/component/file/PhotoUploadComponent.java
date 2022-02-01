package ru.gosuslugi.pgu.fs.component.file;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import ru.gosuslugi.pgu.common.core.exception.EntityNotFoundException;
import ru.gosuslugi.pgu.common.core.json.JsonProcessingUtil;
import ru.gosuslugi.pgu.dto.ApplicantAnswer;
import ru.gosuslugi.pgu.dto.AttachmentInfo;
import ru.gosuslugi.pgu.dto.ScenarioDto;
import ru.gosuslugi.pgu.dto.descriptor.FieldComponent;
import ru.gosuslugi.pgu.dto.descriptor.types.ComponentType;
import ru.gosuslugi.pgu.fs.common.component.AbstractComponent;
import ru.gosuslugi.pgu.fs.common.utils.AnswerUtil;
import ru.gosuslugi.pgu.fs.service.TerrabyteService;
import ru.gosuslugi.pgu.fs.utils.CycledComponentUtils;
import ru.gosuslugi.pgu.terrabyte.client.model.FileInfo;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static ru.gosuslugi.pgu.common.core.logger.LoggerUtil.error;

@Slf4j
@Component
@RequiredArgsConstructor
public class PhotoUploadComponent extends AbstractComponent<String> {

    public static final String FILE_MNEMONIC = "mnemonic";
    private static final String VALUE_PATTERN = ".value";
    private static final String MNEMONIC_PATTERN = "%s.%s.%s.%s";
    private static final String DEFAULT_MNEMONIC_PATTERN = "%s.%s.%s.%s";
    private static final String UPLOADED_FILE = "uploadedFile";
    private static final String UPLOAD_ID_ATTR_NAME = "uploadId";
    private static final String OBJECT_ID_ATTR_NAME = "objectId";
    private static final String OBJECT_TYPE_ATTR_NAME = "objectType";
    private static final String MNEMONIC_ATTR_NAME = "mnemonic";
    private static final String FILENAME_ATTR_NAME = "name";

    public static final String PARSE_ERROR_MESSAGE = "Ошибка формата данных для загрзуки файлов. Попробуйте позднее";

    private static final ObjectMapper objectMapper = JsonProcessingUtil.getObjectMapper();

    private final TerrabyteService terrabyteService;

    @Override
    public ComponentType getType() {
        return ComponentType.PhotoUploadComponent;
    }

    @Override
    protected void preProcess(FieldComponent component, ScenarioDto scenarioDto) {
        Map<String, Object> uploadedFileMap = getUploadedFileAttributeValue(component);

        if (uploadedFileMap == null) {
            return;
        }
        String uploadId = (String) uploadedFileMap.get(UPLOAD_ID_ATTR_NAME);
        String uploadIndex = CycledComponentUtils.getCurrentIndexForComponentId(component, scenarioDto);
        uploadedFileMap.put(FILE_MNEMONIC, String.format(DEFAULT_MNEMONIC_PATTERN, component.getId(), component.getType().name(), uploadId, uploadIndex));

        List<FileInfo> files;
        try {
            files = terrabyteService.getAllFilesInfoForOrderId(scenarioDto);
            if (!CollectionUtils.isEmpty(files)) {
                String mnemonic = String.format(MNEMONIC_PATTERN, component.getId(), component.getType().name(), uploadId, uploadIndex);
                files.stream()
                        .filter(Objects::nonNull)
                        .filter(fileInfo -> fileInfo.getMnemonic().toLowerCase().startsWith(mnemonic.toLowerCase()))
                        .max(Comparator.comparing(FileInfo::getMnemonic))
                        .ifPresent(fileInfo -> {
                            uploadedFileMap.put(FILE_MNEMONIC, fileInfo.getMnemonic());
                            uploadedFileMap.put("name", fileInfo.getFileName());
                        });
            }
        } catch (EntityNotFoundException e) {
            if (log.isInfoEnabled()) log.info(e.getMessage());
        }
    }

    private Map<String, Object> getUploadedFileAttributeValue(FieldComponent component) {
        LinkedHashMap<String, Object> uploadedFileMap = null;
        Object uploadedFile = component.getAttrs().get(UPLOADED_FILE);
        if (uploadedFile instanceof LinkedHashMap) {
            uploadedFileMap = (LinkedHashMap) uploadedFile;
        }
        return uploadedFileMap;
    }

    @Override
    protected void validateAfterSubmit(Map<String, String> incorrectAnswers, Map.Entry<String, ApplicantAnswer> entry, ScenarioDto scenarioDto, FieldComponent fieldComponent) {
        List<AttachmentInfo> attachmentInfoList = scenarioDto.getAttachmentInfo().getOrDefault(fieldComponent.getId(), new ArrayList<>());
        try {
            LinkedHashMap<String, Object> innerObject = objectMapper.readValue(
                    scenarioDto.getCurrentValue().get(fieldComponent.getId()).getValue(), new TypeReference<>() {});
            String actualMnemonic = innerObject.get(MNEMONIC_ATTR_NAME).toString();
            String actualFilename = innerObject.get(FILENAME_ATTR_NAME).toString();
            Map<String, Object> uploadedFileMap = getUploadedFileAttributeValue(fieldComponent);
            String uploadId = (String) uploadedFileMap.get(UPLOAD_ID_ATTR_NAME);
            String objectId = innerObject.get(OBJECT_ID_ATTR_NAME).toString();
            String objectTypeId = innerObject.get(OBJECT_TYPE_ATTR_NAME).toString();
            AttachmentInfo newAttachmentInfo = new AttachmentInfo(uploadId, actualMnemonic, actualFilename, objectId, objectTypeId);
            attachmentInfoList.removeIf(item -> item.getUploadId().equals(uploadId));
            if (!attachmentInfoList.contains(newAttachmentInfo)) {
                attachmentInfoList.add(newAttachmentInfo);
            }
        } catch (Exception e) {
            error(log, () -> String.format("Error during photo upload: orderId=%s, componentId=%s, currentValue=%s", scenarioDto.getOrderId(),
                    fieldComponent.getId(), scenarioDto.getCurrentValue()), e);
            incorrectAnswers.put(fieldComponent.getId(), PARSE_ERROR_MESSAGE);
            return;
        }
        scenarioDto.getAttachmentInfo().put(fieldComponent.getId(), attachmentInfoList);
        terrabyteService.checkTerrabyteForSpecificFile(incorrectAnswers, entry, fieldComponent, VALUE_PATTERN);
    }

    public static String getPhotoMnemonic(Map.Entry<String, ApplicantAnswer> entry) {
        return AnswerUtil.toMap(entry, true).get(MNEMONIC_ATTR_NAME).toString();
    }
}
