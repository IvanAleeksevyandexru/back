package ru.gosuslugi.pgu.components.dto;

import ru.atc.carcass.security.rest.model.EsiaAddress;

/**
 * Адресный тип, который используется в сценарии
 */
public enum AddressType {

    /** Actual */
    actualResidence("Актуальный", EsiaAddress.Type.LOCATION),

    /** REGISTRATION */
    permanentRegistry("Регистрационный", EsiaAddress.Type.REGISTRATION);

    private final String description;
    private final EsiaAddress.Type esiaAddressType;

    AddressType(String description, EsiaAddress.Type esiaAddressType) {
        this.description = description;
        this.esiaAddressType = esiaAddressType;
    }

    public String getDescription() {
        return description;
    }

    public EsiaAddress.Type getEsiaAddressType() {
        return esiaAddressType;
    }

    /**
     * Безопасное преобразование строки в AddressType
     * @param string строка
     * @return AddressType объект или null
     */
    public static AddressType formString(String string) {
        for (AddressType candidate : values()) {
            if (candidate.name().equals(string)) {
                return candidate;
            }
        }
        return null;
    }
}
