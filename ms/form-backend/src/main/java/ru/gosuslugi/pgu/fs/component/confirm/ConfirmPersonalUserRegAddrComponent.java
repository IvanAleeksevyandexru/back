package ru.gosuslugi.pgu.fs.component.confirm;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import ru.atc.carcass.security.rest.model.EsiaAddress;
import ru.gosuslugi.pgu.common.core.json.JsonProcessingUtil;
import ru.gosuslugi.pgu.common.esia.search.dto.UserPersonalData;
import ru.gosuslugi.pgu.components.BasicComponentUtil;
import ru.gosuslugi.pgu.components.dto.AddressType;
import ru.gosuslugi.pgu.dto.ApplicantAnswer;
import ru.gosuslugi.pgu.dto.descriptor.FieldComponent;
import ru.gosuslugi.pgu.dto.descriptor.types.ComponentType;
import ru.gosuslugi.pgu.fs.common.component.ComponentResponse;
import ru.gosuslugi.pgu.fs.component.address.AbstractFullAddressComponent;
import ru.gosuslugi.pgu.fs.component.confirm.model.ConfirmPersonalUserRegAddress;
import ru.gosuslugi.pgu.fs.esia.EsiaRestContactDataClient;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

import static java.util.Objects.isNull;
import static ru.gosuslugi.pgu.components.ComponentAttributes.FIAS_ATTR;
import static ru.gosuslugi.pgu.components.ComponentAttributes.REG_ADDR_ATTR;
import static ru.gosuslugi.pgu.components.ComponentAttributes.REG_ADDR_ZIP_CODE_ATTR;
import static ru.gosuslugi.pgu.components.ComponentAttributes.REG_DATE_ATTR;

/**
 * Компонент для отображения и валидации адреса регистрации пользователя
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ConfirmPersonalUserRegAddrComponent extends AbstractFullAddressComponent<ConfirmPersonalUserRegAddress> {

    /** по-умолчанию обновляем Регистрационный адрес <a href="https://jira.egovdev.ru/browse/EPGUCORE-51164">EPGUCORE-51164</a> */
    public static final AddressType DEFAULT_ADDRESS_TYPE = AddressType.permanentRegistry;

    private final UserPersonalData userPersonalData;
    private final EsiaRestContactDataClient esiaRestContactDataClient;

    @Override
    public ComponentType getType() {
        return ComponentType.ConfirmPersonalUserRegAddr;
    }

    @Override
    public ComponentResponse<ConfirmPersonalUserRegAddress> getInitialValue(FieldComponent component) {
        Optional<EsiaAddress> esiaAddressOptional = Optional.empty();
        AddressType addressType = BasicComponentUtil.getAddrType(component);

        // Если указан тип не являющийся значением по умолчанию, то делается попытка использовать этот адрес в данном компоненте
        if (!isNull(addressType) && !DEFAULT_ADDRESS_TYPE.equals(addressType)) {
            esiaAddressOptional = userPersonalData.getAddresses().stream()
                    .filter(a -> a.getType().equals(addressType.getEsiaAddressType().getCode()))
                    .findFirst();
        }

        // Если адрес остался пустой, то вычитываем по ключю по умолчанию
        if (esiaAddressOptional.isEmpty()) {
            esiaAddressOptional = userPersonalData.getAddresses().stream()
                    .filter(a -> a.getType().equals(DEFAULT_ADDRESS_TYPE.getEsiaAddressType().getCode()))
                    .findFirst();
        }

        Set<String> fields = BasicComponentUtil.getPreSetFields(component);

        if (esiaAddressOptional.isPresent()) {
            ConfirmPersonalUserRegAddress result = new ConfirmPersonalUserRegAddress();
            EsiaAddress esiaAddress = esiaAddressOptional.get();
            if (fields.contains(REG_ADDR_ATTR)) {
                result.setRegAddr(esiaAddress.getAddressStr());
            }

            if (fields.contains(REG_ADDR_ZIP_CODE_ATTR)) {
                result.setRegZipCode(esiaAddress.getZipCode());
            }

            if (fields.contains(FIAS_ATTR)) {
                result.setFias(esiaAddress.getFiasCode());
            }

            if (fields.contains(REG_DATE_ATTR)) {
                //TODO is missing in EsiaAddress object
            }

            return ComponentResponse.of(result);
        }

        return ComponentResponse.empty();
    }

    @Override
    protected void validateAfterSubmit(Map<String, String> incorrectAnswers, Map.Entry<String, ApplicantAnswer> entry, FieldComponent fieldComponent) {
        super.validateAfterSubmit(incorrectAnswers, entry, fieldComponent);

        if (entry.getValue() != null && entry.getValue().getValue() != null) {
            Map map = JsonProcessingUtil.fromJson(entry.getValue().getValue(), Map.class);
            Set<String> fields = BasicComponentUtil.getPreSetFields(fieldComponent);

            if (CollectionUtils.isEmpty(map) || map.get(REG_ADDR_ATTR) == null) {
                incorrectAnswers.put(entry.getKey(), "Адрес не задан");
                return;
            }

            if(requiredAddressParamsIsBlank(fieldComponent, (Map) map.get(REG_ADDR_ATTR), incorrectAnswers)) return;

            if (fields.contains(REG_DATE_ATTR)) {
                if (map.get(REG_DATE_ATTR) == null) {
                    incorrectAnswers.put(entry.getKey(), "Дата не заполнена");
                    return;
                }
                try {
                    LocalDate.parse((String) map.get(REG_DATE_ATTR), DateTimeFormatter.ofPattern("dd.MM.yyyy"));
                } catch (DateTimeParseException e) {
                    incorrectAnswers.put(entry.getKey(), "Некорректная дата");
                }
            }
        }
    }

    private boolean requiredAddressParamsIsBlank(FieldComponent fieldComponent, Map regAddrValue, Map<String, String> incorrectAnswers) {
        if (fieldComponent.getAttrs().containsKey("requiredAddressParams")) {
            LinkedHashMap<String, Objects> requiredParams = (LinkedHashMap<String, Objects>) fieldComponent.getAttrs().get("requiredAddressParams");
            for (var param: requiredParams.entrySet()) {
                String stringParamValue = String.valueOf(regAddrValue.getOrDefault(param.getKey(),""));
                if (StringUtils.isEmpty(stringParamValue)) {
                    incorrectAnswers.put(fieldComponent.getId(), "Не определен обязательный атрибут: " + param.getValue());
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public String getFullAddressJsonPath() {
        // regAddr after root
        return "$['regAddr']";
    }
}
