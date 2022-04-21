package ru.gosuslugi.pgu.components.descriptor.types;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
@AllArgsConstructor
@NoArgsConstructor
public class FullAddress {
    private String regionType;
    private String regionShortType;
    private String region;

    private String cityType;
    private String cityShortType;
    private String city;

    private String districtType;
    private String districtShortType;
    private String district;

    private String townType;
    private String townShortType;
    private String town;

    private String inCityDistType;
    private String inCityDistShortType;
    private String inCityDist;

    private String streetType;
    private String streetShortType;
    private String street;

    private String additionalAreaType;
    private String additionalAreaShortType;
    private String additionalArea;

    private String additionalStreetType;
    private String additionalStreetShortType;
    private String additionalStreet;

    private String houseType;
    private String houseShortType;
    private String house;

    private boolean houseCheckbox;
    private boolean houseCheckboxClosed;

    private String building1Type;
    private String building1ShortType;
    private String building1;

    private String building2Type;
    private String building2ShortType;
    private String building2;

    private String apartmentType;
    private String apartmentShortType;
    private String apartment;

    private boolean apartmentCheckbox;
    private boolean apartmentCheckboxClosed;
    private String index;
    private String geoLat;
    private String geoLon;
    private String fullAddress;
    private String addressStr;
    private String lat;
    private String lng;
    private String kladrCode;
    private String fiasCode;
    private String regionFias;
    private String regionKladr;
    private String townFias;
    private String townKladr;
    private String streetFias;
    private String streetKladr;
    private String houseFias;
    private String houseKladr;
    private String apartmentFias;
    private String apartmentKladr;
    private String okato;
    private String oktmo;
    private Integer hasErrors;
    private String regionCode;
    private String postalCode;

    private String oktmoName;
}
