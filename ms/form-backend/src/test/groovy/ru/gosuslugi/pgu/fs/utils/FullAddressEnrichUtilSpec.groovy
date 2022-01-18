package ru.gosuslugi.pgu.fs.utils

import ru.gosuslugi.pgu.components.descriptor.types.FullAddress
import spock.lang.Specification

class FullAddressEnrichUtilSpec extends Specification {

    static type = 'type'
    static shortType = 'shortType'

    def 'Test region enrich'() {
        given:
        def beforeEnrichAddress = new FullAddress()
        beforeEnrichAddress.setRegion(region)
        beforeEnrichAddress.setRegionType(regionType)
        beforeEnrichAddress.setRegionShortType(regionShortType)

        when:
        def afterEnrichAddress = FullAddressEnrichUtil.enrich(beforeEnrichAddress)

        then:
        afterEnrichAddress.getRegionType() == regionType
        afterEnrichAddress.getRegionShortType() == regionShortType

        where:
        region | regionType | regionShortType
        'Омская' | type | shortType
        'Москва' | type | shortType
    }

    def 'Test address enrich'() {
        given:
        def beforeEnrichAddress = new FullAddress()
        beforeEnrichAddress.setCity('Омск')
        beforeEnrichAddress.setCityType(type)
        beforeEnrichAddress.setCityShortType(shortType)
        beforeEnrichAddress.setDistrict('Кировск')
        beforeEnrichAddress.setDistrictType(type)
        beforeEnrichAddress.setDistrictShortType(shortType)
        beforeEnrichAddress.setTown('Талица')
        beforeEnrichAddress.setTownType(type)
        beforeEnrichAddress.setTownShortType(shortType)
        beforeEnrichAddress.setInCityDist('Московка')
        beforeEnrichAddress.setInCityDistType(type)
        beforeEnrichAddress.setInCityDistShortType(shortType)
        beforeEnrichAddress.setStreet('Лукашевича')
        beforeEnrichAddress.setStreetType(type)
        beforeEnrichAddress.setStreetShortType(shortType)
        beforeEnrichAddress.setAdditionalArea('Старый кипричный')
        beforeEnrichAddress.setAdditionalAreaType(type)
        beforeEnrichAddress.setAdditionalAreaShortType(shortType)
        beforeEnrichAddress.setAdditionalStreet('8 марта')
        beforeEnrichAddress.setAdditionalStreetType(type)
        beforeEnrichAddress.setAdditionalStreetShortType(shortType)
        beforeEnrichAddress.setHouse('5')
        beforeEnrichAddress.setHouseType(type)
        beforeEnrichAddress.setHouseShortType(shortType)
        beforeEnrichAddress.setBuilding1('1')
        beforeEnrichAddress.setBuilding1Type(type)
        beforeEnrichAddress.setBuilding1ShortType(shortType)
        beforeEnrichAddress.setBuilding2('2')
        beforeEnrichAddress.setBuilding2Type(type)
        beforeEnrichAddress.setBuilding2ShortType(shortType)
        beforeEnrichAddress.setApartment('11')
        beforeEnrichAddress.setApartmentType(type)
        beforeEnrichAddress.setApartmentShortType(shortType)

        when:
        def afterEnrichAddress = FullAddressEnrichUtil.enrich(beforeEnrichAddress)

        then:
        afterEnrichAddress.getCityType() == type
        afterEnrichAddress.getCityShortType() == shortType
        afterEnrichAddress.getDistrictType() == type
        afterEnrichAddress.getDistrictShortType() == shortType
        afterEnrichAddress.getTownType() == type
        afterEnrichAddress.getTownShortType() == shortType
        afterEnrichAddress.getInCityDistType() == type
        afterEnrichAddress.getInCityDistShortType() == shortType
        afterEnrichAddress.getStreetType() == type
        afterEnrichAddress.getStreetShortType() == shortType
        afterEnrichAddress.getAdditionalAreaType() == type
        afterEnrichAddress.getAdditionalAreaShortType() == shortType
        afterEnrichAddress.getAdditionalStreetType() == type
        afterEnrichAddress.getAdditionalStreetShortType() == shortType
        afterEnrichAddress.getHouseType() == type
        afterEnrichAddress.getHouseShortType() == shortType
        afterEnrichAddress.getBuilding1Type() == type
        afterEnrichAddress.getBuilding1ShortType() == shortType
        afterEnrichAddress.getBuilding2Type() == type
        afterEnrichAddress.getBuilding2ShortType() == shortType
        afterEnrichAddress.getApartmentType() == type
        afterEnrichAddress.getApartmentShortType() == shortType
    }


}