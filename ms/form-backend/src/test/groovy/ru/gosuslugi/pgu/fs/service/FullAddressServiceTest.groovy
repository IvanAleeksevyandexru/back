package ru.gosuslugi.pgu.fs.service

import ru.atc.carcass.security.rest.model.EsiaAddress
import ru.gosuslugi.pgu.fs.utils.FullAddressEnrichUtil
import ru.gosuslugi.pgu.pgu_common.nsi.dto.DadataAddress
import ru.gosuslugi.pgu.pgu_common.nsi.dto.DadataAddressElement
import ru.gosuslugi.pgu.pgu_common.nsi.dto.DadataAddressResponse
import spock.lang.Specification

class FullAddressServiceTest extends Specification {

    def fullAddressService = new FullAddressService(null)

    // DADATA in priority, then ESIA
    def postalCode() {

        expect:
        def full = fullAddressService.fromEsiaAddressAndAddressResponse(
                new EsiaAddress(zipCode: esia),
                new DadataAddressResponse(postalCode: postalCode, address: new DadataAddress(postIndex: postIndex), dadataQc: 0)
        )

        full.getIndex() == expected
        full.getPostalCode() == expected

        where:
        esia     | postalCode | postIndex | expected
        "111111" | "222222"   | "123456"  | "222222"
        "111111" | null       | "123456"  | "123456"
        "111111" | null       | null      | "111111"
        null     | ""         | ""        | ""
        null     | null       | ""        | ""

    }

    // ESIA in priority
    def flat() {

        expect:
        def full = fullAddressService.fromEsiaAddressAndAddressResponse(
                new EsiaAddress(flat: esia),
                new DadataAddressResponse(
                    address: new DadataAddress(elements: [
                        new DadataAddressElement(
                            level: 14,
                            data: dadata
                        )
                    ]),
                    dadataQc: 0
                )
        )

        full.getApartment() == expected

        where:
        esia     | dadata   | expected
        "11П1"   | "11"     | "11П1"
        null     | "11"     | ""
        "11"     | ""       | "11"
        null     | null     | ""

    }

    // DADATA in priority
    def fiasCode() {
        expect:
        def full = fullAddressService.fromEsiaAddressAndAddressResponse(
                new EsiaAddress(fiasCode: esia),
                new DadataAddressResponse(
                        address: new DadataAddress(fiasCode: dadata),
                        dadataQc: 0
                )
        )

        full.getFiasCode() == expected

        where:
        esia                                     | dadata                                   | expected
        "aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa"   | "bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb"   | "bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb"
        "aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa"   | null                                     | "aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa"
        null                                     | null                                     | ""

    }

    // DADATA only
    def kladrCode() {
        expect:

        def expected = "6900000100000970020"
        def full = fullAddressService.fromEsiaAddressAndAddressResponse(
                null,
                new DadataAddressResponse(
                        address: new DadataAddress(elements: [
                                new DadataAddressElement(
                                        kladrCode: "6900000100000"
                                ),
                                new DadataAddressElement(
                                        kladrCode: expected
                                )
                        ]),
                        dadataQc: 0
                )
        )
        full.getKladrCode() == expected

    }

    // DADATA only
    def regionCode() {

        expect:

        def full = fullAddressService.fromEsiaAddressAndAddressResponse(
                null,
                new DadataAddressResponse(
                        address: new DadataAddress(elements: [
                                new DadataAddressElement(
                                        kladrCode: "6900000100000"
                                ),
                                new DadataAddressElement(
                                        kladrCode: dadata
                                )
                        ]),
                        dadataQc: 0
                )
        )

        full.getRegionCode() == expected

        where:
        dadata            | expected
        "6900000100000"   | "69"
        "6"               | ""
        null              | ""

    }

    // DADATA only
    def latLon() {

        expect:
        def full = fullAddressService.fromEsiaAddressAndAddressResponse(
                null,
                new DadataAddressResponse(
                        geo_lat: lat,
                        geo_lon: lon,
                        dadataQc: 0
                )
        )

        full.getGeoLat() == expectedLat
        full.getLat() == expectedLat

        full.getGeoLon() == expectedLon
        full.getLng() == expectedLon

        where:
        lat             | lon           | expectedLat       | expectedLon
        "56.8790793"    | "35.8911467"  | "56.8790793"      | "35.8911467"
        null            | ""            | ""                | ""

    }

