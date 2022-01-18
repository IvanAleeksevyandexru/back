package ru.gosuslugi.pgu.fs.service;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import ru.gosuslugi.pgu.components.descriptor.types.FullAddress;

import java.util.Arrays;
import java.util.List;

import static org.apache.commons.lang.StringUtils.isBlank;

@Mapper
public interface FullAddressEnrichMapper {
    List<String> REGION_CITIES = Arrays.asList("Москва", "Санкт-Петербург", "Севастополь");

    @Mapping(expression = "java(normalize(fullAddress.getRegion(), fullAddress.getRegionType(), \"область\", \"город\"))", target = "regionType")
    @Mapping(expression = "java(normalize(fullAddress.getRegion(), fullAddress.getRegionShortType(), \"обл\", \"г\"))", target = "regionShortType")
    @Mapping(expression = "java(normalize(fullAddress.getDistrict(), fullAddress.getDistrictType(), \"район\"))", target = "districtType")
    @Mapping(expression = "java(normalize(fullAddress.getDistrict(), fullAddress.getDistrictShortType(), \"р-н\"))", target = "districtShortType")
    @Mapping(expression = "java(normalize(fullAddress.getCity(), fullAddress.getCityType(), \"город\"))", target = "cityType")
    @Mapping(expression = "java(normalize(fullAddress.getCity(), fullAddress.getCityShortType(), \"г\"))", target = "cityShortType")
    @Mapping(expression = "java(normalize(fullAddress.getInCityDist(), fullAddress.getInCityDistType(), \"район города\"))", target = "inCityDistType")
    @Mapping(expression = "java(normalize(fullAddress.getInCityDist(), fullAddress.getInCityDistShortType(), \"р-н\"))", target = "inCityDistShortType")
    @Mapping(expression = "java(normalize(fullAddress.getTown(), fullAddress.getTownType(), \"населенный пункт\"))", target = "townType")
    @Mapping(expression = "java(normalize(fullAddress.getTown(), fullAddress.getTownShortType(), \"нп\"))", target = "townShortType")
    @Mapping(expression = "java(normalize(fullAddress.getStreet(), fullAddress.getStreetType(), \"улица\"))", target = "streetType")
    @Mapping(expression = "java(normalize(fullAddress.getStreet(), fullAddress.getStreetShortType(), \"ул\"))", target = "streetShortType")
    @Mapping(expression = "java(normalize(fullAddress.getHouse(), fullAddress.getHouseType(), \"дом\"))", target = "houseType")
    @Mapping(expression = "java(normalize(fullAddress.getHouse(), fullAddress.getHouseShortType(), \"д\"))", target = "houseShortType")
    @Mapping(expression = "java(normalize(fullAddress.getBuilding1(), fullAddress.getBuilding1Type(), \"корпус\"))", target = "building1Type")
    @Mapping(expression = "java(normalize(fullAddress.getBuilding1(), fullAddress.getBuilding1ShortType(), \"к\"))", target = "building1ShortType")
    @Mapping(expression = "java(normalize(fullAddress.getBuilding2(), fullAddress.getBuilding2Type(), \"строение\"))", target = "building2Type")
    @Mapping(expression = "java(normalize(fullAddress.getBuilding2(), fullAddress.getBuilding2ShortType(), \"стр\"))", target = "building2ShortType")
    @Mapping(expression = "java(normalize(fullAddress.getApartment(), fullAddress.getApartmentType(), \"квартира\"))", target = "apartmentType")
    @Mapping(expression = "java(normalize(fullAddress.getApartment(), fullAddress.getApartmentShortType(), \"кв\"))", target = "apartmentShortType")
    @Mapping(expression = "java(normalize(fullAddress.getAdditionalArea(), fullAddress.getAdditionalAreaType(), \"территория\"))", target = "additionalAreaType")
    @Mapping(expression = "java(normalize(fullAddress.getAdditionalArea(), fullAddress.getAdditionalAreaShortType(), \"тер\"))", target = "additionalAreaShortType")
    @Mapping(expression = "java(normalize(fullAddress.getAdditionalStreet(), fullAddress.getAdditionalStreetType(), \"улица в доп. территории\"))", target = "additionalStreetType")
    @Mapping(expression = "java(normalize(fullAddress.getAdditionalStreet(), fullAddress.getAdditionalStreetShortType(), \"ул\"))", target = "additionalStreetShortType")
    void updateAddress(@MappingTarget FullAddress target, FullAddress fullAddress);

    default String stringToString(String str) {
        return str==null ? "" : str;
    }

    default String normalize(String field, String value, String defaultValue) {
        if (isBlank(field)) {
            return "";
        }
        if (isBlank(value)) {
            return defaultValue;
        }
        return value;
    }

    default String normalize(String field, String value, String defaultValue, String defaultCityValue) {
        if (isBlank(field)) {
            return "";
        }
        if (isBlank(value)) {
            if (REGION_CITIES.contains(field)) {
                return defaultCityValue;
            }
            return defaultValue;
        }
        return value;
    }
}
