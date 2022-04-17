package ru.gosuslugi.pgu.fs.component.userdata;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestClientException;
import ru.atc.carcass.security.rest.model.person.Person;
import ru.gosuslugi.pgu.common.core.exception.NsiExternalException;
import ru.gosuslugi.pgu.dto.ratelimit.RateLimitRequest;
import ru.gosuslugi.pgu.common.core.exception.ExternalServiceException;
import ru.gosuslugi.pgu.common.core.json.JsonProcessingUtil;
import ru.gosuslugi.pgu.common.esia.search.dto.UserPersonalData;
import ru.gosuslugi.pgu.components.FieldComponentUtil;
import ru.gosuslugi.pgu.dto.ApplicantAnswer;
import ru.gosuslugi.pgu.dto.ScenarioDto;
import ru.gosuslugi.pgu.dto.descriptor.FieldComponent;
import ru.gosuslugi.pgu.dto.descriptor.types.ComponentType;
import ru.gosuslugi.pgu.fs.common.component.AbstractComponent;
import ru.gosuslugi.pgu.fs.common.component.ComponentResponse;
import ru.gosuslugi.pgu.fs.component.userdata.model.CarDetailInfoComponentDto;
import ru.gosuslugi.pgu.pgu_common.gibdd.dto.ExternalServiceCallResult;
import ru.gosuslugi.pgu.pgu_common.gibdd.dto.FederalNotaryInfo;
import ru.gosuslugi.pgu.pgu_common.gibdd.dto.FederalNotaryRequest;
import ru.gosuslugi.pgu.pgu_common.gibdd.dto.GibddServiceResponse;
import ru.gosuslugi.pgu.pgu_common.gibdd.dto.VehicleFullInfo;
import ru.gosuslugi.pgu.pgu_common.gibdd.dto.VehicleInfoRequest;
import ru.gosuslugi.pgu.pgu_common.gibdd.service.GibddDataService;
import ru.gosuslugi.pgu.ratelimit.client.RateLimitService;

import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import static ru.gosuslugi.pgu.components.ComponentAttributes.GOV_REG_NUMBER_ATTR;
import static ru.gosuslugi.pgu.components.ComponentAttributes.PDF_LINK_ATTR;
import static ru.gosuslugi.pgu.components.ComponentAttributes.STS_ATTR;
import static ru.gosuslugi.pgu.components.ComponentAttributes.TX_ATTR;
import static ru.gosuslugi.pgu.components.ComponentAttributes.TYPE_ID_ATTR;
import static ru.gosuslugi.pgu.components.ComponentAttributes.VIN_ATTR;

