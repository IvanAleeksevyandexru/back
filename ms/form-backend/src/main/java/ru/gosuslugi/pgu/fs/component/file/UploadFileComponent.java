package ru.gosuslugi.pgu.fs.component.file;

import com.fasterxml.jackson.core.type.TypeReference;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import ru.gosuslugi.pgu.common.core.exception.JsonParsingException;
import ru.gosuslugi.pgu.common.core.json.JsonProcessingUtil;
import ru.gosuslugi.pgu.common.esia.search.dto.UserPersonalData;
import ru.gosuslugi.pgu.dto.ApplicantAnswer;
import ru.gosuslugi.pgu.dto.AttachmentInfo;
import ru.gosuslugi.pgu.dto.ScenarioDto;
import ru.gosuslugi.pgu.dto.descriptor.FieldComponent;
import ru.gosuslugi.pgu.dto.descriptor.ServiceDescriptor;
import ru.gosuslugi.pgu.dto.descriptor.types.ComponentType;
import ru.gosuslugi.pgu.fs.common.component.AbstractComponent;
import ru.gosuslugi.pgu.fs.common.utils.AnswerUtil;
import ru.gosuslugi.pgu.fs.component.file.model.FileUploadAnswerDto;
import ru.gosuslugi.pgu.fs.component.file.model.UploadDto;
import ru.gosuslugi.pgu.fs.utils.CycledComponentUtils;
import ru.gosuslugi.pgu.terrabyte.client.TerrabyteClient;
import ru.gosuslugi.pgu.terrabyte.client.model.FileInfo;
import ru.gosuslugi.pgu.terrabyte.client.model.FileType;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.OptionalInt;
import java.util.Set;
import java.util.function.Function;
import java.util.function.ToIntFunction;
import java.util.stream.Collectors;

import static ru.gosuslugi.pgu.components.ComponentAttributes.ARCHIVE_TYPES;
import static ru.gosuslugi.pgu.components.ComponentAttributes.MAX_FILE_COUNT;
import static ru.gosuslugi.pgu.components.ComponentAttributes.MAX_SIZE;

@Component
@RequiredArgsConstructor
@Slf4j
public class UploadFileComponent extends AbstractComponent<String> {

    public static final String PARSE_ERROR_MESSAGE = "Ошибка формата данных для загрузки файлов. Попробуйте позднее";
    public static final String EMPTY_ANSWER_MESSAGE = "Пустой ответ без обязательных файлов";
    private static final String UNABLE_TO_RETRIEVE_DATA = "Не возможно запросить данные о загруженных файлах в сервисе \"Терабайт\"!";
    public static final String REQUIRED_FILES_WERE_NOT_FOUND = "Не найдено обязательных файлов!";
    public static final String EMPTY_FILES_NOT_ALLOWED = "Запрещена загрузка пустых файлов! Проверьте загружаемый файл";
    /**
     * Данный параметр используется на front-end при формировании поля mnemonic.
     * (See {@link UploadFileComponent#checkFiles})
     */
    private static final String UPLOAD_ID_KEY = "uploadId";
    /**
     * Указатель на список загружаемых файлов.
     */
    private static final String UPLOADS_KEY = "uploads";
    /**
     * Предполагает наличие смежных загружаемых файлов.
     */
    private static final String RELATED_UPLOADS_KEY = "relatedUploads";
    /**
     * Данное поле в JSON может принимать значения true/false
     * и указывает на необходимость проверять наличие файла.
     * Отсутствие поля в описании равносильно fileRequired=false.
     */
    private static final String FILE_REQUIRED = "fileRequired";
    private static final String MNEMONIC_PATTERN = "%s.%s.%s";

    public static final int DEFAULT_ATTACHMENT_MAX_SIZE = 157286400; // 150mb (in bytes)
    public static final int DEFAULT_ATTACHMENT_MAX_FILE_COUNT = 10;

    @NonNull
    protected final UserPersonalData userPersonalData;

    @NonNull
    protected final TerrabyteClient client;

    @Override
    public ComponentType getType() {
        return ComponentType.FileUploadComponent;
    }

