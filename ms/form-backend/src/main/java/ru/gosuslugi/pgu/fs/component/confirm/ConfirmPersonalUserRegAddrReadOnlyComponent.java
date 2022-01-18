package ru.gosuslugi.pgu.fs.component.confirm;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.atc.carcass.security.rest.model.EsiaAddress;
import ru.gosuslugi.pgu.common.core.json.JsonProcessingUtil;
import ru.gosuslugi.pgu.common.esia.search.dto.UserPersonalData;
import ru.gosuslugi.pgu.components.BasicComponentUtil;
import ru.gosuslugi.pgu.components.descriptor.types.AddressHideLevels;
import ru.gosuslugi.pgu.components.descriptor.types.FullAddress;
import ru.gosuslugi.pgu.components.dto.AddressType;
import ru.gosuslugi.pgu.dto.ApplicantAnswer;
import ru.gosuslugi.pgu.dto.ScenarioDto;
import ru.gosuslugi.pgu.dto.descriptor.FieldComponent;
import ru.gosuslugi.pgu.dto.descriptor.FieldComponentDisclaimer;
import ru.gosuslugi.pgu.dto.descriptor.types.ComponentType;
import ru.gosuslugi.pgu.fs.common.component.AbstractComponent;
import ru.gosuslugi.pgu.fs.common.component.ComponentResponse;
import ru.gosuslugi.pgu.fs.component.confirm.mapper.FullAddressMapper;
import ru.gosuslugi.pgu.fs.component.confirm.model.ConfirmPersonalUserRegAddressReadOnly;
import ru.gosuslugi.pgu.fs.esia.EsiaRestContactDataClient;
import ru.gosuslugi.pgu.fs.service.LkNotifierService;
import ru.gosuslugi.pgu.fs.utils.FullAddressEnrichUtil;
import ru.gosuslugi.pgu.fs.utils.FullAddressFiasUtil;
import ru.gosuslugi.pgu.pgu_common.nsi.dto.DadataAddressResponse;
import ru.gosuslugi.pgu.pgu_common.nsi.service.NsiDadataService;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.*;
import java.util.stream.Collectors;

import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;
import static org.apache.commons.lang.StringUtils.isBlank;
import static ru.gosuslugi.pgu.components.ComponentAttributes.*;

