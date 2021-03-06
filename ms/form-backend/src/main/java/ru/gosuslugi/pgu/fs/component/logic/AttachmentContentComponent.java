package ru.gosuslugi.pgu.fs.component.logic;

import com.fasterxml.jackson.core.type.TypeReference;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.gosuslugi.pgu.common.core.exception.PguException;
import ru.gosuslugi.pgu.common.core.json.JsonProcessingUtil;
import ru.gosuslugi.pgu.common.esia.search.dto.UserPersonalData;
import ru.gosuslugi.pgu.dto.ApplicantAnswer;
import ru.gosuslugi.pgu.dto.AttachmentInfo;
import ru.gosuslugi.pgu.dto.ScenarioDto;
import ru.gosuslugi.pgu.dto.descriptor.FieldComponent;
import ru.gosuslugi.pgu.dto.descriptor.types.ComponentType;
import ru.gosuslugi.pgu.fs.common.component.AbstractComponent;
import ru.gosuslugi.pgu.fs.common.component.ComponentResponse;
import ru.gosuslugi.pgu.fs.common.utils.AnswerUtil;
import ru.gosuslugi.pgu.terrabyte.client.TerrabyteClient;
import ru.gosuslugi.pgu.terrabyte.client.model.FileInfo;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * https://jira.egovdev.ru/browse/EPGUCORE-88600
 * https://jira.egovdev.ru/browse/EPGUCORE-90208 - изменение постфикса мнемоники файла для возможности повторного сохранения на террабайте
 */
@Slf4j
@Component
@AllArgsConstructor
public class AttachmentContentComponent extends AbstractComponent<List<FileInfo>> {

    private static final Long MAX_FILE_SIZE = 5242880L;

    private final TerrabyteClient terrabyteClient;
    private final UserPersonalData userPersonalData;

    @Override
    public ComponentResponse<List<FileInfo>> getInitialValue(FieldComponent component, ScenarioDto scenarioDto) {
        List<FileInfo> result = new ArrayList<>();

        List<String> list = (ArrayList<String>) component.getAttrs().get("fileList");
        List<FileInfo> componentFiles = list.stream().map(v -> terrabyteClient.getFileInfo(
                        JsonProcessingUtil.fromJson(v, new TypeReference<>() {}),
                        userPersonalData.getUserId(),
                        userPersonalData.getToken()))
                .collect(Collectors.toList());

        // опция очистки от прикрепленных файлов для ордера
        Map<String, String> mnemonicsMap = new HashMap<>();
        if (component.getBooleanAttr("cleanUp")) {
            mnemonicsMap = deleteAttachmentFiles(scenarioDto, component);
        }

        // add files to zip
        if (component.getAttrs().containsKey("toZip")) {
            Map<String, String> zipMeta = (Map<String, String>) component.getAttrs().get("toZip");
            // используем модифицированную мнемонику
            String mnemonic = buildNewMnemonic(mnemonicsMap, scenarioDto, component);

            try {
                FileInfo file = terrabyteClient.zipFiles(
                        scenarioDto.getOrderId(),
                        false,
                        zipMeta.get("fileName"),
                        mnemonic,
                        componentFiles,
                        userPersonalData.getUserId(),
                        userPersonalData.getToken());
                result.add(file);
            } catch (Exception e) {
                log.error("Couldn't zip attachment files orderId {}, code: {}", scenarioDto.getOrderId(), e.getMessage());
                throw new PguException("Не удалось прикрепить к заявлению файл. Попробуйте позже или обратитесь в службу поддержки");
            }
        } else {
            // add files to order
            for (FileInfo src: componentFiles) {
                FileInfo trg = new FileInfo();
                // используем модифицированную мнемонику
                String mnemonic = buildNewMnemonic(mnemonicsMap, scenarioDto, component);

                trg.setMnemonic(mnemonic);
                trg.setObjectId(scenarioDto.getOrderId());
                trg.setObjectTypeId(2);

                terrabyteClient.copyFile(src, trg, userPersonalData.getUserId(), userPersonalData.getToken());
                FileInfo file = terrabyteClient.getFileInfo(trg, userPersonalData.getUserId(), userPersonalData.getToken());

                result.add(file);
            }
        }

        Long maxSize = component.getAttrs().containsKey("maxSize") ? Long.parseLong(component.getAttrs().get("maxSize").toString()) : MAX_FILE_SIZE;
        for (FileInfo file: result) {
            if (file.getFileSize() > maxSize) {
                deleteAttachmentFiles(scenarioDto, component);
                log.error("File is too large: {}", file);
                throw new PguException("Размер файла превышает максимально допустимый. Обратитесь в службу поддержки");
            }
        }

        scenarioDto.getApplicantAnswers().put(component.getId(), new ApplicantAnswer(true, jsonProcessingService.toJson(result)));
        addAttachmentInfo(scenarioDto, result, component);

        return ComponentResponse.of(result);
    }

