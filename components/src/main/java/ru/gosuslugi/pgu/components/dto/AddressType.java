package ru.gosuslugi.pgu.components.dto;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Arrays;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Адресный тип, который используется в сценарии
 */
@Getter
@RequiredArgsConstructor
public enum AddressType {

    actualResidence("Адрес фактического проживания", "PLV"),
    permanentRegistry("Адрес постоянной регистрации", "PRG"),
    legalAddress("Юридический адрес организации", "OLG"),
    factAddress("Фактический адрес организации", "OPS"),
    tempAddress("Адрес временной регистрации", "PTA");

    private static final Map<String, AddressType> addressTypeMap;

    private final String description;
    private final String esiaAddressType;

    static {
        addressTypeMap = Arrays.stream(AddressType.values())
                .collect(Collectors.toUnmodifiableMap(AddressType::name, Function.identity()));
    }

    /**
     * Безопасное преобразование строки в AddressType
     *
     * @param addressType строка
     * @return AddressType объект или null
     */
    public static AddressType fromString(String addressType) {
        return Optional.ofNullable(addressType)
                .map(addressTypeMap::get)
                .orElse(null);
    }
}
