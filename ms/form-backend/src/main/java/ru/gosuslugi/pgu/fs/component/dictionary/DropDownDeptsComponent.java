package ru.gosuslugi.pgu.fs.component.dictionary;

import com.fasterxml.jackson.core.type.TypeReference;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.gosuslugi.pgu.components.BasicComponentUtil;
import ru.gosuslugi.pgu.dto.ApplicantAnswer;
import ru.gosuslugi.pgu.dto.ScenarioDto;
import ru.gosuslugi.pgu.dto.descriptor.FieldComponent;
import ru.gosuslugi.pgu.dto.descriptor.ServiceDescriptor;
import ru.gosuslugi.pgu.dto.descriptor.types.ComponentType;
import ru.gosuslugi.pgu.fs.common.component.AbstractComponent;
import ru.gosuslugi.pgu.fs.common.component.ComponentResponse;
import ru.gosuslugi.pgu.fs.common.service.InitialValueFromService;
import ru.gosuslugi.pgu.fs.service.DictionaryFilterService;
import ru.gosuslugi.pgu.fs.service.LkNotifierService;
import ru.gosuslugi.pgu.pgu_common.nsi.dto.DadataAddressResponse;
import ru.gosuslugi.pgu.pgu_common.nsi.service.NsiDadataService;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

import static java.util.Objects.nonNull;
import static org.springframework.util.StringUtils.hasText;
import static ru.gosuslugi.pgu.components.ComponentAttributes.*;
import static ru.gosuslugi.pgu.dto.descriptor.types.ComponentType.DropDownDepts;

@Slf4j
@Component
@RequiredArgsConstructor
public class DropDownDeptsComponent extends AbstractComponent<Map<String, Object>> {

    private static final String ADDRESS_OKTMO = "addressOktmo";
    private static final String ADDRESS_ATTRIBUTE_NAME = "addressString";

    private final NsiDadataService nsiDadataService;
    private final InitialValueFromService initialValueFromService;
    private final DictionaryFilterService dictionaryFilterService;
    private final LkNotifierService lkNotifierService;

    @Override
    public ComponentType getType() {
        return DropDownDepts;
    }

    @Override
    public ComponentResponse<Map<String, Object>> getInitialValue(FieldComponent component, ScenarioDto scenarioDto, ServiceDescriptor serviceDescriptor) {
        if (Objects.nonNull(component.getAttrs()) && component.getAttrs().containsKey(ADDRESS_ATTRIBUTE_NAME)) {
            var addressAttr = component.getAttrs().get(ADDRESS_ATTRIBUTE_NAME);
            if (Objects.nonNull(addressAttr)) {
                @SuppressWarnings("unchecked")
                Map<String, Object> addressAttrMap = (Map<String, Object>) addressAttr;
                String fullAddress = initialValueFromService.getValue(component, scenarioDto, addressAttrMap);
                if (nonNull(fullAddress)) {
                    DadataAddressResponse dadataAddressResponse = nsiDadataService.getAddress(fullAddress);
                    if (dadataAddressResponse.getError().getCode() == 0) {
                        Map<String, Object> presetValues = getAddressPreSetComponentValues(component, dadataAddressResponse, dadataAddressResponse);
                        presetValues.putAll(dictionaryFilterService.getInitialValue(component, scenarioDto));
                        return ComponentResponse.of(presetValues);
                    }
                }
            }
        }
        Map<String, Object> presetValues = new HashMap<>(dictionaryFilterService.getInitialValue(component, scenarioDto));
        return presetValues.isEmpty() ? ComponentResponse.empty() : ComponentResponse.of(presetValues);
    }

    @Override
    protected void preProcess(FieldComponent component, ScenarioDto scenarioDto) {
        if (Objects.nonNull(component.getAttrs()) && component.getAttrs().containsKey(ADDRESS_ATTRIBUTE_NAME)) {
            var addressAttr = component.getAttrs().get(ADDRESS_ATTRIBUTE_NAME);
            if (Objects.nonNull(addressAttr)) {
                @SuppressWarnings("unchecked")
                Map<String, Object> addressAttrMap = (Map<String, Object>) addressAttr;
                String fullAddress = initialValueFromService.getValue(component, scenarioDto, addressAttrMap);
                if (nonNull(fullAddress)) {
                    DadataAddressResponse dadataAddressResponse = nsiDadataService.getAddress(fullAddress);
                    if (dadataAddressResponse.getError().getCode() == 0 && nonNull(dadataAddressResponse.getOktmo())) {
                        component.getAttrs().put(ADDRESS_OKTMO, dadataAddressResponse.getOktmo());
                    }
                }
            }
        }
    }