    @Override
    public void preProcess(FieldComponent component, ScenarioDto scenarioDto, ServiceDescriptor serviceDescriptor) {
        if (component.isComponentInCycle()) {
            String uploadIndex = CycledComponentUtils.getCurrentIndexForComponentId(component, scenarioDto);
            List<Map<String, Object>> uploadsDescription = (List<Map<String, Object>>) component.getAttrs().get(UPLOADS_KEY);
            uploadsDescription.forEach(it -> it.computeIfPresent(UPLOAD_ID_KEY, (k, v) -> v + uploadIndex));
        }

        calcSharedAttributes(component, scenarioDto, serviceDescriptor);

        List<Map<String, Object>> uploadsDescription = (List<Map<String, Object>>) component.getAttrs().get(UPLOADS_KEY);
        uploadsDescription.forEach(it -> it.computeIfPresent("label", (k, v) ->
                componentReferenceService.getValueByContext((String) v, Function.identity(),
                        componentReferenceService.buildPlaceholderContext(component, scenarioDto),
                        componentReferenceService.getContexts(scenarioDto))));
    }

    @Override
    protected void postProcess(Map.Entry<String, ApplicantAnswer> entry, ScenarioDto scenarioDto, FieldComponent fieldComponent) {
        List<String> zipSuffixes = (List<String>) fieldComponent.getAttrs().getOrDefault(ARCHIVE_TYPES, Collections.emptyList());
        if (!CollectionUtils.isEmpty(zipSuffixes)) {
            List<FileInfo> fileInfoList = client.getAllFilesByOrderId(scenarioDto.getOrderId(), userPersonalData.getUserId(), userPersonalData.getToken());
            TerrabyteFileProcessor terrabyteFileProcessor = TerrabyteFileProcessor.of(client, userPersonalData);
            fileInfoList.stream()
                    .filter(fileInfo -> Objects.nonNull(fileInfo.getFileExt()) && zipSuffixes.contains(fileInfo.getFileExt().toUpperCase()))
                    .forEach(fileInfo -> {
                        /* файлы, имеющие тип, отмеченный в массиве archiveTypes необходимо упаковать в .zip архив */
                        terrabyteFileProcessor.replaceByZipped(fileInfo);
                        List<AttachmentInfo> attachmentInfoList = scenarioDto.getAttachmentInfo().getOrDefault(fieldComponent.getId(), Collections.emptyList());
                        /* добавить .zip суффикс к имени файла в attachmentInfo */
                        attachmentInfoList.stream()
                                .filter(item -> item.getUploadMnemonic().equals(fileInfo.getMnemonic()))
                                .findFirst()
                                .ifPresent(attachmentInfo -> {
                                    attachmentInfo.setUploadMnemonic(attachmentInfo.getUploadMnemonic() + ".ZIP");
                                    attachmentInfo.setUploadFilename(terrabyteFileProcessor.toZipFileName(fileInfo));
                                });
                    });
        }
    }

    @Override
    protected void validateAfterSubmit(Map<String, String> incorrectAnswers, Map.Entry<String, ApplicantAnswer> entry, ScenarioDto scenarioDto, FieldComponent fieldComponent) {
        Set<String> requiredUploadMnemonicPatterns = getRequiredUploadMnemonicPatterns(fieldComponent);
        boolean answerValueEmpty = StringUtils.isEmpty(AnswerUtil.getValue(entry));
        if (requiredUploadMnemonicPatterns.isEmpty()) {
            if (!answerValueEmpty) {
                addAttachmentInfoToScenarioDto(incorrectAnswers, fieldComponent, scenarioDto, entry);
            }
            return;
        }
        if (answerValueEmpty) {
            incorrectAnswers.put(entry.getKey(), EMPTY_ANSWER_MESSAGE);
            return;
        }

        List<FileInfo> files = client.getAllFilesByOrderId(scenarioDto.getOrderId(),
                userPersonalData.getUserId(), userPersonalData.getToken());
        if (files.isEmpty()) {
            incorrectAnswers.put(entry.getKey(), UNABLE_TO_RETRIEVE_DATA);
        } else {
            var emptyFilesOptional = files.stream()
                    .filter(f -> Objects.nonNull(f.getFileSize()) && f.getFileSize() <= 0).findAny();
            if (emptyFilesOptional.isPresent()) {
                incorrectAnswers.put(entry.getKey(), EMPTY_FILES_NOT_ALLOWED);
            }
            if (!checkFiles(files, entry, requiredUploadMnemonicPatterns)) {
                incorrectAnswers.put(entry.getKey(), REQUIRED_FILES_WERE_NOT_FOUND);
            }
        }
        if (!incorrectAnswers.isEmpty()) {
            return;
        }

        addAttachmentInfoToScenarioDto(incorrectAnswers, fieldComponent, scenarioDto, entry);
    }