    private void addAttachmentInfo(ScenarioDto scenarioDto, List<FileInfo> files, FieldComponent component) {
        if (files.isEmpty()) {
            return;
        }

        var objectId = Long.toString(scenarioDto.getOrderId());
        scenarioDto.getAttachmentInfo().put(
                component.getId(),
                files.stream().map(e -> new AttachmentInfo(
                        Long.toString(e.getFileUid()),
                        e.getMnemonic(),
                        e.getFileName(),
                        objectId,
                        Integer.toString(e.getObjectTypeId()))
                ).collect(Collectors.toList())
        );
    }

    // Кроме удаления строим и возвращаем Map - ключ: стандартная мнемоника, значение: модифицированная мнемоника
    private Map<String, String> deleteAttachmentFiles(ScenarioDto scenarioDto, FieldComponent component) {
        Map<String, String> mnemonicsMap = new HashMap<>();
        for (AttachmentInfo attachmentInfo: scenarioDto.getAttachmentInfo().getOrDefault(component.getId(), new ArrayList<>())) {
            FileInfo file = new FileInfo();
            file.setObjectId(Long.parseLong(attachmentInfo.getObjectId()));
            file.setObjectTypeId(Integer.parseInt(attachmentInfo.getObjectTypeId()));

            String defaultMnemonic = buildDefaultMnemonic(scenarioDto, component);
            String currentMnemonic = attachmentInfo.getUploadMnemonic();
            mnemonicsMap.put(defaultMnemonic, modifyMnemonic(currentMnemonic, defaultMnemonic));

            file.setMnemonic(currentMnemonic);
            file.setFileName(attachmentInfo.getUploadFilename());
            file.setFileUid(Long.parseLong(attachmentInfo.getUploadId()));
            try {
                terrabyteClient.deleteFile(file, userPersonalData.getUserId(), userPersonalData.getToken());
            } catch (Exception e) {
                log.warn("Couldn't delete file orderId {} and type {}, code: {}", scenarioDto.getOrderId(), file.getObjectTypeId(), e.getMessage());
            }
        }
        scenarioDto.getAttachmentInfo().remove(component.getId());
        return mnemonicsMap;
    }

    @Override
    public ComponentType getType() {
        return ComponentType.AttachmentContent;
    }

    public static List<String> getAttachmentMnemonics(Map.Entry<String, ApplicantAnswer> entry) {
        return AnswerUtil.toMapList(entry, true).stream().map(e -> e.get("mnemonic").toString()).collect(Collectors.toList());
    }

    private String buildDefaultMnemonic(ScenarioDto scenarioDto, FieldComponent component) {
        return component.getId() + "." + getType().toString() + "." + component.getAttrs().getOrDefault("mnemonic", "mnemonic") + "." + scenarioDto.getOrderId();
    }

    /**
     * Возвращает новую мнемонику файла
     *
     * @param mnemonicsMap Map, полученный после удаления файлов, ключ: стандартная мнемоника, значение: модифицированная мнемоника
     */
    private String buildNewMnemonic(Map<String, String> mnemonicsMap, ScenarioDto scenarioDto, FieldComponent component) {
        String defaultMnemonic = buildDefaultMnemonic(scenarioDto, component);
        return mnemonicsMap.getOrDefault(defaultMnemonic, modifyMnemonic(defaultMnemonic, defaultMnemonic));
    }

    /**
     * Модифицирует мнемонику файла с дополнительным 0 в постфиксе или инкрементом
     *
     * @param currentMnemonic текущая мнемоника
     * @param defaultMnemonic стандартная мнемоника
     */
    private String modifyMnemonic(String currentMnemonic, String defaultMnemonic) {
        if (currentMnemonic.equals(defaultMnemonic)) {
            return defaultMnemonic + "0";
        } else {
            int index = Integer.parseInt(currentMnemonic.substring(defaultMnemonic.length())) + 1;
            return defaultMnemonic + index;
        }
    }
}