/**
 * Информация о ТС (в другом виде - более детальная)
 * https://jira.egovdev.ru/browse/EPGUCORE-90200 - расширение для 1.4+
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class CarDetailInfoComponent extends AbstractComponent<CarDetailInfoComponentDto> implements CarInfo {

    protected final GibddDataService gibddDataService;
    protected final UserPersonalData userPersonalData;
    private final RateLimitService rateLimitService;

    // Версия сервиса
    private static final String VERSION = "v1";
    // Максимальное количество обращений за контролируемый период времени
    private static final String LIMIT = "5";
    // Период времени, в течение, которого считаются обращения пользователя к услуге. Измеряется в секундах
    private static final String TTL = "600";

    @Override
    public ComponentResponse<CarDetailInfoComponentDto> getInitialValue(FieldComponent component, ScenarioDto scenarioDto) {
        // пытаемся взять сохраненные данные
        CarDetailInfoComponentDto carInfo = getStoredValue(component.getArgument(VIN_ATTR), component.getId(), scenarioDto);
        // если там нет - проверяем нужно ли сразу же получить данные из витрины ГИБДД
        if (carInfo == null && component.getAttrs().getOrDefault("preloadVehicleInfo", "").toString().equalsIgnoreCase("true")) {
            checkAccess(component);

            GibddServiceResponse<VehicleFullInfo> response = getVehicleInfo(component, false);
            carInfo = new CarDetailInfoComponentDto();
            carInfo.setVehicleInfo(response.getData());
            carInfo.setVehicleServiceCallResult(response.getExternalServiceCallResult());
        }

        // задаем ссылку на скачивание
        if (component.getAttrs().containsKey(PDF_LINK_ATTR) && scenarioDto.getOrderId() != null) {
            component.getAttrs().put(PDF_LINK_ATTR, component.getAttrs().get(PDF_LINK_ATTR).toString().replace("${orderId}", scenarioDto.getOrderId().toString()));
        }

        return ComponentResponse.of(carInfo);
    }

    @Override
    public ComponentType getType() {
        return ComponentType.CarDetailInfo;
    }

    public GibddServiceResponse<VehicleFullInfo> getVehicleInfo(FieldComponent component, boolean hasSensitiveData) {
        String vin = component.getArgument(VIN_ATTR);
        String sts = component.getArgument(STS_ATTR);
        String govRegNumber = component.getArgument(GOV_REG_NUMBER_ATTR);
        String tx = String.valueOf(component.getAttrs().get(TX_ATTR));
        String typeId = component.getArgument(TYPE_ID_ATTR);

        Person person = userPersonalData.getPerson();
        VehicleInfoRequest vehicleInfoRequest = buildVehicleInfoRequest(person, typeId, vin, govRegNumber, sts, tx);
        vehicleInfoRequest.setHasSensitiveData(hasSensitiveData);

        GibddServiceResponse<VehicleFullInfo> result = new GibddServiceResponse<>();
        try {
            VehicleFullInfo vehicleInfo = gibddDataService.getVehicleFullInfo(vehicleInfoRequest);
            result.setData(vehicleInfo);
            if (vehicleInfo == null) {
                result.setExternalServiceCallResult(ExternalServiceCallResult.NOT_FOUND_ERROR);
            }
        } catch (RestClientException | ExternalServiceException | NsiExternalException e)  {
            log.error("Не удалось получить данные из сервиса", e);
            result.setExternalServiceCallResult(ExternalServiceCallResult.EXTERNAL_SERVER_ERROR);
            result.setErrorMessage(e.getMessage());
        }

        return result;
    }

    public GibddServiceResponse<FederalNotaryInfo> getFederalNotaryInfo(Long orderId, FieldComponent component) {
        checkAccess(component);

        String vin = component.getArgument(VIN_ATTR);
        String tx = String.valueOf(component.getAttrs().get(TX_ATTR));

        FederalNotaryRequest federalNotaryRequest = FederalNotaryRequest
                .builder()
                .orderId(orderId.toString())
                .vin(vin)
                .tx(tx)
                .build();

        GibddServiceResponse<FederalNotaryInfo> response = new GibddServiceResponse<>();
        try {
            FederalNotaryInfo notaryInfo = gibddDataService.getFederalNotaryInfo(federalNotaryRequest);
            response.setData(notaryInfo);
        } catch (ExternalServiceException | RestClientException | NsiExternalException e) {
            log.error("Не удалось получить данные из сервиса Федеральной нотариальной палаты", e);
            response.setExternalServiceCallResult(ExternalServiceCallResult.EXTERNAL_SERVER_ERROR);
            response.setErrorMessage(e.getMessage());
        }

        return response;
    }

    protected CarDetailInfoComponentDto getStoredValue(String vin, String componentId, ScenarioDto scenarioDto) {
        String displayId = scenarioDto.getDisplay().getId();
        // если происходит обновление страницы - пытаемся взять данные из экрана
        if (scenarioDto.getFinishedAndCurrentScreens().getLast().equals(displayId)) {
            Optional<FieldComponent> optionalComponent = scenarioDto.getDisplay().getComponents().stream().filter(c -> componentId.equals(c.getId())).findFirst();
            if (optionalComponent.isPresent() && StringUtils.hasText(optionalComponent.get().getValue())) {
                return JsonProcessingUtil.fromJson(optionalComponent.get().getValue(), CarDetailInfoComponentDto.class);
            }
        }

        // если происходит переход назад - пытаемся взять данные из кэша ответов
        ApplicantAnswer answer = Optional.ofNullable(scenarioDto.getCachedAnswers().get(componentId)).orElse(scenarioDto.getApplicantAnswers().get(componentId));
        if (!scenarioDto.getFinishedAndCurrentScreens().contains(displayId) && Objects.nonNull(answer) && !StringUtils.isEmpty(answer.getValue())) {
            CarDetailInfoComponentDto carInfo = JsonProcessingUtil.fromJson(answer.getValue(), CarDetailInfoComponentDto.class);
            if (vin.equalsIgnoreCase(carInfo.getVin())) {
                return carInfo;
            }
        }

        return null;
    }

    // Проверка возможности обращения к внешнему сервису
    private void checkAccess(FieldComponent component) {
        Map<String, String> params = FieldComponentUtil.getAttrStringMap(component, "rateLimit");
        var key = component.getId() + "-" + userPersonalData.getUserId();

        var rateLimitRequest = new RateLimitRequest();
        rateLimitRequest.setLimit(Long.parseLong(params.getOrDefault("limit",LIMIT)));
        rateLimitRequest.setTtl(Long.parseLong(params.getOrDefault("ttl", TTL)));
        rateLimitRequest.setVersion(params.getOrDefault("version",VERSION));

        rateLimitService.apiCheck(rateLimitRequest, key, "Проверить автомобиль пока нельзя");
    }
}