    private void calcSharedAttributes(FieldComponent component, ScenarioDto scenarioDto, ServiceDescriptor serviceDescriptor) {
        calcFileUploadSharedAttributes(component, scenarioDto, serviceDescriptor, MAX_FILE_COUNT, FileUploadAnswerDto::getTotalCount);
        calcFileUploadSharedAttributes(component, scenarioDto, serviceDescriptor, MAX_SIZE, FileUploadAnswerDto::getTotalSize);
    }

    private void calcFileUploadSharedAttributes(FieldComponent component,
                                                ScenarioDto scenarioDto,
                                                ServiceDescriptor serviceDescriptor,
                                                String attribute,
                                                ToIntFunction<FileUploadAnswerDto> attributeToIntFunction) {
        if (component.getAttrs().containsKey(attribute)) return;
        int initialValue = calcComponentAttributesIntMax(scenarioDto, serviceDescriptor, attribute);
        int totalValue = calcFileUploadAnswerAttributesSum(scenarioDto, attributeToIntFunction);
        int result = initialValue - totalValue;
        component.getAttrs().put(attribute, result);
    }

    private int calcComponentAttributesIntMax(ScenarioDto scenarioDto, ServiceDescriptor serviceDescriptor, String attribute) {
        Set<String> uploadIds = scenarioDto.getAttachmentInfo().keySet();

        OptionalInt max = uploadIds.stream()
                .map(serviceDescriptor::getFieldComponentById)
                .flatMap(Optional::stream)
                .filter(component -> component.getAttrs().containsKey(attribute))
                .mapToInt(component -> (int) component.getAttrs().get(attribute))
                .max();

        int defaultValue = 0;
        if (MAX_SIZE.equals(attribute)) defaultValue = DEFAULT_ATTACHMENT_MAX_SIZE;
        if (MAX_FILE_COUNT.equals(attribute)) defaultValue = DEFAULT_ATTACHMENT_MAX_FILE_COUNT;

        return max.orElse(defaultValue);
    }

    private int calcFileUploadAnswerAttributesSum(ScenarioDto scenarioDto, ToIntFunction<FileUploadAnswerDto> attributeFunction) {
        Set<String> uploadIds = scenarioDto.getAttachmentInfo().keySet();

        return scenarioDto.getApplicantAnswers().entrySet().stream()
                .filter(entry -> uploadIds.contains(entry.getKey()))
                .map(Map.Entry::getValue)
                .map(answer -> JsonProcessingUtil.fromJson(answer.getValue(), new TypeReference<FileUploadAnswerDto>() {}))
                .filter(dto -> Objects.nonNull(dto.getTotalSize()))
                .mapToInt(attributeFunction)
                .sum();
    }

    private void addAttachmentInfoToScenarioDto(Map<String, String> incorrectAnswers, FieldComponent fieldComponent, ScenarioDto scenarioDto, Map.Entry<String, ApplicantAnswer> entry) {
        List<AttachmentInfo> attachmentInfoList = scenarioDto.getAttachmentInfo().getOrDefault(fieldComponent.getId(), new ArrayList<>());
        FileUploadAnswerDto fileUploadAnswer = jsonProcessingService.fromJson(AnswerUtil.getValue(entry), FileUploadAnswerDto.class);
        try {
            List<UploadDto> componentUploads = fileUploadAnswer.getUploads();
            if (componentUploads != null) {
                componentUploads.stream().filter(upload -> upload.getPdfFileName() == null).forEach(upload -> {
                    String currentUploadId = upload.getUploadId();
                    attachmentInfoList.removeIf(item -> item.getUploadId().equals(currentUploadId));
                    upload.getValue().forEach(file -> {
                        AttachmentInfo newAttachment = new AttachmentInfo(
                                currentUploadId,
                                file.getMnemonic(),
                                file.getFileName(),
                                String.valueOf(file.getObjectId()),
                                String.valueOf(file.getObjectTypeId()));
                        if (!attachmentInfoList.contains(newAttachment)) {
                            attachmentInfoList.add(newAttachment);
                        }
                    });
                });
            }
        } catch (Exception e) {
            log.error("While processing attachment: {}", e.getMessage(), e);
            incorrectAnswers.put(entry.getKey(), PARSE_ERROR_MESSAGE);
        }
        scenarioDto.getAttachmentInfo().put(fieldComponent.getId(), attachmentInfoList);
    }

