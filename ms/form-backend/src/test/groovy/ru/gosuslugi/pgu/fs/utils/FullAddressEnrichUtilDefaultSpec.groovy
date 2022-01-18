package ru.gosuslugi.pgu.fs.utils

import ru.gosuslugi.pgu.components.descriptor.types.FullAddress
import spock.lang.Specification

class FullAddressEnrichUtilDefaultSpec extends Specification {

    def 'Test region enrich'() {
        given:
        def beforeEnrichAddress = new FullAddress()
        beforeEnrichAddress.setRegion(region)

        when:
        def afterEnrichAddress = FullAddressEnrichUtil.enrich(beforeEnrichAddress)

        then:
        afterEnrichAddress.getRegionType() == expectedRegionType
        afterEnrichAddress.getRegionShortType() == expectedRegionShortType

        where:
        region | expectedRegionType | expectedRegionShortType
        'Омская' | 'область' | 'обл'
        'Москва' | 'город' | 'г'
    }

    def 'Test address enrich'() {
        given:
        def beforeEnrichAddress = new FullAddress()
        beforeEnrichAddress.setCity('Омск')
        beforeEnrichAddress.setDistrict('Кировск')
        beforeEnrichAddress.setTown('Талица')
        beforeEnrichAddress.setInCityDist('Московка')
        beforeEnrichAddress.setStreet('Лукашевича')
        beforeEnrichAddress.setAdditionalArea('Старый кипричный')
        beforeEnrichAddress.setAdditionalStreet('8 марта')
        beforeEnrichAddress.setHouse('5')
        beforeEnrichAddress.setBuilding1('1')
        beforeEnrichAddress.setBuilding2('2')
        beforeEnrichAddress.setApartment('11')

        when:
        def afterEnrichAddress = FullAddressEnrichUtil.enrich(beforeEnrichAddress)

        then:
        afterEnrichAddress.getCityType() == 'город'
        afterEnrichAddress.getCityShortType() == 'г'
        afterEnrichAddress.getDistrictType() == 'район'
        afterEnrichAddress.getDistrictShortType() == 'р-н'
        afterEnrichAddress.getTownType() == 'населенный пункт'
        afterEnrichAddress.getTownShortType() == 'нп'
        afterEnrichAddress.getInCityDistType() == 'район города'
        afterEnrichAddress.getInCityDistShortType() == 'р-н'
        afterEnrichAddress.getStreetType() == 'улица'
        afterEnrichAddress.getStreetShortType() == 'ул'
        afterEnrichAddress.getAdditionalAreaType() == 'территория'
        afterEnrichAddress.getAdditionalAreaShortType() == 'тер'
        afterEnrichAddress.getAdditionalStreetType() == 'улица в доп. территории'
        afterEnrichAddress.getAdditionalStreetShortType() == 'ул'
        afterEnrichAddress.getHouseType() == 'дом'
        afterEnrichAddress.getHouseShortType() == 'д'
        afterEnrichAddress.getBuilding1Type() == 'корпус'
        afterEnrichAddress.getBuilding1ShortType() == 'к'
        afterEnrichAddress.getBuilding2Type() == 'строение'
        afterEnrichAddress.getBuilding2ShortType() == 'стр'
        afterEnrichAddress.getApartmentType() == 'квартира'
        afterEnrichAddress.getApartmentShortType() == 'кв'
    }

}