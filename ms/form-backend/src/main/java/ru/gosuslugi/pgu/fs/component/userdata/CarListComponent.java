package ru.gosuslugi.pgu.fs.component.userdata;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;
import ru.atc.carcass.security.rest.model.person.Person;
import ru.atc.carcass.security.rest.model.person.PersonDoc;
import ru.gosuslugi.pgu.common.core.exception.ExternalServiceException;
import ru.gosuslugi.pgu.fs.common.exception.FormBaseException;
import ru.gosuslugi.pgu.common.core.json.JsonProcessingUtil;
import ru.gosuslugi.pgu.common.esia.search.dto.UserPersonalData;
import ru.gosuslugi.pgu.dto.ApplicantAnswer;
import ru.gosuslugi.pgu.dto.ScenarioDto;
import ru.gosuslugi.pgu.dto.descriptor.FieldComponent;
import ru.gosuslugi.pgu.dto.descriptor.types.ComponentType;
import ru.gosuslugi.pgu.fs.common.component.AbstractComponent;
import ru.gosuslugi.pgu.fs.common.component.ComponentResponse;
import ru.gosuslugi.pgu.fs.component.userdata.model.CarInfoComponentDto;
import ru.gosuslugi.pgu.fs.component.userdata.model.CarListComponentDto;
import ru.gosuslugi.pgu.pgu_common.gibdd.dto.ExternalServiceCallResult;
import ru.gosuslugi.pgu.pgu_common.gibdd.dto.FederalNotaryInfo;
import ru.gosuslugi.pgu.pgu_common.gibdd.dto.FederalNotaryRequest;
import ru.gosuslugi.pgu.pgu_common.gibdd.dto.OwnerDocumentType;
import ru.gosuslugi.pgu.pgu_common.gibdd.dto.OwnerVehiclesRequest;
import ru.gosuslugi.pgu.pgu_common.gibdd.dto.VehicleFullInfo;
import ru.gosuslugi.pgu.pgu_common.gibdd.service.GibddDataService;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static ru.gosuslugi.pgu.components.ComponentAttributes.FID_DOC_ATTR;
import static ru.gosuslugi.pgu.components.ComponentAttributes.RF_PASSPORT_ATTR;
import static ru.gosuslugi.pgu.components.ComponentAttributes.VERIFIED_ATTR;

@Slf4j
@Component
@RequiredArgsConstructor
public class CarListComponent extends AbstractComponent<CarListComponentDto> {

    public static final String TX_ATTR = "tx";
    public static final String PERSON_RF_USER_TYPE = "PERSON_RF";
    public static final String RETRY_VALUE = "0";

    private final GibddDataService gibddDataService;
    private final UserPersonalData userPersonalData;

    @Override
    public ComponentResponse<CarListComponentDto> getInitialValue(FieldComponent component, ScenarioDto scenarioDto) {
        String tx = String.valueOf(component.getAttrs().get(TX_ATTR));
        return ComponentResponse.of(getCarList(tx));
    }

    @Override
    public ComponentType getType() {
        return ComponentType.CarList;
    }

    @Override
    public void validateAfterSubmit(Map<String, String> incorrectAnswers, Map.Entry<String, ApplicantAnswer> entry, ScenarioDto scenarioDto, FieldComponent component) {
        ApplicantAnswer answer = entry.getValue();
        if (RETRY_VALUE.equals(answer.getValue())) {
            Optional<FieldComponent> fieldBox = scenarioDto.getDisplay().getComponents().stream().filter(el -> el.getId().equals(entry.getKey())).findAny();
            if (fieldBox.isPresent()) {
                FieldComponent field = fieldBox.get();
                field.setValue(JsonProcessingUtil.toJson(getCarList(String.valueOf(component.getAttrs().get(TX_ATTR)))));
                field.setAttrs(component.getAttrs());
            }

            incorrectAnswers.put(entry.getKey(), "повторный запрос данных");
            return;
        }

        // заполняем данные из нотариальной конторы для выбранного ТС - будет использоваться на следующем шаге сценария
        VehicleFullInfo vehicleInfo = JsonProcessingUtil.fromJson(answer.getValue(), VehicleFullInfo.class);
        CarInfoComponentDto result = new CarInfoComponentDto();
        result.setVehicleInfo(vehicleInfo);
        result.setApproveDate(LocalDate.now().toString());
        String vin = result.getVehicleInfo().getVin();
        if (vin != null) {
            FederalNotaryRequest federalNotaryRequest = FederalNotaryRequest
                    .builder()
                    .orderId(scenarioDto.getOrderId().toString())
                    .vin(vin)
                    .tx(String.valueOf(component.getAttrs().get(TX_ATTR)))
                    .build();
            try {
                FederalNotaryInfo notaryInfo = gibddDataService.getFederalNotaryInfo(federalNotaryRequest);
                result.setNotaryInfo(notaryInfo);
            } catch (ExternalServiceException | RestClientException e) {
                log.error("Не удалось получить данные из сервиса Федеральной нотариальной палаты", e);
                result.setNotaryServiceCallResult(ExternalServiceCallResult.EXTERNAL_SERVER_ERROR);
            }
        } else {
            log.error("ShowcaseOwner вернул vin = null");
            result.setNotaryServiceCallResult(ExternalServiceCallResult.EXTERNAL_SERVER_ERROR);
        }

        answer.setValue(JsonProcessingUtil.toJson(result));
    }

    private CarListComponentDto getCarList(String tx) {
        Person person = userPersonalData.getPerson();
        PersonDoc doc;
        Optional<PersonDoc> passportOptional = userPersonalData.getDocs()
            .stream()
            .filter(it -> (it.getType().equals(RF_PASSPORT_ATTR) || it.getType().equals(FID_DOC_ATTR) && it.getVrfStu().equals(VERIFIED_ATTR)))
            .max(Comparator.comparing(PersonDoc::getType));

        if (passportOptional.isEmpty()) {
            throw new FormBaseException("У пользователя не найден верифицированный паспорт");
        }
        doc = passportOptional.get();

        OwnerVehiclesRequest request = OwnerVehiclesRequest
            .builder()
            .lastName(person.getLastName())
            .firstName(person.getFirstName())
            .birthDay(person.getBirthDate())
            .userType(PERSON_RF_USER_TYPE)
            .documentType(doc.getType().equals(RF_PASSPORT_ATTR) ? OwnerDocumentType.PASSPORT_RF : OwnerDocumentType.FID_DOC)
            .documentNumSer(doc.getSeries() + doc.getNumber())
            .tx(tx)
            .build();

        CarListComponentDto result = new CarListComponentDto();
        try {
            List<VehicleFullInfo> vehicles = gibddDataService.getOwnerVehiclesInfo(request);
            result.setVehicles(vehicles);
            if (vehicles.isEmpty()) {
                result.setVehicleServiceCallResult(ExternalServiceCallResult.NOT_FOUND_ERROR);
            }
        } catch (ExternalServiceException | RestClientException e) {
            log.error("Не удалось получить данные о ТС владельца", e);
            result.setVehicleServiceCallResult(ExternalServiceCallResult.EXTERNAL_SERVER_ERROR);
        }

        return result;
    }
}
