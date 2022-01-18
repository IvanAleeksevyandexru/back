package ru.gosuslugi.pgu.fs.service.validation.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONException;
import org.springframework.stereotype.Service;
import ru.gosuslugi.pgu.common.core.exception.JsonParsingException;
import ru.gosuslugi.pgu.common.esia.search.dto.UserPersonalData;
import ru.gosuslugi.pgu.components.ComponentAttributes;
import ru.gosuslugi.pgu.dto.ApplicantAnswer;
import ru.gosuslugi.pgu.dto.ScenarioDto;
import ru.gosuslugi.pgu.dto.descriptor.FieldComponent;
import ru.gosuslugi.pgu.fs.common.component.ComponentResponse;
import ru.gosuslugi.pgu.fs.service.impl.NsiDictionaryFilterHelper;
import ru.gosuslugi.pgu.pgu_common.nsi.dto.NsiDictionary;
import ru.gosuslugi.pgu.pgu_common.nsi.dto.filter.NsiDictionaryFilterRequest;
import ru.gosuslugi.pgu.pgu_common.nsi.service.NsiDictionaryService;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Supplier;

import static ru.gosuslugi.pgu.components.ComponentAttributes.DICTIONARY_NAME_ATTR;
import static ru.gosuslugi.pgu.fs.helper.GenderHelper.MAN_GENDER;

@Slf4j
@Service
@RequiredArgsConstructor
public class NsiDictionaryFilterValidationStrategy extends AbstractDictionaryFilterValidationStrategy {

    private final NsiDictionaryService nsiDictionaryService;
    private final NsiDictionaryFilterHelper nsiDictionaryFilterHelper;
    private final UserPersonalData userPersonalData;

    @Override
    public DictType getDictUrlType() {
        return DictType.dictionary;
    }

    @Override
    public void validateAfterSubmit(Map<String, String> incorrectAnswers,
                                    Map.Entry<String, ApplicantAnswer> entry,
                                    ScenarioDto scenarioDto,
                                    FieldComponent fieldComponent,
                                    Supplier<ComponentResponse<String>> supplier) {
        try {
            NsiDictionary dictionary = getDictionaryForFilter(fieldComponent, scenarioDto, supplier);
            validateDictionaryItem(incorrectAnswers, entry, fieldComponent, dictionary);
        } catch (JSONException e) {
            throw new JsonParsingException(NOT_CORRECT_JSON_FORMAT, e);
        }
    }

    /**
     * Подготавливает запрос к nsi-сервису, предварительно проверяя на наличие в компоненте атрибута {@link ComponentAttributes#DICTIONARY_FILTER_NAME_ATTR}.
     * Получает ответ от nsi-сервиса по фильтру из компонента. Ответ используется для валидации.
     *
     * @param fieldComponent компонент
     * @param scenarioDto    сценарий
     * @param supplier       поставщик ответа компонента, реализуемый в месте вызова метода
     * @return ответ от nsi-сервиса по словарю с именем, заданным атрибутом {@link ComponentAttributes#DICTIONARY_NAME_ATTR}
     * @throws JSONException исключение в случае ошибки парсинга json
     */
    public NsiDictionary getDictionaryForFilter(FieldComponent fieldComponent,
                                                 ScenarioDto scenarioDto,
                                                 Supplier<ComponentResponse<String>> supplier) throws JSONException {
        // Заполнить предустановленные значения
        Map<String, String> presetProperties = nsiDictionaryFilterHelper.getPresetValue(fieldComponent, supplier);
        String dictionaryName = fieldComponent.getAttrs().get(DICTIONARY_NAME_ATTR).toString();
        Map<String, Object> attrs = fieldComponent.getAttrs();
        // TODO: EPGUCORE-66516 Поправить обработку на linkedValues.
        if (attrs.get(DICTIONARY_NAME_ATTR) instanceof List) {
            List<String> dictionaryNames = (List<String>) attrs.get(DICTIONARY_NAME_ATTR);
            String gender = Objects.nonNull(userPersonalData.getUserId()) && Objects.nonNull(userPersonalData.getPerson()) ? userPersonalData.getPerson().getGender() : null;
            if (dictionaryNames != null && dictionaryNames.size() == 2 && gender != null) {
                dictionaryName = gender.equalsIgnoreCase(MAN_GENDER) ? dictionaryNames.get(0) : dictionaryNames.get(1);
            }
        }
        NsiDictionaryFilterRequest requestBody = nsiDictionaryFilterHelper.buildNsiDictionaryFilterRequest(scenarioDto, fieldComponent, presetProperties);
        return nsiDictionaryService.getDictionaryItemForMapsByFilter(dictionaryName, requestBody);
    }
}