/**
 * Компонент для отображения и валидации адреса регистрации пользователя
 * Переработан в Read-Only модель - нет валидации
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ConfirmPersonalUserRegAddrReadOnlyComponent extends AbstractComponent<ConfirmPersonalUserRegAddressReadOnly> {

    /** по-умолчанию обновляем Регистрационный адрес <a href="https://jira.egovdev.ru/browse/EPGUCORE-51164">EPGUCORE-51164</a> */
    public static final AddressType DEFAULT_ADDRESS_TYPE = AddressType.permanentRegistry;

    private final UserPersonalData userPersonalData;
    private final NsiDadataService nsiDadataService;
    private final EsiaRestContactDataClient esiaRestContactDataClient;
    private final LkNotifierService lkNotifierService;
    private final FullAddressMapper fullAddressMapper;

    @Override
    public ComponentType getType() {
        return ComponentType.ConfirmPersonalUserRegReadOnlyAddr;
    }

    @Override
    public ComponentResponse<ConfirmPersonalUserRegAddressReadOnly> getInitialValue(FieldComponent component) {
        Optional<EsiaAddress> esiaAddressOptional = getEsiaAddress(component);

        //если в json-е не задан addrType, то по дефолту отображать адрес постоянной регистрации
        if(!component.getAttrs().containsKey(ADDR_TYPE)) {
            esiaAddressOptional=userPersonalData.getAddresses().stream()
                    .filter(a->a.getType().equals(DEFAULT_ADDRESS_TYPE.getEsiaAddressType().getCode()))
                    .findFirst();
        }

        if (esiaAddressOptional.isPresent()) {
            EsiaAddress esiaAddress= esiaAddressOptional.get();
            String address = esiaAddress.getZipCode() + ", " + esiaAddress.getAddressStr();

            DadataAddressResponse addressResponse = nsiDadataService.getAddress(address);
            if (addressResponse == null || addressResponse.getError() == null || addressResponse.getError().getCode() != 0) {
                if (log.isInfoEnabled()) {
                    if (addressResponse == null || addressResponse.getError() == null ) {
                        log.info("Не удалось получить проверить адрес в Dadata сервисе для адреса \"{}\": нет ответа", address);
                    } else {
                        log.info(
                            "Не удалось получить проверить адрес в Dadata сервисе для адреса \"{}\": код = {}, сообщение = \"{}\"",
                            address,
                            addressResponse.getError().getCode(),
                            addressResponse.getError().getMessage()
                        );
                    }
                }
                return getErrorValue("Не удалось получить проверить адрес в Dadata сервисе");
            } else {
                String error = validateInitialAddress(addressResponse, component);
                if (nonNull(error)) {
                    return getErrorValue(error);
                }
            }
            return getValue(component, addressResponse);
        }
        return getErrorValue("");
    }

    @Override
    protected void preValidate(ComponentResponse<ConfirmPersonalUserRegAddressReadOnly> initialValue, FieldComponent component, ScenarioDto scenarioDto) {
        ConfirmPersonalUserRegAddressReadOnly address = initialValue.get();
        component.getAttrs().remove("disclaimer");
        if(address.getRegAddr()==null){
            FieldComponentDisclaimer disclaimer = createDisclaimer(BasicComponentUtil.getAddrType(component));
            component.getAttrs().put("disclaimer",disclaimer);
            scenarioDto.getErrors().put(component.getId(), address.getError());
        }
        if (nonNull(address) && !isBlank(address.getError())) {
            scenarioDto.getErrors().put(component.getId(), address.getError());
        }
    }

    private FieldComponentDisclaimer createDisclaimer(AddressType addrType){
        String str = (AddressType.actualResidence == addrType)? "Адрес фактического проживания":"Адрес постоянной регистрации";
        FieldComponentDisclaimer disclaimer = new FieldComponentDisclaimer(
                WARN_ATTR,
                "Добавьте адрес",
                str + " нужен для отправки заявления. " +
                        "Этот адрес сохранится в профиле, и в будущих заявлениях не придется вводить его заново");
       return disclaimer;
    }

    private ComponentResponse<ConfirmPersonalUserRegAddressReadOnly> getValue(FieldComponent component, DadataAddressResponse addressResponse) {
        FullAddress fullAddress = FullAddressFiasUtil.addMetaInfoWithOptionalGeoPoints(addressResponse, fullAddressMapper);
        if (isNull(fullAddress)) {
            return getErrorValue("Полнота адреса недостаточна");
        }
        FullAddressEnrichUtil.setAddressParts(fullAddress, addressResponse);
        ConfirmPersonalUserRegAddressReadOnly result = new ConfirmPersonalUserRegAddressReadOnly();
        result.setRegAddr(fullAddress);
        return ComponentResponse.of(result);
    }

    private ComponentResponse<ConfirmPersonalUserRegAddressReadOnly> getErrorValue(String error) {
        ConfirmPersonalUserRegAddressReadOnly result = new ConfirmPersonalUserRegAddressReadOnly();
        result.setError(error);
        return ComponentResponse.of(result);
    }

    private Optional<EsiaAddress> getEsiaAddress(FieldComponent component) {
        Optional<EsiaAddress> esiaAddressOptional = Optional.empty();
        AddressType addressType = BasicComponentUtil.getAddrType(component);
        if (!isNull(addressType)) {
            esiaAddressOptional = userPersonalData.getAddresses().stream()
                .filter(a -> a.getType().equals(addressType.getEsiaAddressType().getCode()))
                .findFirst();
        }
        return esiaAddressOptional;
    }

    public static String validateInitialAddress(DadataAddressResponse value, FieldComponent fieldComponent) {
        if (
            value.getDadataQc() != 0
            && value.getDadataQc() != 3
        ) {
            return "Адрес не распознан";
        }
        List<AddressHideLevels> addressHideLevels = Optional.ofNullable(getAddressHideLevels(fieldComponent))
            .orElse(
                Arrays.asList(
                    AddressHideLevels.street, AddressHideLevels.additionalStreet,
                    AddressHideLevels.house, AddressHideLevels.building1, AddressHideLevels.building2,
                    AddressHideLevels.apartment
                )
            );

        switch (value.getDadataQcComplete()) {
            case 0:
            case 8:
            case 9:
            case 10:
                return null;
            case 1:
                return addressHideLevels.contains(AddressHideLevels.region) ? null : "В адресе не указан регион";
            case 2:
                return addressHideLevels.contains(AddressHideLevels.town) && addressHideLevels.contains(AddressHideLevels.city) ? null : "В адресе не указан город";
            case 3:
                return addressHideLevels.contains(AddressHideLevels.street) && addressHideLevels.contains(AddressHideLevels.additionalStreet) ? null : "В адресе не указана улица";
            case 4:
                return (addressHideLevels.contains(AddressHideLevels.house) && addressHideLevels.contains(AddressHideLevels.building1) && addressHideLevels.contains(AddressHideLevels.building2)) ? null : "В адресе не указан дом" ;
            case 5:
                return addressHideLevels.contains(AddressHideLevels.apartment) ? null : "В адресе не указана квартиры";
            default:
                return "Неподходящий адрес";
        }
   }

    private static List<AddressHideLevels> getAddressHideLevels(FieldComponent fieldComponent) {
        if (fieldComponent.getAttrs() == null || !fieldComponent.getAttrs().containsKey("hideLevels")) {
            return null;
        }
        List<String> res = (List<String>) fieldComponent.getAttrs().get("hideLevels");
        return res.stream().map(AddressHideLevels::valueOf).collect(Collectors.toList());
    }

    @Override
    protected void validateAfterSubmit(Map<String, String> incorrectAnswers, Map.Entry<String, ApplicantAnswer> entry, FieldComponent fieldComponent) {
        ConfirmPersonalUserRegAddressReadOnly initialValue = getInitialValue(fieldComponent).get();
        if (nonNull(initialValue) && !isBlank(initialValue.getError())) {
            incorrectAnswers.put(entry.getKey(), initialValue.getError());
            return;
        }

        if (entry.getValue() != null && entry.getValue().getValue() != null) {
            Set<String> fields = BasicComponentUtil.getPreSetFields(fieldComponent);
            if (fields.contains(REG_DATE_ATTR)) {
                ConfirmPersonalUserRegAddressReadOnly address = JsonProcessingUtil.fromJson(entry.getValue().getValue(), ConfirmPersonalUserRegAddressReadOnly.class);
                if (isBlank(address.getRegDate())) {
                    incorrectAnswers.put(entry.getKey(), "Дата не заполнена");
                    return;
                }
                try {
                    LocalDate.parse(address.getRegDate(), DateTimeFormatter.ofPattern("dd.MM.yyyy"));
                    initialValue.setRegDate(address.getRegDate());
                } catch (DateTimeParseException e) {
                    incorrectAnswers.put(entry.getKey(), "Некорректная дата");
                    return;
                }
            }
        }
        // Set Answer
        entry.getValue().setValue(JsonProcessingUtil.toJson(initialValue));
    }

    @Override
    protected void postProcess(Map.Entry<String, ApplicantAnswer> entry, ScenarioDto scenarioDto, FieldComponent fieldComponent) {
        if(fieldComponent.isSendAnalytics()){
            ConfirmPersonalUserRegAddressReadOnly address = JsonProcessingUtil.fromJson(entry.getValue().getValue(), ConfirmPersonalUserRegAddressReadOnly.class);
            lkNotifierService.updateOrderRegion(scenarioDto.getOrderId(), address.getRegAddr().getOkato());
        }
    }
}
