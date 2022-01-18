package ru.gosuslugi.pgu.fs.service.validation.impl;

import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.JsonPath;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.gosuslugi.pgu.dto.ApplicantAnswer;
import ru.gosuslugi.pgu.dto.ScenarioDto;
import ru.gosuslugi.pgu.dto.descriptor.FieldComponent;
import ru.gosuslugi.pgu.fs.common.component.ComponentResponse;
import ru.gosuslugi.pgu.fs.common.utils.AnswerUtil;
import ru.gosuslugi.pgu.pgu_common.nsi.dto.NsiSuggestDictionaryItem;
import ru.gosuslugi.pgu.pgu_common.nsi.service.NsiDictionaryService;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Supplier;

import static ru.gosuslugi.pgu.components.ComponentAttributes.DICTIONARY_NAME_ATTR;
import static ru.gosuslugi.pgu.components.ComponentAttributes.ORIGINAL_ITEM;

@Slf4j
@Service
@RequiredArgsConstructor
public class NsiSuggestFilterValidationStrategy extends AbstractDictionaryFilterValidationStrategy {

    public static final String ATTRIBUTE_NAME_FOR_CHECK = "fullname";
    private static final String ACT_REC_REGISTRATOR = "act_rec_registrator";
    private static final String ATTRIBUTE_TEXT = "text";
    private final NsiDictionaryService nsiDictionaryService;

    @Override
    public DictType getDictUrlType() {
        return DictType.nsiSuggest;
    }


    @Override
    public void validateAfterSubmit(Map<String, String> incorrectAnswers,
                                    Map.Entry<String, ApplicantAnswer> entry,
                                    ScenarioDto scenarioDto,
                                    FieldComponent fieldComponent,
                                    Supplier<ComponentResponse<String>> supplier) {

        String dictionaryName = fieldComponent.getAttrs().get(DICTIONARY_NAME_ATTR).toString();
        DocumentContext documentContext = JsonPath.parse(AnswerUtil.getValue(entry));
        String itemToCheck = "";
        LinkedHashMap<String,Map> originalItemMap = (LinkedHashMap<String, Map>) jsonProcessingService.getFieldFromContext(ORIGINAL_ITEM, documentContext, Map.class);
        if(Objects.nonNull(originalItemMap)) {
            itemToCheck = String.valueOf(originalItemMap.get(ATTRIBUTE_NAME_FOR_CHECK));
        }
        //для компонента ru.gosuslugi.pgu.fs.component.userdata.MaritalStatusInput
        LinkedHashMap originalItem = jsonProcessingService.getFieldFromContext(ACT_REC_REGISTRATOR, documentContext, LinkedHashMap.class);
        if(Objects.nonNull(originalItem)) {
            itemToCheck = String.valueOf(originalItem.get(ATTRIBUTE_TEXT));
        }
        Map<String,Object> nsiDictionaryItem = nsiDictionaryService.getNsiDictionaryItemByValue(dictionaryName, ATTRIBUTE_NAME_FOR_CHECK,itemToCheck);

        if (nsiDictionaryItem.isEmpty()) {
            incorrectAnswers.put(entry.getKey(),
                    String.format("NSI Suggest dictionary %s doesn't contain %s item", dictionaryName, itemToCheck));
        }
    }
}