    // ESIA in priority
    def house() {
        expect:
        def full = fullAddressService.fromEsiaAddressAndAddressResponse(
                new EsiaAddress(
                        house: "10/17"
                ),
                new DadataAddressResponse(
                        address: new DadataAddress(elements: [
                                new DadataAddressElement(level: FullAddressEnrichUtil.HOUSE_LEVEL, data: "10", type: "", shortType: ""),
                        ]),
                        dadataQc: 0
                )
        )

        full.getHouse() == "10/17"
        full.getHouseShortType() == ""
        full.getHouseType() == ""

    }

    // ESIA in priority
    def building1() {

        expect:
        def full = fullAddressService.fromEsiaAddressAndAddressResponse(
                new EsiaAddress(
                        frame: "1"
                ),
                new DadataAddressResponse(
                        address: new DadataAddress(elements: [
                                new DadataAddressElement(level: FullAddressEnrichUtil.BUILDING1_LEVEL, data: "2", shortType: "к", type: "корпус"),
                        ]),
                        dadataQc: 0
                )
        )

        full.getBuilding1() == "1"
        full.getBuilding1ShortType() == "к"
        full.getBuilding1Type() == "корпус"

    }

    // ESIA in priority
    def building2() {

        expect:
        def full = fullAddressService.fromEsiaAddressAndAddressResponse(
                new EsiaAddress(
                        building: "1"
                ),
                new DadataAddressResponse(
                        address: new DadataAddress(elements: [
                                new DadataAddressElement(level: FullAddressEnrichUtil.BUILDING2_LEVEL, data: "2", shortType: "стр", type: "строение"),
                        ]),
                        dadataQc: 0
                )
        )

        full.getBuilding2() == "1"
        full.getBuilding2ShortType() == "стр"
        full.getBuilding2Type() == "строение"

    }

    // DADATA in priority
    def region() {

        expect:
        def full = fullAddressService.fromEsiaAddressAndAddressResponse(
                new EsiaAddress(region: "Москва"),
                new DadataAddressResponse(
                        address: new DadataAddress(elements: [
                                new DadataAddressElement(
                                        level: FullAddressEnrichUtil.REGION_LEVEL,
                                        data: "Санкт-Петербург",
                                        shortType: "г",
                                        type: "город"
                                )
                        ]),
                        dadataQc: 0
                )
        )

        full.getRegion() == "Санкт-Петербург"
        full.getRegionShortType() == "г"
        full.getRegionType() == "город"

    }

    // DADATA in priority
    def district() {

        expect:
        def full = fullAddressService.fromEsiaAddressAndAddressResponse(
                new EsiaAddress(area: "Северный"),
                new DadataAddressResponse(
                        address: new DadataAddress(elements: [
                                new DadataAddressElement(level: FullAddressEnrichUtil.DISTRICT_LEVEL, data: "Южный", shortType: "р-н", type: "район")
                        ]),
                        dadataQc: 0
                )
        )

        full.getDistrict() == "Южный"
        full.getDistrictShortType() == "р-н"
        full.getDistrictType() == "район"

    }

    // DADATA in priority
    def city() {

        expect:
        def full = fullAddressService.fromEsiaAddressAndAddressResponse(
                new EsiaAddress(city: "Тула"),
                new DadataAddressResponse(
                        address: new DadataAddress(elements: [
                                new DadataAddressElement(
                                        level: FullAddressEnrichUtil.CITY_LEVEL,
                                        data: "Екатеринбург",
                                        shortType: "г",
                                        type: "город"
                                )
                        ]),
                        dadataQc: 0
                )
        )

        full.getCity() == "Екатеринбург"
        full.getCityShortType() == "г"
        full.getCityType() == "город"

    }

    // DADATA in priority
    def town() {

        expect:
        def full = fullAddressService.fromEsiaAddressAndAddressResponse(
                new EsiaAddress(settlement: "ДНТ Домостроитель"),
                new DadataAddressResponse(
                        address: new DadataAddress(elements: [
                                new DadataAddressElement(
                                        level: FullAddressEnrichUtil.TOWN_LEVEL,
                                        data: "ДНТ Сарай",
                                        shortType: "тер",
                                        type: "территория"
                                )
                        ]),
                        dadataQc: 0
                )
        )

        full.getTown() == "ДНТ Сарай"
        full.getTownShortType() == "тер"
        full.getTownType() == "территория"

    }

