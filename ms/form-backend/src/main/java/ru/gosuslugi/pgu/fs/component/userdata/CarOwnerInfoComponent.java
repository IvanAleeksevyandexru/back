package ru.gosuslugi.pgu.fs.component.userdata;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.logging.log4j.util.Strings;
import org.springframework.stereotype.Component;
import ru.gosuslugi.pgu.common.core.json.JsonProcessingUtil;
import ru.gosuslugi.pgu.components.FieldComponentUtil;
import ru.gosuslugi.pgu.dto.ApplicantAnswer;
import ru.gosuslugi.pgu.dto.ScenarioDto;
import ru.gosuslugi.pgu.dto.descriptor.FieldComponent;
import ru.gosuslugi.pgu.dto.descriptor.types.ComponentType;
import ru.gosuslugi.pgu.fs.common.component.AbstractComponent;
import ru.gosuslugi.pgu.fs.common.component.ComponentResponse;
import ru.gosuslugi.pgu.fs.component.userdata.model.CarOwnerInfoComponentDto;
import ru.gosuslugi.pgu.fs.component.userdata.model.OwnerDocumentDto;
import ru.gosuslugi.pgu.fs.component.userdata.model.OwnerDto;
import ru.gosuslugi.pgu.pgu_common.gibdd.dto.Owner;

import java.time.DateTimeException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static ru.gosuslugi.pgu.common.core.date.util.DateUtil.ESIA_DATE_FORMAT;
import static ru.gosuslugi.pgu.components.ComponentAttributes.PDF_LINK_ATTR;

@Slf4j
@Component
@RequiredArgsConstructor
public class CarOwnerInfoComponent extends AbstractComponent<CarOwnerInfoComponentDto> {

    private static final String CAR_LIST_REF_ATTR = "carListRef";
    private static final String ACTIONS_ATTR = "actions";
    private static final String CAN_BE_SOLD_ATTR = "canBeSold";
    private static final String CANNOT_BE_SOLD_ATTR = "cannotBeSold";
    private static final String SHOW_IS_ATTR = "showIs";

    @Override
    public ComponentResponse<CarOwnerInfoComponentDto> getInitialValue(FieldComponent component, ScenarioDto scenarioDto) {
        // Получаем данные полученные из компонета CarList
        String carListValue  = component.getAttrs().get(CAR_LIST_REF_ATTR).toString();
        ApplicantAnswer answer = scenarioDto.getApplicantAnswers().get(carListValue);
        if (answer == null) {
            answer = scenarioDto.getCurrentValue().get(carListValue);
        }

        CarOwnerInfoComponentDto result = JsonProcessingUtil.fromJson(answer.getValue(), CarOwnerInfoComponentDto.class);
        if (result.getVehicleInfo() != null && result.getVehicleInfo().getOwner() != null) {
            Owner owner = result.getVehicleInfo().getOwner();
            String date = "";
            if (Strings.isNotBlank(owner.getDocumentDate())) {
                try {
                    date = LocalDate.parse(owner.getDocumentDate(), DateTimeFormatter.ISO_DATE).format(DateTimeFormatter.ofPattern(ESIA_DATE_FORMAT));
                } catch (DateTimeException e) {
                    log.error("Дата выдачи документа ({}) имеет неверный формат", owner.getDocumentDate(), e);
                }
            }
            OwnerDocumentDto ownerDocumentInfo = new OwnerDocumentDto(owner.getDocumentNumSer(), date, owner.getIdDocumentType());
            String fullName = Stream.of(owner.getLastName(), owner.getFirstName(), owner.getMiddleName())
                    .filter(Strings::isNotBlank)
                    .collect(Collectors.joining(" "));
            OwnerDto ownerInfo = new OwnerDto(fullName, ownerDocumentInfo);
            result.setOwnerInfo(ownerInfo);
        }

        // в зависимости от ограничений показываем разный блок кнопок
        boolean canBeSold = true;
        // ТС нельзя продавать если на него есть ограгичения, оно находится в розыске или в залоге
        if (result.getVehicleInfo() == null || result.getVehicleInfo().getRestrictionsFlag() ||
                result.getVehicleInfo().getSearchingTransportFlag() ||
                result.getNotaryInfo() == null || result.getNotaryInfo().getIsPledged()) {
            canBeSold = false;
        }
        List<Map<Object, Object>> actions = FieldComponentUtil.getList(component, ACTIONS_ATTR, true);
        component.getAttrs().put(ACTIONS_ATTR, canBeSold ?
                actions.stream().filter(action -> action.get(SHOW_IS_ATTR).equals(CAN_BE_SOLD_ATTR)).collect(Collectors.toList()) :
                actions.stream().filter(action -> action.get(SHOW_IS_ATTR).equals(CANNOT_BE_SOLD_ATTR)).collect(Collectors.toList()));

        // задаем ссылку на скачивание
        if (component.getAttrs().containsKey(PDF_LINK_ATTR)) {
            component.getAttrs().put(PDF_LINK_ATTR, component.getAttrs().get(PDF_LINK_ATTR).toString().replace("${orderId}", scenarioDto.getOrderId().toString()));
        }

        return ComponentResponse.of(result);
    }

    @Override
    public ComponentType getType() {
        return ComponentType.CarOwnerInfo;
    }
}
