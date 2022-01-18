package ru.gosuslugi.pgu.fs.utils;

import org.mapstruct.factory.Mappers;
import ru.gosuslugi.pgu.components.descriptor.types.FullAddress;
import ru.gosuslugi.pgu.fs.service.FullAddressEnrichMapper;
import ru.gosuslugi.pgu.pgu_common.nsi.dto.DadataAddress;
import ru.gosuslugi.pgu.pgu_common.nsi.dto.DadataAddressElement;
import ru.gosuslugi.pgu.pgu_common.nsi.dto.DadataAddressResponse;

import java.util.*;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.Objects.isNull;
import static org.apache.commons.lang.StringUtils.isBlank;
import static org.apache.commons.lang.StringUtils.isNotBlank;

public class FullAddressEnrichUtil {

    private static final FullAddressEnrichMapper MAPPER = Mappers.getMapper(FullAddressEnrichMapper.class);

    private static final int REGION_LEVEL = 1;
    private static final int DISTRICT_LEVEL = 3;
    private static final int CITY_LEVEL = 4;
    private static final int IN_CITY_DIST_LEVEL = 5;
    private static final int TOWN_LEVEL = 6;
    private static final int STREET_LEVEL = 7;
    private static final int HOUSE_LEVEL = 11;
    private static final int BUILDING1_LEVEL = 12;
    private static final int BUILDING2_LEVEL = 13;
    private static final int APARTMENT_LEVEL = 14;
    private static final int ADDITIONAL_AREA_LEVEL = 90;
    private static final int ADDITIONAL_STREET_LEVEL = 91;

    private static final Map<Integer, BiConsumer<FullAddress, DadataAddressElement>> CONSUMER_MAP = new HashMap<>();
    static {
        CONSUMER_MAP.put(REGION_LEVEL, (address, element) -> {address.setRegion(element.getData()); address.setRegionType(element.getType()); address.setRegionShortType(element.getShortType());});
        CONSUMER_MAP.put(DISTRICT_LEVEL, (address, element) -> {address.setDistrict(element.getData()); address.setDistrictType(element.getType()); address.setDistrictShortType(element.getShortType());});
        CONSUMER_MAP.put(CITY_LEVEL, (address, element) -> {address.setCity(element.getData());address.setCityType(element.getType()); address.setCityShortType(element.getShortType());});
        CONSUMER_MAP.put(IN_CITY_DIST_LEVEL, (address, element) -> {address.setInCityDist(element.getData()); address.setInCityDistType(element.getType()); address.setInCityDistShortType(element.getShortType());});
        CONSUMER_MAP.put(TOWN_LEVEL, (address, element) -> {address.setTown(element.getData()); address.setTownType(element.getType()); address.setTownShortType(element.getShortType());});
        CONSUMER_MAP.put(STREET_LEVEL, (address, element) -> {address.setStreet(element.getData()); address.setStreetType(element.getType()); address.setStreetShortType(element.getShortType());});
        CONSUMER_MAP.put(HOUSE_LEVEL, (address, element) -> {address.setHouse(element.getData()); address.setHouseType(element.getType()); address.setHouseShortType(element.getShortType());});
        CONSUMER_MAP.put(BUILDING1_LEVEL, (address, element) -> {address.setBuilding1(element.getData()); address.setBuilding1Type(element.getType()); address.setBuilding1ShortType(element.getShortType());});
        CONSUMER_MAP.put(BUILDING2_LEVEL, (address, element) -> {address.setBuilding2(element.getData()); address.setBuilding2Type(element.getType()); address.setBuilding2ShortType(element.getShortType());});
        CONSUMER_MAP.put(APARTMENT_LEVEL, (address, element) -> {address.setApartment(element.getData()); address.setApartmentType(element.getType()); address.setApartmentShortType(element.getShortType());});
        CONSUMER_MAP.put(ADDITIONAL_AREA_LEVEL, (address, element) -> {address.setAdditionalArea(element.getData()); address.setAdditionalAreaType(element.getType()); address.setAdditionalAreaShortType(element.getShortType());});
        CONSUMER_MAP.put(ADDITIONAL_STREET_LEVEL, (address, element) -> {address.setAdditionalStreet(element.getData()); address.setAdditionalStreetType(element.getType()); address.setAdditionalStreetShortType(element.getShortType());});
    }

    private FullAddressEnrichUtil() {
    }

    /**
     * Заполнение адресных элементов из addressResponse
     * @see <a href="https://jira.egovdev.ru/browse/EPGUCORE-53184">EPGUCORE-53184</a>
     * @param fullAddress адрес структура для заполнения
     * @param addressResponse ответ очищенных данных от Dadata
     */
    public static void setAddressParts(FullAddress fullAddress, DadataAddressResponse addressResponse) {

        Optional<DadataAddress> dadataAddress = Optional.ofNullable(addressResponse)
                .map(DadataAddressResponse::getAddress);
        List<DadataAddressElement> elements = dadataAddress
            .map(DadataAddress::getElements)
            .orElse(Collections.emptyList());

        // Делаем рабочую копию
        Map<Integer, BiConsumer<FullAddress, DadataAddressElement>> consumerMapCopy = new HashMap<>(CONSUMER_MAP);

        // Заполняем елементы
        for (DadataAddressElement element: elements) {
            BiConsumer<FullAddress, DadataAddressElement> biConsumer = consumerMapCopy.get(element.getLevel());
            if (!isNull(biConsumer)) {
                biConsumer.accept(fullAddress, element);
                consumerMapCopy.remove(element.getLevel());
            }
        }

        // Выставление пустых значений в типы, которые не были переданы из addressResponse
        DadataAddressElement emptyElement = new DadataAddressElement();
        emptyElement.setData("");
        emptyElement.setShortType("");
        emptyElement.setType("");
        consumerMapCopy.forEach((k, v) -> v.accept(fullAddress, emptyElement));

        // Выставления признаков отсутствия адресных атрибутов
        fullAddress.setHouseCheckbox(isBlank(fullAddress.getHouse()));
        fullAddress.setApartmentCheckbox(isBlank(fullAddress.getApartment()));
        if (dadataAddress.isPresent()) {

            // Полный адрес
            fullAddress.setFullAddress(
                Stream.of(dadataAddress.get().getPostIndex(), dadataAddress.get().getFullAddress())
                    .filter(item -> isNotBlank(item))
                    .collect(Collectors.joining(", "))
            );

            // индекс
            fullAddress.setIndex(dadataAddress.get().getPostIndex());
        }
    }

    public static FullAddress enrich(FullAddress address) {
        MAPPER.updateAddress(address,address);
        return address;
    }
}