    // DADATA in priority
    def street() {

        expect:
        def full = fullAddressService.fromEsiaAddressAndAddressResponse(
                new EsiaAddress(street: "улица Майская"),
                new DadataAddressResponse(
                        address: new DadataAddress(elements: [
                                new DadataAddressElement(
                                        level: FullAddressEnrichUtil.STREET_LEVEL,
                                        data: "Майская",
                                        shortType: "ул",
                                        type: "улица"
                                )
                        ]),
                        dadataQc: 0
                )
        )

        full.getStreet() == "Майская"
        full.getStreetShortType() == "ул"
        full.getStreetType() == "улица"

    }

    // DADATA in priority
    def inCityDistrict() {

        expect:
        def full = fullAddressService.fromEsiaAddressAndAddressResponse(
                new EsiaAddress(district: "Симферопольский"),
                new DadataAddressResponse(
                        address: new DadataAddress(elements: [
                                new DadataAddressElement(
                                        level: FullAddressEnrichUtil.IN_CITY_DIST_LEVEL,
                                        data: "Нижнегородский",
                                        shortType: "р-н",
                                        type: "район"
                                )
                        ]),
                        dadataQc: 0
                )
        )

        full.getInCityDist() == "Нижнегородский"
        full.getInCityDistShortType() == "р-н"
        full.getInCityDistType() == "район"

    }

    def addressStr() {

        expect:
        def full = fullAddressService.fromEsiaAddressAndAddressResponse(
                new EsiaAddress(region: "Не москва", street: "Другая", house: "5", frame: "1", building: "11", flat: "51"),
                new DadataAddressResponse(
                        address: new DadataAddress(elements: [
                                new DadataAddressElement(
                                        level: FullAddressEnrichUtil.REGION_LEVEL,
                                        data: "Москва",
                                        shortType: "г",
                                ),
                                new DadataAddressElement(
                                        level: FullAddressEnrichUtil.STREET_LEVEL,
                                        data: "Московская",
                                        shortType: "ул",
                                ),
                                new DadataAddressElement(
                                        level: FullAddressEnrichUtil.HOUSE_LEVEL,
                                        data: "100",
                                        shortType: "д",
                                ),
                                new DadataAddressElement(
                                        level: FullAddressEnrichUtil.BUILDING1_LEVEL,
                                        data: "50",
                                        shortType: "к",
                                ),
                                new DadataAddressElement(
                                        level: FullAddressEnrichUtil.BUILDING2_LEVEL,
                                        data: "10",
                                        shortType: "стр",
                                ),
                                new DadataAddressElement(
                                        level: FullAddressEnrichUtil.APARTMENT_LEVEL,
                                        data: "333",
                                        shortType: "кв",
                                )
                        ]),
                        dadataQc: 0
                )
        )

        full.getAddressStr() == "г. Москва, ул. Московская, д. 5, к. 1, стр. 11, кв. 51"

    }

    def houseCheckbox() {

        expect:

        def full = fullAddressService.fromEsiaAddressAndAddressResponse(
                new EsiaAddress(house: esia),
                new DadataAddressResponse(
                        address: new DadataAddress(elements: [
                                new DadataAddressElement(
                                        level: FullAddressEnrichUtil.HOUSE_LEVEL,
                                        data: dadata,
                                        shortType: "д"
                                )
                        ]),
                        dadataQc: 0
                )
        )

        full.isHouseCheckbox() == expected

        where:
        esia    | dadata | expected
        "15"    | null   | false
        "10"    | "15"   | false
        ""      | ""     | true
        ""      | "10"   | true
        null    | "5"    | true

    }

    def appartmentCheckbox() {

        expect:

        def full = fullAddressService.fromEsiaAddressAndAddressResponse(
                new EsiaAddress(flat: esia),
                new DadataAddressResponse(
                        address: new DadataAddress(elements: [
                                new DadataAddressElement(
                                        level: FullAddressEnrichUtil.APARTMENT_LEVEL,
                                        data: dadata,
                                        shortType: "кв"
                                )
                        ]),
                        dadataQc: 0
                )
        )

        full.isApartmentCheckbox() == expected

        where:
        esia    | dadata | expected
        "15"    | null   | false
        "10"    | "15"   | false
        ""      | ""     | true
        ""      | "10"   | true
        null    | "5"    | true

    }

}
