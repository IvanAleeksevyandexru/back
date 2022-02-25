package ru.gosuslugi.pgu.fs.utils;

import lombok.experimental.UtilityClass;
import org.apache.commons.lang.StringUtils;
import org.mapstruct.factory.Mappers;
import ru.gosuslugi.pgu.components.descriptor.types.FullAddress;
import ru.gosuslugi.pgu.fs.service.FullAddressEnrichMapper;
import ru.gosuslugi.pgu.pgu_common.nsi.dto.DadataAddress;
import ru.gosuslugi.pgu.pgu_common.nsi.dto.DadataAddressElement;
import ru.gosuslugi.pgu.pgu_common.nsi.dto.DadataAddressResponse;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.apache.commons.lang.StringUtils.isBlank;

@UtilityClass
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

    private static final String DISTRICT_CITY_TYPE = "город";
    private static final String FULL_ADDRESS_DELIMITER = ", ";

    private static final Map<Integer, BiFunction<FullAddress, DadataAddressElement, Integer>> CONSUMER_MAP =
            Map.ofEntries(
                    Map.entry(REGION_LEVEL, FullAddressEnrichUtil::setRegionPart),
                    Map.entry(DISTRICT_LEVEL, FullAddressEnrichUtil::setDistrictPart),
                    Map.entry(CITY_LEVEL, FullAddressEnrichUtil::setCityPart),
                    Map.entry(IN_CITY_DIST_LEVEL, FullAddressEnrichUtil::setInCityDistrictPart),
                    Map.entry(TOWN_LEVEL, FullAddressEnrichUtil::setTownPart),
                    Map.entry(STREET_LEVEL, FullAddressEnrichUtil::setStreetPart),
                    Map.entry(HOUSE_LEVEL, FullAddressEnrichUtil::setHousePart),
                    Map.entry(BUILDING1_LEVEL, FullAddressEnrichUtil::setBuilding1Part),
                    Map.entry(BUILDING2_LEVEL, FullAddressEnrichUtil::setBuilding2Part),
                    Map.entry(APARTMENT_LEVEL, FullAddressEnrichUtil::setApartmentPart),
                    Map.entry(ADDITIONAL_AREA_LEVEL, FullAddressEnrichUtil::setAdditionalAreaPart),
                    Map.entry(ADDITIONAL_STREET_LEVEL, FullAddressEnrichUtil::setAdditionalStreetPart)
            );

    /**
     * Заполнение адресных элементов из addressResponse
     *
     * @param fullAddress     адрес структура для заполнения
     * @param addressResponse ответ очищенных данных от Dadata
     * @see <a href="https://jira.egovdev.ru/browse/EPGUCORE-53184">EPGUCORE-53184</a>
     */
    public static void setAddressParts(FullAddress fullAddress, DadataAddressResponse addressResponse) {
        // Делаем рабочую копию
        Map<Integer, BiFunction<FullAddress, DadataAddressElement, Integer>> consumerMapCopy = new HashMap<>(CONSUMER_MAP);

        Optional<DadataAddress> dadataAddressOptional = Optional.ofNullable(addressResponse)
                .map(DadataAddressResponse::getAddress);

        List<DadataAddressElement> dadataAddressElements = dadataAddressOptional.map(DadataAddress::getElements)
                .orElse(Collections.emptyList());

        // Заполняем елементы и удаляем заполненные уровни из consumerMapCopy
        CONSUMER_MAP.forEach((level, enrichFunction) -> dadataAddressElements.stream()
                .filter(element -> Objects.equals(element.getLevel(), level))
                .findFirst()
                .ifPresent(element -> consumerMapCopy.remove(enrichFunction.apply(fullAddress, element))));

        // Выставление пустых значений в типы, которые не были переданы из addressResponse
        consumerMapCopy.forEach((level, enrichFunction) -> {
            DadataAddressElement emptyElement = new DadataAddressElement();
            emptyElement.setLevel(level);
            emptyElement.setData(StringUtils.EMPTY);
            emptyElement.setShortType(StringUtils.EMPTY);
            emptyElement.setType(StringUtils.EMPTY);
            enrichFunction.apply(fullAddress, emptyElement);
        });

        // Выставления признаков отсутствия адресных атрибутов
        fullAddress.setHouseCheckbox(isBlank(fullAddress.getHouse()));
        fullAddress.setApartmentCheckbox(isBlank(fullAddress.getApartment()));

        dadataAddressOptional.ifPresent(dadataAddress -> {
            // Полный адрес
            fullAddress.setFullAddress(
                    Stream.of(dadataAddress.getPostIndex(), dadataAddress.getFullAddress())
                            .filter(StringUtils::isNotBlank)
                            .collect(Collectors.joining(FULL_ADDRESS_DELIMITER))
            );
            // индекс
            fullAddress.setIndex(dadataAddress.getPostIndex());
            fullAddress.setPostalCode(dadataAddress.getPostIndex());
        });
    }

    public static FullAddress enrich(FullAddress address) {
        MAPPER.updateAddress(address, address);
        return address;
    }

    private static int setRegionPart(FullAddress address, DadataAddressElement element) {
        address.setRegion(element.getData());
        address.setRegionType(element.getType());
        address.setRegionShortType(element.getShortType());
        return REGION_LEVEL;
    }

    private static int setDistrictPart(FullAddress address, DadataAddressElement element) {
        if (element.getType().equalsIgnoreCase(DISTRICT_CITY_TYPE)) {
            address.setCity(element.getData());
            address.setCityType(element.getType());
            address.setCityShortType(element.getShortType());
            return CITY_LEVEL;
        } else {
            address.setDistrict(element.getData());
            address.setDistrictType(element.getType());
            address.setDistrictShortType(element.getShortType());
            return DISTRICT_LEVEL;
        }
    }

    private static int setCityPart(FullAddress address, DadataAddressElement element) {
        address.setCity(element.getData());
        address.setCityType(element.getType());
        address.setCityShortType(element.getShortType());
        return CITY_LEVEL;
    }

    private static int setInCityDistrictPart(FullAddress address, DadataAddressElement element) {
        address.setInCityDist(element.getData());
        address.setInCityDistType(element.getType());
        address.setInCityDistShortType(element.getShortType());
        return IN_CITY_DIST_LEVEL;
    }

    private static int setTownPart(FullAddress address, DadataAddressElement element) {
        address.setTown(element.getData());
        address.setTownType(element.getType());
        address.setTownShortType(element.getShortType());
        return TOWN_LEVEL;
    }

    private static int setStreetPart(FullAddress address, DadataAddressElement element) {
        address.setStreet(element.getData());
        address.setStreetType(element.getType());
        address.setStreetShortType(element.getShortType());
        return STREET_LEVEL;
    }

    private static int setHousePart(FullAddress address, DadataAddressElement element) {
        address.setHouse(element.getData());
        address.setHouseType(element.getType());
        address.setHouseShortType(element.getShortType());
        return HOUSE_LEVEL;
    }

    private static int setBuilding1Part(FullAddress address, DadataAddressElement element) {
        address.setBuilding1(element.getData());
        address.setBuilding1Type(element.getType());
        address.setBuilding1ShortType(element.getShortType());
        return BUILDING1_LEVEL;
    }

    private static int setBuilding2Part(FullAddress address, DadataAddressElement element) {
        address.setBuilding2(element.getData());
        address.setBuilding2Type(element.getType());
        address.setBuilding2ShortType(element.getShortType());
        return BUILDING2_LEVEL;
    }

    private static int setApartmentPart(FullAddress address, DadataAddressElement element) {
        address.setApartment(element.getData());
        address.setApartmentType(element.getType());
        address.setApartmentShortType(element.getShortType());
        return APARTMENT_LEVEL;
    }

    private static int setAdditionalAreaPart(FullAddress address, DadataAddressElement element) {
        address.setAdditionalArea(element.getData());
        address.setAdditionalAreaType(element.getType());
        address.setAdditionalAreaShortType(element.getShortType());
        return ADDITIONAL_AREA_LEVEL;
    }

    private static int setAdditionalStreetPart(FullAddress address, DadataAddressElement element) {
        address.setAdditionalStreet(element.getData());
        address.setAdditionalStreetType(element.getType());
        address.setAdditionalStreetShortType(element.getShortType());
        return ADDITIONAL_STREET_LEVEL;
    }
}
