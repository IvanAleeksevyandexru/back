package ru.gosuslugi.pgu.fs.component.address;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.gosuslugi.pgu.common.core.exception.JsonParsingException;
import ru.gosuslugi.pgu.common.core.json.JsonProcessingUtil;
import ru.gosuslugi.pgu.dto.ApplicantAnswer;
import ru.gosuslugi.pgu.dto.ScenarioDto;
import ru.gosuslugi.pgu.dto.UserRegionDto;
import ru.gosuslugi.pgu.dto.descriptor.FieldComponent;
import ru.gosuslugi.pgu.dto.descriptor.ServiceDescriptor;
import ru.gosuslugi.pgu.dto.descriptor.types.ComponentType;
import ru.gosuslugi.pgu.fs.common.component.AbstractComponent;
import ru.gosuslugi.pgu.fs.common.component.ComponentResponse;
import ru.gosuslugi.pgu.fs.service.LkNotifierService;
import ru.gosuslugi.pgu.pgu_common.nsi.dto.DadataAddress;
import ru.gosuslugi.pgu.pgu_common.nsi.dto.DadataAddressResponse;
import ru.gosuslugi.pgu.pgu_common.nsi.service.NsiDadataService;

import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import static java.util.Objects.nonNull;
import static org.apache.commons.lang.StringUtils.isBlank;
import static org.springframework.util.StringUtils.hasText;

@Slf4j
@Component
@RequiredArgsConstructor
public class CityInputComponent extends AbstractComponent<DadataAddressResponse> {


    private final NsiDadataService nsiDadataService;


    private final LkNotifierService lkNotifierService;

    @Override
    public ComponentType getType() {
        return ComponentType.CityInput;
    }

    @Override
    public ComponentResponse<DadataAddressResponse> getInitialValue(FieldComponent component, ScenarioDto scenarioDto, ServiceDescriptor serviceDescriptor) {
        if (component.getBooleanAttr("useBarLocation")) {
            var userRegion = getDadataRegionFromServiceInfo(scenarioDto);
            if (Objects.nonNull(userRegion)) return ComponentResponse.of(userRegion);
        }
        return ComponentResponse.empty();
    }

    @Override
    protected void validateAfterSubmit(Map<String, String> incorrectAnswers, Map.Entry<String, ApplicantAnswer> entry, FieldComponent fieldComponent) {
        DadataAddressResponse dadataAddressResponse = Optional.ofNullable(entry)
                .map(Map.Entry::getValue)
                .map(ApplicantAnswer::getValue)
                .map(this::getDadataAddressFromJsonString)
                .orElse(null);
        String address = Optional.ofNullable(dadataAddressResponse)
                .map(DadataAddressResponse::getAddress)
                .map(DadataAddress::getFullAddress)
                .orElse(null);

        if (isBlank(address)) {
            if (fieldComponent.isRequired()) {
                incorrectAnswers.put(entry.getKey(), "Адрес не задан");
            }
            return;
        }
        String error = validateAddress(dadataAddressResponse);
        if (nonNull(error)) {
            incorrectAnswers.put(entry.getKey(), error);
        }
    }

    /**
     * Конвертирует адрес из json объекта в объект класса {@link DadataAddressResponse}
     * @param dadataJson json объекта адреса регистрации
     * @return адрес
     */
    private DadataAddressResponse getDadataAddressFromJsonString(String dadataJson) {
        DadataAddressResponse result;
        try {
            result = JsonProcessingUtil.fromJson(dadataJson, DadataAddressResponse.class);
        } catch (JsonParsingException e) {
            if (log.isWarnEnabled()) log.warn("Error by json parsing \"" + dadataJson + "\" address to " + DadataAddressResponse.class + " class. Details: " + e);
            result = null;
        }
        return result;
    }

    private DadataAddressResponse getDadataRegionFromServiceInfo(ScenarioDto scenarioDto) {
        UserRegionDto userRegion = scenarioDto.getServiceInfo().getUserRegion();
        if (Objects.isNull(userRegion) || Objects.isNull(userRegion.getPath())) return null;
        return nsiDadataService.getAddress(userRegion.getPath());
    }


    private static String validateAddress(DadataAddressResponse value) {
        if (
                value.getDadataQc() != 0
                        && value.getDadataQc() != 3
        ) {
            return "Адрес не распознан";
        }
        return null;
    }

    @Override
    protected void postProcess(Map.Entry<String, ApplicantAnswer> entry, ScenarioDto scenarioDto, FieldComponent fieldComponent) {
        if(fieldComponent.isSendAnalytics()){
            String okato = Optional.ofNullable(entry)
                    .map(Map.Entry::getValue)
                    .map(ApplicantAnswer::getValue)
                    .map(this::getDadataAddressFromJsonString)
                    .map(DadataAddressResponse::getOkato)
                    .orElse(null);
            lkNotifierService.updateOrderRegion(scenarioDto.getOrderId(), okato);
        }
    }
}
