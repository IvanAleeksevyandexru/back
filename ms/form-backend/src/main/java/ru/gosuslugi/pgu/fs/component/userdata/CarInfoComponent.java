package ru.gosuslugi.pgu.fs.component.userdata;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import ru.atc.carcass.security.rest.model.person.Person;
import ru.gosuslugi.pgu.common.core.exception.NsiExternalException;
import ru.gosuslugi.pgu.common.core.json.JsonProcessingUtil;
import ru.gosuslugi.pgu.common.esia.search.dto.UserPersonalData;
import ru.gosuslugi.pgu.dto.ApplicantAnswer;
import ru.gosuslugi.pgu.dto.ScenarioDto;
import ru.gosuslugi.pgu.dto.descriptor.FieldComponent;
import ru.gosuslugi.pgu.dto.descriptor.types.ComponentType;
import ru.gosuslugi.pgu.fs.common.component.AbstractComponent;
import ru.gosuslugi.pgu.fs.common.component.ComponentResponse;
import ru.gosuslugi.pgu.fs.component.userdata.model.CarInfoComponentDto;
import ru.gosuslugi.pgu.pgu_common.gibdd.dto.*;
import ru.gosuslugi.pgu.pgu_common.gibdd.service.GibddDataService;

import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import static ru.gosuslugi.pgu.components.ComponentAttributes.*;

