package ru.gosuslugi.pgu.fs.component.file;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import ru.gosuslugi.pgu.dto.ApplicantAnswer;
import ru.gosuslugi.pgu.dto.ScenarioDto;
import ru.gosuslugi.pgu.dto.descriptor.FieldComponent;
import ru.gosuslugi.pgu.dto.descriptor.types.ComponentType;
import ru.gosuslugi.pgu.fs.common.component.AbstractComponent;
import ru.gosuslugi.pgu.fs.service.TerrabyteService;
import ru.gosuslugi.pgu.terrabyte.client.model.FileInfo;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class PhotoEditorComponent extends AbstractComponent<String> {

    private static final String UPLOADS = "uploads";
    private static final String VALUE_PATTERN_EDITOR_COMPONENT = ".value";
    private static final String VALUE_PATTERN_UPLOAD_COMPONENT = ".value.uploads.value[0]";

    private final TerrabyteService terrabyteService;

    @Override
    public ComponentType getType() {
        return ComponentType.PhotoEditorComponent;
    }

    @Override
    protected void preProcess(FieldComponent component, ScenarioDto scenarioDto) {
        if (component.getAttrs() == null) {
            return;
        }
        Object attr = component.getAttrs().get(UPLOADS);

        FileInfo fileInfo = terrabyteService.getFileInfoFromUserAnswer(scenarioDto.getCurrentValue(), scenarioDto.getDisplay().getComponents().get(0), VALUE_PATTERN_UPLOAD_COMPONENT);
        if (fileInfo != null) {
            updateFileParameters(attr, fileInfo, component.getId());
        }
    }

    private void updateFileParameters(Object attr, FileInfo fileInfo, String componentId) {
        if (isAttrEmpty(attr)) {
            return;
        }
        List<?> uploads = (List<?>) attr;
        if (!(uploads.get(0) instanceof LinkedHashMap)) {
            if (log.isWarnEnabled()) log.warn("wrong refernces syntax was found in component with id {}", componentId);
            return;
        }
        LinkedHashMap<String, String> upload = (LinkedHashMap) uploads.get(0);
        upload.put("name", fileInfo.getFileName());
        upload.put("objectId", Long.toString(fileInfo.getObjectId()));
        upload.put("objectType", Integer.toString(fileInfo.getObjectTypeId()));
        upload.put("mnemonic", fileInfo.getMnemonic());
    }

    private boolean isAttrEmpty(Object attr) {
        return !(attr instanceof List) || CollectionUtils.isEmpty((List<?>) attr);
    }

    @Override
    protected void validateAfterSubmit(Map<String, String> incorrectAnswers, Map.Entry<String, ApplicantAnswer> entry, FieldComponent fieldComponent) {
        terrabyteService.checkTerrabyteForSpecificFile(incorrectAnswers, entry, fieldComponent, VALUE_PATTERN_EDITOR_COMPONENT);
    }

}
