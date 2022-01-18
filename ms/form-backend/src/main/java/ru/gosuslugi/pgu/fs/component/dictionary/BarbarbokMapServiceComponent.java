package ru.gosuslugi.pgu.fs.component.dictionary;

import com.fasterxml.jackson.core.type.TypeReference;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.RequestMethod;
import ru.gosuslugi.pgu.common.core.json.JsonProcessingUtil;
import ru.gosuslugi.pgu.common.esia.search.dto.UserPersonalData;
import ru.gosuslugi.pgu.dto.ScenarioDto;
import ru.gosuslugi.pgu.dto.SmevConverterRequestDto;
import ru.gosuslugi.pgu.dto.descriptor.FieldComponent;
import ru.gosuslugi.pgu.dto.descriptor.types.ComponentType;
import ru.gosuslugi.pgu.fs.common.component.ComponentResponse;
import ru.gosuslugi.pgu.fs.component.dictionary.dto.MapServiceAttributes;
import ru.gosuslugi.pgu.fs.component.logic.BackRestCallComponent;
import ru.gosuslugi.pgu.fs.service.DictionaryFilterService;
import ru.gosuslugi.pgu.fs.service.LkNotifierService;
import ru.gosuslugi.pgu.fs.service.impl.NsiDictionaryFilterHelper;
import ru.gosuslugi.pgu.fs.utils.CalculatedAttributesHelper;
import ru.gosuslugi.pgu.fs.utils.ParseAttrValuesHelper;
import ru.gosuslugi.pgu.pgu_common.nsi.dto.NsiDictionary;
import ru.gosuslugi.pgu.pgu_common.nsi.service.NsiDadataService;
import ru.gosuslugi.pgu.pgu_common.nsi.service.NsiDictionaryService;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Function;

import static ru.gosuslugi.pgu.components.ComponentAttributes.BODY_ATTR;
import static ru.gosuslugi.pgu.components.ComponentAttributes.METHOD_ATTR;
import static ru.gosuslugi.pgu.components.ComponentAttributes.OKATO_ATTR_NAME;
import static ru.gosuslugi.pgu.components.ComponentAttributes.PATH_ATTR;
import static ru.gosuslugi.pgu.components.ComponentAttributes.SERVICE_ID_ATTR;
import static ru.gosuslugi.pgu.components.ComponentAttributes.URL_ATTR;

/**
 * Компонент выбора подразделения ведомства на карте, получаемых из сервиса barbarbok
 */
@Component
public class BarbarbokMapServiceComponent extends AbstractMapComponent<String> implements AttributesDto<MapServiceAttributes> {

    private final BackRestCallComponent backRestCallComponent;
    private final String smevConverterUrl;

    public BarbarbokMapServiceComponent(LkNotifierService lkNotifierService,
                                        CalculatedAttributesHelper calculatedAttributesHelper,
                                        ParseAttrValuesHelper parseAttrValuesHelper,
                                        NsiDadataService nsiDadataService,
                                        UserPersonalData userPersonalData,
                                        DictionaryFilterService dictionaryFilterService,
                                        NsiDictionaryService nsiDictionaryService,
                                        NsiDictionaryFilterHelper nsiDictionaryFilterHelper,
                                        BackRestCallComponent backRestCallComponent,
                                        @Value("${pgu.smev-converter-url}") String smevConverterUrl) {
        super(lkNotifierService, calculatedAttributesHelper, parseAttrValuesHelper, nsiDadataService,
                userPersonalData, dictionaryFilterService, nsiDictionaryService, nsiDictionaryFilterHelper);
        this.backRestCallComponent = backRestCallComponent;
        this.smevConverterUrl = smevConverterUrl;
    }

    @Override
    public ComponentType getType() {
        return ComponentType.BarbarbokMapService;
    }

    @Override
    public TypeReference<MapServiceAttributes> getAttributesDtoType() {
        return new TypeReference<>() {
        };
    }

    @Override
    public ComponentResponse<String> getInitialValue(FieldComponent component, ScenarioDto scenarioDto) {
        var copy = FieldComponent.getCopy(component);

        Map<String, Object> attrs = copy.getAttrs();
        attrs.put(SERVICE_ID_ATTR, scenarioDto.getServiceCode());


        MapServiceAttributes attrsDto = getAttributesDto(copy);
        var addressData = getAddressData(copy, scenarioDto, attrsDto);
        var initialValue = new LinkedHashMap<String, Object>();
        initialValue.putAll(addressData);
        initialValue.putAll(getPersonalDataValues(scenarioDto, attrsDto));

        var smevConverterRequestDto = objectMapper.convertValue(attrs, SmevConverterRequestDto.class);
        var data = smevConverterRequestDto.getData();
        Object okato = addressData.get(OKATO_ATTR_NAME);
        if (okato != null) {
            data = data.replace(okato.toString(), okato.toString().substring(0, 2) + "000");
        }
        data = componentReferenceService.getValueByContext(data, Function.identity(),
                componentReferenceService.buildPlaceholderContext(copy, scenarioDto),
                componentReferenceService.getContexts(scenarioDto));
        smevConverterRequestDto.setData(data);

        attrs.putAll(Map.of(
                METHOD_ATTR, RequestMethod.POST,
                URL_ATTR, smevConverterUrl,
                PATH_ATTR, "/services/get",
                BODY_ATTR, JsonProcessingUtil.toJson(smevConverterRequestDto),
                "esia_auth", true
        ));

        var responseDto = backRestCallComponent.getResponse(copy);
        var dict = mapper.convertValue(responseDto.getResponse(), NsiDictionary.class);
        if (HttpStatus.valueOf(responseDto.getStatusCode()).isError()) {
            dict.getError().setCode(responseDto.getStatusCode());
            dict.getError().setMessage(responseDto.getErrorMessage());
        }
        initialValue.put("barbarbokResponse", dict);

        clearAttrs(component.getAttrs());
        return ComponentResponse.of(jsonProcessingService.toJson(initialValue));
    }

    private void clearAttrs(Map<String, Object> attrs) {
        attrs.remove("data");
        attrs.remove("templateName");
    }
}