/**
 * https://jira.egovdev.ru/browse/EPGUCORE-90200 - расширение для 1.4+ - 404
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class CarInfoComponent extends AbstractComponent<CarInfoComponentDto> implements CarInfo {

    @Value("${gibdd.request-timeout:20}")
    private Integer requestTimeout;

    private final GibddDataService gibddDataService;
    private final UserPersonalData userPersonalData;

    public static final String RETRY_VALUE = "0";

    @Override
    public ComponentResponse<CarInfoComponentDto> getInitialValue(FieldComponent component, ScenarioDto scenarioDto) {
        String vin = component.getArgument(VIN_ATTR);
        String tx = String.valueOf(component.getAttrs().get(TX_ATTR));
        String typeId = component.getArgument(TYPE_ID_ATTR);

        // пытаемся сначало взять данные из кэша
        CarInfoComponentDto carInfo = getStoredValue(vin, component.getId(), scenarioDto);
        if (carInfo == null) {
            // если typeId = GRZ_RegistrationDocNumber, то это версия 1.4+
            if (VehicleInfoRequestType.GRZ_RegistrationDocNumber.getId().equals(typeId)) {
                String govRegNumber = component.getArgument(GOV_REG_NUMBER_ATTR);
                String sts = component.getArgument(STS_ATTR);
                carInfo = getCarInfo(scenarioDto.getOrderId().toString(), typeId, govRegNumber, sts, tx);
            } else {
                carInfo = getCarInfo(scenarioDto.getOrderId().toString(), vin, tx);
            }
        }

        return ComponentResponse.of(carInfo);
    }

    @Override
    @SuppressWarnings("unchecked")
    public void validateAfterSubmit(Map<String, String> incorrectAnswers, Map.Entry<String, ApplicantAnswer> entry, ScenarioDto scenarioDto, FieldComponent component) {
        ApplicantAnswer answer = entry.getValue();
        if (RETRY_VALUE.equals(answer.getValue())) {
            Optional<FieldComponent> fieldBox = scenarioDto.getDisplay().getComponents().stream().filter(el -> el.getId().equals(entry.getKey())).findAny();
            if (fieldBox.isPresent()) {
                FieldComponent field = fieldBox.get();
                field.setValue(JsonProcessingUtil.toJson(getCarInfo(scenarioDto.getOrderId().toString(), field.getArguments().get(VIN_ATTR),
                        String.valueOf(component.getAttrs().get(TX_ATTR)))));
                field.setAttrs(component.getAttrs());
            }

            incorrectAnswers.put(entry.getKey(), "повторный запрос данных");
        }
    }

    @Override
    public ComponentType getType() {
        return ComponentType.CarInfo;
    }

    private CarInfoComponentDto getCarInfo(String orderId, String vin, String tx) {
        Person person = userPersonalData.getPerson();
        VehicleInfoRequest vehicleInfoRequest = VehicleInfoRequest
                .builder()
                .lastName(person.getLastName())
                .firstName(person.getFirstName())
                .middleName(person.getMiddleName())
                .vin(vin)
                .tx(tx)
                .build();
        FederalNotaryRequest federalNotaryRequest = FederalNotaryRequest
                .builder()
                .orderId(orderId)
                .vin(vin)
                .tx(tx)
                .build();

        CompletableFuture<GibddServiceResponse<VehicleInfo>> vehicleInfoFuture = gibddDataService
                .getAsyncVehicleInfo(vehicleInfoRequest)
                .orTimeout(requestTimeout, TimeUnit.SECONDS)
                .handle((result, ex) -> ex != null ? new GibddServiceResponse<>(null, ExternalServiceCallResult.EXTERNAL_SERVER_ERROR, ex.getMessage()) : result);
        CompletableFuture<GibddServiceResponse<FederalNotaryInfo>> notaryInfoFuture = gibddDataService
                .getAsyncFederalNotaryInfo(federalNotaryRequest)
                .orTimeout(requestTimeout, TimeUnit.SECONDS)
                .handle((result, ex) -> ex != null ? new GibddServiceResponse<>(null, ExternalServiceCallResult.EXTERNAL_SERVER_ERROR, ex.getMessage()) : result);
        CompletableFuture<Void> allFuture = CompletableFuture.allOf(vehicleInfoFuture, notaryInfoFuture);
        allFuture.join();

        CarInfoComponentDto result = new CarInfoComponentDto();
        result.setVin(vin);
        fillVehicleInfo(result, vehicleInfoFuture);
        fillFederalNotaryInfo(result, notaryInfoFuture);

        return result;
    }

    public CarInfoComponentDto getStoredValue(String vin, String componentId, ScenarioDto scenarioDto) {
        String displayId = scenarioDto.getDisplay().getId();
        // если происходит обновление страницы - пытаемся взять данные из экрана
        if (scenarioDto.getFinishedAndCurrentScreens().getLast().equals(displayId)) {
            Optional<FieldComponent> optionalComponent = scenarioDto.getDisplay().getComponents().stream().filter(c -> componentId.equals(c.getId())).findFirst();
            if (optionalComponent.isPresent()) {
                return JsonProcessingUtil.fromJson(optionalComponent.get().getValue(), CarInfoComponentDto.class);
            }
        }

        // если происходит переход назад - пытаемся взять данные из кэша ответов
        ApplicantAnswer answer = Optional.ofNullable(scenarioDto.getCachedAnswers().get(componentId)).orElse(scenarioDto.getApplicantAnswers().get(componentId));
        if (!scenarioDto.getFinishedAndCurrentScreens().contains(displayId) && Objects.nonNull(answer) && !StringUtils.isEmpty(answer.getValue())) {
            CarInfoComponentDto carInfo = JsonProcessingUtil.fromJson(answer.getValue(), CarInfoComponentDto.class);
            if (vin.equalsIgnoreCase(carInfo.getVin())) {
                return carInfo;
            }
        }

        return null;
    }

    private void fillVehicleInfo(CarInfoComponentDto result, CompletableFuture<GibddServiceResponse<VehicleInfo>> future) {
        GibddServiceResponse<VehicleInfo> vehicleInfoResult = future.join();
        result.setVehicleInfo(vehicleInfoResult.getData());
        result.setVehicleServiceCallResult(vehicleInfoResult.getExternalServiceCallResult());
    }

    private void fillFederalNotaryInfo(CarInfoComponentDto result, CompletableFuture<GibddServiceResponse<FederalNotaryInfo>> future) {
        GibddServiceResponse<FederalNotaryInfo> notaryInfoResult = future.join();
        result.setNotaryInfo(notaryInfoResult.getData());
        result.setNotaryServiceCallResult(notaryInfoResult.getExternalServiceCallResult());
    }

    /**
     * Расширение для 1.4+
     * Выполняется последовательный запрос: getVehicleInfo с определением vin, затем getFederalNotaryInfo с этим vin
     */
    private CarInfoComponentDto getCarInfo(String orderId, String typeId, String govRegNumber, String sts, String tx) {
        var person = userPersonalData.getPerson();
        VehicleInfoRequest vehicleInfoRequest = buildVehicleInfoRequest(person, typeId, null, govRegNumber, sts, tx);
        CarInfoComponentDto dtoResult = new CarInfoComponentDto();
        try {
            VehicleInfo vehicleInfo = gibddDataService.getVehicleInfo(vehicleInfoRequest);
            if (vehicleInfo == null) {
                setNotFoundError(dtoResult, govRegNumber, sts);
                return dtoResult;
            }

            dtoResult.setVehicleInfo(vehicleInfo);
            String vin = vehicleInfo.getVin();

            if (!StringUtils.isEmpty(vin)) {
                dtoResult.setVin(vin);
                var federalNotaryRequest = buildFederalNotaryRequest(orderId, vin, tx);
                try {
                    var federalNotaryInfo = gibddDataService.getFederalNotaryInfo(federalNotaryRequest);
                    dtoResult.setNotaryInfo(federalNotaryInfo);
                } catch (NsiExternalException e) {
                    log.error("Не удалось получить данные из сервиса Федеральной нотариальной палаты", e);
                    dtoResult.setNotaryServiceCallResult(ExternalServiceCallResult.EXTERNAL_SERVER_ERROR);
                    return dtoResult;
                }
            } else {
                setNotFoundError(dtoResult, govRegNumber, sts);
            }
            return dtoResult;

        } catch (NsiExternalException e) {
            log.error("Не удалось получить данные из сервиса", e);
            dtoResult.setVehicleServiceCallResult(ExternalServiceCallResult.EXTERNAL_SERVER_ERROR);
            dtoResult.setNotaryServiceCallResult(ExternalServiceCallResult.NOT_FOUND_ERROR);
            return dtoResult;
        }
    }

    private void setNotFoundError(CarInfoComponentDto dtoResult, String govRegNumber, String sts) {
        dtoResult.setVehicleServiceCallResult(ExternalServiceCallResult.NOT_FOUND_ERROR);
        dtoResult.setNotaryServiceCallResult(ExternalServiceCallResult.NOT_FOUND_ERROR);
        log.error("Не определен VIN для ГРЗ=" + govRegNumber + " и СТС=" + sts);
    }

}