    /**
     * Метод  проверяет список файлов по типу прекрепленного файла и виду мнемонического описания
     * <br> на front-end объект mnemonic формируется по правилу:
     * <br> {componentId}.{componentType}.{uploadId}.{fileNumber}
     *
     * @param files - список файлов полученный из системмы Терабайт.
     * @return true если в списке есть хотя бы один файл удовлетворяющий условию.
     */
    private boolean checkFiles(List<FileInfo> files, Map.Entry<String, ApplicantAnswer> entry, Set<String> requiredUploadMnemonicPatterns) {
        if (CollectionUtils.isEmpty(files)) {
            return false;
        }
        List<String> terrabyteFileMnemonics = getMnemonicDetailForComponentId(files, entry.getKey());
        List<String> answerFilesMnemonics = getFilesMnemonics(entry);
        Map<String, List<String>> requiredUploadPatternToAnswerFileMnemonics = requiredUploadMnemonicPatterns.stream()
                .collect(Collectors.toMap(Function.identity(), pattern ->
                        answerFilesMnemonics.stream().filter(m -> m.matches(pattern)).collect(Collectors.toList())));
        Collection<List<String>> values = requiredUploadPatternToAnswerFileMnemonics.values();
        if (values.stream().anyMatch(CollectionUtils::isEmpty)) {
            return false;
        }
        return terrabyteFileMnemonics.containsAll(values.stream().flatMap(Collection::stream).collect(Collectors.toList()));


    }

    /**
     * Метод возвращает список всех мнемонических описаний файлов соответствующих этому компоненту!
     */
    private List<String> getMnemonicDetailForComponentId(List<FileInfo> files, String componentId) {
        String mnemonicType = prepareComponentIdPattern(componentId);
        return files.stream().filter(details -> {
            if (FileType.ATTACHMENT.getType() != details.getObjectTypeId()) {
                return false;
            }
            return details.getMnemonic().matches(mnemonicType);
        }).map(FileInfo::getMnemonic).collect(Collectors.toList());
    }

    /**
     * @return componentId.ComponentType.*
     */
    private String prepareComponentIdPattern(String componentId) {
        return componentId + '.' +
                ComponentType.FileUploadComponent.name() + ".*";
    }

    /**
     * @param fieldComponent - используется для извлечения списка {@value #UPLOAD_ID_KEY}
     * @return Список выражений для поиска по {@link #UPLOAD_ID_KEY}
     */
    private Set<String> getRequiredUploadMnemonicPatterns(FieldComponent fieldComponent) {
        Set<String> patterns = new HashSet<>();
        addRequiredUploadMnemonicPattern(fieldComponent.getAttrs(), patterns);
        return patterns;
    }

    private void addRequiredUploadMnemonicPattern(Map<String, ?> map, Set<String> patterns) {
        Object uploads = map.get(UPLOADS_KEY);
        if (uploads instanceof List) {
            List<Map<String, ?>> uploadsList = (List<Map<String, ?>>) uploads;
            for (Map<String, ?> uploadsMap : uploadsList) {
                Object o = uploadsMap.get(UPLOAD_ID_KEY);
                Object r = uploadsMap.get(FILE_REQUIRED);
                if (o != null && r != null) {
                    String uploadId = (String) o;
                    boolean fileRequired = Boolean.parseBoolean(String.valueOf(r));
                    if (StringUtils.hasText(uploadId) && fileRequired) {
                        patterns.add(".*" + uploadId + ".*");
                    }
                }
                Object relatedUploads = uploadsMap.get(RELATED_UPLOADS_KEY);
                if (relatedUploads instanceof List) {
                    List<Map<String, ?>> relatedUploadsList = (List<Map<String, ?>>) relatedUploads;
                    for (Map<String, ?> relatedUploadMap : relatedUploadsList) {
                        // Предполагаются вложенные объекты, увы пришлось рекурсией, но глубина погружения не велика.
                        addRequiredUploadMnemonicPattern(relatedUploadMap, patterns);
                    }
                }
            }
        }
    }

    public List<String> getFilesMnemonics(Map.Entry<String, ApplicantAnswer> entry) {
        List<String> mnemonics = new ArrayList<>();
        try {
            FileUploadAnswerDto fileUploadAnswer = jsonProcessingService.fromJson(AnswerUtil.getValue(entry), FileUploadAnswerDto.class);
            List<UploadDto> componentUploads = fileUploadAnswer.getUploads();
            componentUploads.forEach(upload -> upload.getValue().forEach(file -> mnemonics.add(file.getMnemonic())));
        } catch (JsonParsingException e) {
            //в случае если файлы необязательные и отсуствуют вылетает из-за формата фронта
        }
        return mnemonics;
    }
}
