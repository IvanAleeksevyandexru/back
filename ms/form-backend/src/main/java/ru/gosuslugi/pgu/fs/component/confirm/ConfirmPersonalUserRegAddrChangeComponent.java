package ru.gosuslugi.pgu.fs.component.confirm;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.atc.carcass.security.rest.model.EsiaAddress;
import ru.gosuslugi.pgu.common.esia.search.dto.UserPersonalData;
import ru.gosuslugi.pgu.components.BasicComponentUtil;
import ru.gosuslugi.pgu.components.descriptor.types.FullAddress;
import ru.gosuslugi.pgu.components.dto.AddressType;
import ru.gosuslugi.pgu.dto.ApplicantAnswer;
import ru.gosuslugi.pgu.dto.descriptor.FieldComponent;
import ru.gosuslugi.pgu.dto.descriptor.types.ComponentType;
import ru.gosuslugi.pgu.fs.common.component.ComponentResponse;
import ru.gosuslugi.pgu.fs.component.address.AbstractFullAddressComponent;
import ru.gosuslugi.pgu.fs.esia.EsiaRestContactDataClient;
import ru.gosuslugi.pgu.fs.utils.EsiaAddressUtil;

import java.util.Map;
import java.util.Optional;

/**
 * Компонент для отображения и валидации адреса регистрации пользователя
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ConfirmPersonalUserRegAddrChangeComponent extends AbstractFullAddressComponent<String> {

    /** по-умолчанию обновляем Регистрационный адрес <a href="https://jira.egovdev.ru/browse/EPGUCORE-51164">EPGUCORE-51164</a> */
    public static final AddressType DEFAULT_ADDRESS_TYPE = AddressType.permanentRegistry;

    private final UserPersonalData userPersonalData;
    private final EsiaRestContactDataClient esiaRestContactDataClient;

    @Override
    public ComponentType getType() {
        return ComponentType.ConfirmPersonalUserRegAddrChange;
    }

    @Override
    public ComponentResponse<String> getInitialValue(FieldComponent component) {
        AddressType addressType = Optional.ofNullable(BasicComponentUtil.getAddrType(component)).orElse(DEFAULT_ADDRESS_TYPE);
        Optional<EsiaAddress> esiaAddressOptional = getEsiaAddress(addressType);

        if (esiaAddressOptional.isPresent()) {
            return ComponentResponse.of(esiaAddressOptional.get().getAddressStr());
        }
        return ComponentResponse.empty();
    }

    private Optional<EsiaAddress> getEsiaAddress(AddressType addressType) {
        return userPersonalData.getAddresses().stream()
            .filter(a -> a.getType().equals(addressType.getEsiaAddressType().getCode()))
            .findFirst();
    }

    @Override
    protected void validateAfterSubmit(Map<String, String> incorrectAnswers, Map.Entry<String, ApplicantAnswer> entry, FieldComponent fieldComponent) {
        super.validateAfterSubmit(incorrectAnswers, entry, fieldComponent);

        // Обновление адреса в ESIA как часть общей валидации
        if (incorrectAnswers.isEmpty()) {
            try {
                updateEsiaAddress(entry, fieldComponent);
            } catch (Exception e) {
                if (log.isWarnEnabled()) {
                    log.warn("Не удалось обновить адрес в ESIA", e);
                }
                incorrectAnswers.put(fieldComponent.getId(), "Не удалось обновить адрес. Попробуйте позже.");
            }
        }
    }

    @Override
    public String getFullAddressJsonPath() {
        return "$";
    }

    /**
     * Добавление/изменение адреса пользователя в ЛК
     * @param entry ответ пользователя для данного компонента
     * @param component компонент
     */
    private void updateEsiaAddress(Map.Entry<String, ApplicantAnswer> entry, FieldComponent component) {

        AddressType addressType = Optional.ofNullable(BasicComponentUtil.getAddrType(component)).orElse(DEFAULT_ADDRESS_TYPE);
        FullAddress fullAddress = getFullAddress(entry);
        esiaRestContactDataClient.updateAddress(EsiaAddressUtil.get(addressType.getEsiaAddressType(), fullAddress));
    }
}