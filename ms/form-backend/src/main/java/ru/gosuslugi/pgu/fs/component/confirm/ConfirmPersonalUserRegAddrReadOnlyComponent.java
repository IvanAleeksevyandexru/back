package ru.gosuslugi.pgu.fs.component.confirm;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.atc.carcass.security.rest.model.EsiaAddress;
import ru.gosuslugi.pgu.common.core.exception.ValidationException;
import ru.gosuslugi.pgu.common.core.json.JsonProcessingUtil;
import ru.gosuslugi.pgu.common.esia.search.dto.UserPersonalData;
import ru.gosuslugi.pgu.components.BasicComponentUtil;
import ru.gosuslugi.pgu.components.ValidationUtil;
import ru.gosuslugi.pgu.components.descriptor.types.ValidationFieldDto;
import ru.gosuslugi.pgu.components.dto.AddressType;
import ru.gosuslugi.pgu.components.dto.ErrorDto;
import ru.gosuslugi.pgu.dto.ApplicantAnswer;
import ru.gosuslugi.pgu.dto.ScenarioDto;
import ru.gosuslugi.pgu.dto.descriptor.FieldComponent;
import ru.gosuslugi.pgu.dto.descriptor.FieldComponentDisclaimer;
import ru.gosuslugi.pgu.dto.descriptor.types.ComponentType;
import ru.gosuslugi.pgu.fs.common.component.AbstractComponent;
import ru.gosuslugi.pgu.fs.common.component.ComponentResponse;
import ru.gosuslugi.pgu.fs.component.confirm.model.ConfirmPersonalUserRegAddressReadOnly;
import ru.gosuslugi.pgu.fs.service.FullAddressService;
import ru.gosuslugi.pgu.fs.service.LkNotifierService;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;
import static org.apache.commons.lang.StringUtils.isBlank;
import static ru.gosuslugi.pgu.components.ComponentAttributes.*;
import static ru.gosuslugi.pgu.components.FieldComponentUtil.FIELDS_KEY;

