package ru.gosuslugi.pgu.fs.utils;

import ru.atc.carcass.security.rest.model.EsiaAddress;
import ru.gosuslugi.pgu.components.descriptor.types.FullAddress;

/**
 * Утилитарный класс связанный с EsiaAddress классом
 */
public class EsiaAddressUtil {
    public static final String DEFAULT_COUNTRY_ID = "RUS";

    /**
     * Private constructor
     */
    private EsiaAddressUtil() {
    }

    /**
     * Конвертирует FullAddress объект в EsiaAddress объект
     * @param type ESIA тип адреса
     * @param fullAddress fullAddress объект
     * @return EsiaAddress объект
     */
    public static EsiaAddress get(EsiaAddress.Type type, FullAddress fullAddress) {
        EsiaAddress result = new EsiaAddress();
        result.setType(type.getCode());
        result.setAddressStr(fullAddress.getAddressStr());
        result.setCountryId(DEFAULT_COUNTRY_ID);
        result.setFiasCode(fullAddress.getFiasCode());
        // к. 2, стр. 14, => "frame": "2", "building": "14",
        result.setFrame(fullAddress.getBuilding1());
        result.setBuilding(fullAddress.getBuilding2());
        result.setHouse(fullAddress.getHouse());
        result.setRegion(fullAddress.getRegion());
        result.setStreet(fullAddress.getStreet());
        result.setZipCode(fullAddress.getIndex());
        result.setFlat(fullAddress.getApartment());
        result.setCity(fullAddress.getCity());
        // с. Бутка => "settlement": "Бутка",
        result.setSettlement(fullAddress.getTown());
        // р-н. Талицкий => "area": "Талицкий",
        result.setArea(fullAddress.getDistrict());
        result.setDistrict(fullAddress.getInCityDist());
        result.setAdditionArea(fullAddress.getAdditionalArea());
        result.setAdditionAreaStreet(fullAddress.getAdditionalStreet());
        return result;
    }
}