    /**
     * Предзаполнить значение компонента
     *
     * @param component - описание предзаполняемого компонента
     * @return - строка, содержащая значение в формате json объекта
     */
    public static Map<String, Object> getAddressPreSetComponentValues(FieldComponent component, DadataAddressResponse dadataAddressResponse, DadataAddressResponse regionDadataAddressResponse) {
        Map<String, Object> presetValues = new HashMap<>();
        if (component.getType() != DropDownDepts) {
            if (log.isWarnEnabled()) log.warn("Вызван неверный метод предустановки значений. Тип компонента {} вместо {}", DropDownDepts, component.getType());
            return presetValues;
        }
        Set<String> fields = BasicComponentUtil.getPreSetFields(component);
        if (
                Objects.nonNull(dadataAddressResponse) && Objects.nonNull(dadataAddressResponse.getAddress())
                        && Objects.nonNull(dadataAddressResponse.getAddress().getElements())
                        && !dadataAddressResponse.getAddress().getElements().isEmpty()
        ) {
            String kladrCode = dadataAddressResponse.getAddress().getElements().get(0).getKladrCode();
            if (Objects.nonNull(kladrCode) && !kladrCode.isBlank()) {
                presetValues.put(REG_CODE_ATTR, "R".concat(kladrCode.substring(0, 2)));
            }
            presetValues.put(GEO_LAT_ATTR, dadataAddressResponse.getGeo_lat());
            presetValues.put(GEO_LON_ATTR, dadataAddressResponse.getGeo_lon());
            presetValues.put(OKATO_ATTR_NAME, dadataAddressResponse.getOkato());
            presetValues.put(OKTMO_ATTR_NAME, dadataAddressResponse.getOktmo());

            presetValues.put(ADDRESS,dadataAddressResponse.getAddress().getFullAddress());
            presetValues.put(ADDRESS_FIAS, dadataAddressResponse.getAddress().getFiasCode());

        }
        if (Objects.nonNull(regionDadataAddressResponse)) {
            if (fields.contains(REG_OKATO_ATTR)) {
                presetValues.put(REG_OKATO_ATTR, regionDadataAddressResponse.getOkato());
            }
            if (fields.contains(REG_OKTMO_ATTR)) {
                presetValues.put(REG_OKTMO_ATTR, regionDadataAddressResponse.getOktmo());
            }
            if (Objects.isNull(dadataAddressResponse)) {
                presetValues.put(GEO_LAT_ATTR, regionDadataAddressResponse.getGeo_lat());
                presetValues.put(GEO_LON_ATTR, regionDadataAddressResponse.getGeo_lon());
            }
        }
        return presetValues;
    }

    @Override
    protected void postProcess(Map.Entry<String, ApplicantAnswer> entry, ScenarioDto scenarioDto, FieldComponent fieldComponent) {
        if(fieldComponent.isSendAnalytics()){
            String address = Optional.ofNullable(entry)
                    .map(Map.Entry::getValue)
                    .map(ApplicantAnswer::getValue)
                    .map(this::getAddressFromValue)
                    .orElse(null);
            lkNotifierService.updateOrderRegionByAddress(scenarioDto.getOrderId(), address);
        }
    }

    private String getAddressFromValue(String value) {
        Map<String, Object> valueMap = jsonProcessingService.fromJson(value, new TypeReference<>() {});
        Map<String, Object> originalItem = (Map<String, Object>) valueMap.get(ORIGINAL_ITEM);
        Map<String, Object> attributeValues = (Map<String, Object>) originalItem.get(ATTRIBUTE_VALUES);
        return String.valueOf(attributeValues.get(ADDRESS_UPPERCASE));
    }
}