/**
 * Компонент для отображения и валидации адреса регистрации пользователя
 * Переработан в Read-Only модель - нет валидации
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ConfirmPersonalUserRegAddrReadOnlyComponent
        extends AbstractComponent<ConfirmPersonalUserRegAddressReadOnly> {

    /**
     * по-умолчанию обновляем Регистрационный адрес <a href="https://jira.egovdev.ru/browse/EPGUCORE-51164">EPGUCORE-51164</a>
     */
    public static final AddressType DEFAULT_ADDRESS_TYPE = AddressType.permanentRegistry;
    private static final String DATE_IS_NOT_FILLED = "Дата не заполнена";
    private static final String DISCLAIMER = "disclaimer";

    private final UserPersonalData userPersonalData;
    private final LkNotifierService lkNotifierService;
    private final FullAddressService fullAddressService;

    @Override
    public ComponentType getType() {
        return ComponentType.ConfirmPersonalUserRegReadOnlyAddr;
    }

    @Override
    public ComponentResponse<ConfirmPersonalUserRegAddressReadOnly> getInitialValue(FieldComponent component) {
        Optional<EsiaAddress> esiaAddressOptional = getEsiaAddress(component);

        //если в json-е не задан addrType, то по дефолту отображать адрес постоянной регистрации
        if (!component.getAttrs().containsKey(ADDR_TYPE)) {
            esiaAddressOptional = userPersonalData.getAddresses().stream()
                    .filter(a -> a.getType().equals(DEFAULT_ADDRESS_TYPE.getEsiaAddressType().getCode()))
                    .findFirst();
        }

        if (esiaAddressOptional.isEmpty()) {
            return getErrorValue("");
        }

        var esiaAddress = esiaAddressOptional.get();
        var fullAddress = fullAddressService.fromEsiaAddress(esiaAddress);

        var result = new ConfirmPersonalUserRegAddressReadOnly();
        result.setRegAddr(fullAddress);
        return ComponentResponse.of(result);

    }

    @Override
    protected void preValidate(
            ComponentResponse<ConfirmPersonalUserRegAddressReadOnly> initialValue,
            FieldComponent component,
            ScenarioDto scenarioDto
    ) {
        ConfirmPersonalUserRegAddressReadOnly address = initialValue.get();
        component.getAttrs().remove(DISCLAIMER);
        if (isNull(address.getRegAddr())) {
            FieldComponentDisclaimer disclaimer = createDisclaimer(BasicComponentUtil.getAddrType(component));
            component.getAttrs().put(DISCLAIMER, disclaimer);
            scenarioDto.getErrors().put(component.getId(), address.getError());
        }
        if (!isBlank(address.getError())) {
            scenarioDto.getErrors().put(component.getId(), address.getError());
        }
    }

    private FieldComponentDisclaimer createDisclaimer(AddressType addrType) {
        String str = AddressType.actualResidence == addrType
                ? "Адрес фактического проживания"
                : "Адрес постоянной регистрации";
        return new FieldComponentDisclaimer(
                WARN_ATTR,
                "Добавьте адрес",
                str + " нужен для отправки заявления. " +
                        "Этот адрес сохранится в профиле, и в будущих заявлениях не придется вводить его заново"
        );
    }

    private ComponentResponse<ConfirmPersonalUserRegAddressReadOnly> getErrorValue(String error) {
        ConfirmPersonalUserRegAddressReadOnly result = new ConfirmPersonalUserRegAddressReadOnly();
        result.setError(error);
        return ComponentResponse.of(result);
    }

    private Optional<EsiaAddress> getEsiaAddress(FieldComponent component) {
        return Optional.ofNullable(BasicComponentUtil.getAddrType(component))
                .flatMap(addressType -> userPersonalData.getAddresses().stream()
                        .filter(a -> a.getType().equals(addressType.getEsiaAddressType().getCode()))
                        .findFirst());
    }

    @Override
    protected void validateAfterSubmit(
        Map<String, String> incorrectAnswers,
        Map.Entry<String, ApplicantAnswer> entry,
        FieldComponent fieldComponent
    ) {
        ConfirmPersonalUserRegAddressReadOnly initialValue = getInitialValue(fieldComponent).get();
        if (nonNull(initialValue) && initialValue.getError() != null) {
            incorrectAnswers.put(entry.getKey(), initialValue.getError());
            return;
        }

        if (entry.getValue() == null || entry.getValue().getValue() == null) {
            incorrectAnswers.put(entry.getKey(), DATE_IS_NOT_FILLED);
            return;
        }

        //TODO Не заполненный индекс может приходить из профиля пользователя. После исправления (EPGUCORE-91536) эту проверку можно удалить.
        if (nonNull(initialValue) && isBlank(initialValue.getRegAddr().getPostalCode())) {
            incorrectAnswers.put(entry.getKey(), "Необходимо указать индекс");
            return;
        }

        Set<String> fields = BasicComponentUtil.getPreSetFields(fieldComponent);
        if (fields.contains(REG_DATE_ATTR)) {
            ConfirmPersonalUserRegAddressReadOnly address = JsonProcessingUtil.fromJson(
                    entry.getValue().getValue(),
                    ConfirmPersonalUserRegAddressReadOnly.class
            );
            if (isBlank(address.getRegDate())) {
                incorrectAnswers.put(entry.getKey(), DATE_IS_NOT_FILLED);
                return;
            }
            try {
                List<ValidationFieldDto> validationFieldDto = objectMapper.convertValue(
                        fieldComponent.getAttrs().get(FIELDS_KEY),
                        new TypeReference<>() {}
                );
                Map<String, ErrorDto> errors = ValidationUtil.validateFieldsByRegExp(
                        incorrectAnswers,
                        entry.getValue().getValue(),
                        validationFieldDto
                );
                if (!errors.isEmpty()) {
                    incorrectAnswers.put(entry.getKey(), jsonProcessingService.toJson(errors));
                }

                LocalDate.parse(address.getRegDate(), DateTimeFormatter.ofPattern("dd.MM.yyyy"));
                initialValue.setRegDate(address.getRegDate());
            } catch (JsonProcessingException e) {
                throw new ValidationException("Ошибка при попытке валидации адреса", e);
            } catch (DateTimeParseException e) {
                incorrectAnswers.put(entry.getKey(), "Некорректная дата");
                return;
            }
        }
        // Set Answer
        entry.getValue().setValue(JsonProcessingUtil.toJson(initialValue));
    }

    @Override
    protected void postProcess(
            Map.Entry<String, ApplicantAnswer> entry,
            ScenarioDto scenarioDto,
            FieldComponent fieldComponent
    ) {
        if (fieldComponent.isSendAnalytics()) {
            ConfirmPersonalUserRegAddressReadOnly address = JsonProcessingUtil.fromJson(
                    entry.getValue().getValue(),
                    ConfirmPersonalUserRegAddressReadOnly.class
            );
            lkNotifierService.updateOrderRegion(scenarioDto.getOrderId(), address.getRegAddr().getOkato());
        }
    }
}
