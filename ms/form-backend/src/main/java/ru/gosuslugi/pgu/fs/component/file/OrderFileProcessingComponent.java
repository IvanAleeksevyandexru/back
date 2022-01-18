package ru.gosuslugi.pgu.fs.component.file;

import com.fasterxml.jackson.core.type.TypeReference;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.io.FileUtils;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;
import ru.gosuslugi.pgu.common.core.exception.PguException;
import ru.gosuslugi.pgu.common.core.json.JsonProcessingUtil;
import ru.gosuslugi.pgu.common.esia.search.dto.UserPersonalData;
import ru.gosuslugi.pgu.dto.ApplicantAnswer;
import ru.gosuslugi.pgu.dto.ScenarioDto;
import ru.gosuslugi.pgu.dto.csv.CsvParseDescription;
import ru.gosuslugi.pgu.dto.csv.CsvParseResult;
import ru.gosuslugi.pgu.dto.descriptor.FieldComponent;
import ru.gosuslugi.pgu.dto.descriptor.types.ComponentType;
import ru.gosuslugi.pgu.fs.common.exception.FormBaseException;
import ru.gosuslugi.pgu.fs.common.service.CsvParserService;
import ru.gosuslugi.pgu.fs.component.file.model.OrderFileProcessingDto;
import ru.gosuslugi.pgu.terrabyte.client.TerrabyteClient;
import ru.gosuslugi.pgu.terrabyte.client.model.FileInfo;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import static ru.gosuslugi.pgu.components.ComponentAttributes.IS_NEED_REMOVE_ATTACH_ATTR;
import static ru.gosuslugi.pgu.components.ComponentAttributes.PARSE_DESCRIPTION_ATTR;

/**
 * Компонент обрабытвает CSV файла и сохраняет результаты в черновик
 */
@Slf4j
@Component
public class OrderFileProcessingComponent extends UploadFileComponent {

    private static final String FILE_EXT = "csv";
    private static final String FILE_PARSE_ERROR = "Ошибка при получении данных из файла";

    private final CsvParserService csvParserService;

    public OrderFileProcessingComponent(UserPersonalData userPersonalData,
                                        TerrabyteClient client,
                                        CsvParserService csvParserService) {
        super(userPersonalData, client);
        this.csvParserService = csvParserService;
    }

    @Override
    public ComponentType getType() {
        return ComponentType.OrderFileProcessingComponent;
    }

    @Override
    protected void validateAfterSubmit(Map<String, String> incorrectAnswers, Map.Entry<String, ApplicantAnswer> entry, ScenarioDto scenarioDto, FieldComponent fieldComponent) {
        super.validateAfterSubmit(incorrectAnswers, entry, scenarioDto, fieldComponent);

        CsvParseResult result;
        try {
            List<FileInfo> files = client.getAllFilesByOrderId(scenarioDto.getOrderId(), userPersonalData.getUserId(), userPersonalData.getToken());
            Optional<FileInfo> optionalFile = files.stream().filter(it -> it.getFileExt().equalsIgnoreCase(FILE_EXT)).findFirst();

            if (optionalFile.isPresent()) {
                FileInfo fileInfo = optionalFile.get();
                File file = File.createTempFile(String.format("uid_%d", fileInfo.getFileUid()), "." + fileInfo.getFileExt());
                byte[] bytes = client.getFile(fileInfo, userPersonalData.getUserId(), userPersonalData.getToken());
                FileUtils.writeByteArrayToFile(file, bytes);
                result = csvParserService.parse(file, getParseDescription(fieldComponent));
                if (!file.delete()) {
                    log.warn("Ошибка удаления файла {}", file.getAbsolutePath());
                }
                if (!result.getIsSuccess()) {
                    scenarioDto.getAttachmentInfo().remove(fieldComponent.getId());
                    incorrectAnswers.put(fileInfo.getMnemonic(), result.getError());
                }
            } else {
                throw new PguException("Файл не найден");
            }
        } catch (RestClientException | PguException | IOException e) {
            throw new FormBaseException(FILE_PARSE_ERROR, e);
        }

        String value = scenarioDto.getCurrentValue().get(fieldComponent.getId()).getValue();
        OrderFileProcessingDto answerDto = jsonProcessingService.fromJson(value, OrderFileProcessingDto.class);
        answerDto.setData(result.getData());
        scenarioDto.getCurrentValue().put(fieldComponent.getId(), new ApplicantAnswer(true, jsonProcessingService.toJson(answerDto)));
        correctAttachmentInfo(fieldComponent, scenarioDto);
    }

    @Override
    protected void postProcess(Map.Entry<String, ApplicantAnswer> entry, ScenarioDto scenarioDto, FieldComponent fieldComponent) {}

    /**
     * Получаем условия проверки данных в CSV файле
     */
    private CsvParseDescription getParseDescription(FieldComponent component) {
        if (component.getAttrs().containsKey(PARSE_DESCRIPTION_ATTR)) {
            return JsonProcessingUtil.getObjectMapper().convertValue(component.getAttrs().get(PARSE_DESCRIPTION_ATTR), new TypeReference<>() {
            });
        }

        return new CsvParseDescription();
    }

    private void correctAttachmentInfo(FieldComponent component, ScenarioDto scenarioDto) {
        val isNeedRemoveAttach = (Boolean) component.getAttrs().get(IS_NEED_REMOVE_ATTACH_ATTR);
        val attachmentInfo = scenarioDto.getAttachmentInfo();
        if (Objects.nonNull(attachmentInfo) && Objects.nonNull(isNeedRemoveAttach) && isNeedRemoveAttach) {
            attachmentInfo.remove(component.getId());
        }
    }
}
