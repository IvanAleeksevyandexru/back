package ru.gosuslugi.pgu.fs.utils

import ru.gosuslugi.pgu.components.descriptor.types.FullAddress
import spock.lang.Specification

class FullAddressEnrichUtilEmptySpec extends Specification {

    def "full address must contain empty nodes if it is empty"() {
        given:
        FullAddress before = new FullAddress();

        when:
        FullAddress address = FullAddressEnrichUtil.enrich(before);

        then:
        address.getRegionType().isEmpty()
        address.getRegionShortType().isEmpty()

        address.getCityType().isEmpty()
        address.getCityShortType().isEmpty()

        address.getDistrictType().isEmpty()
        address.getDistrictShortType().isEmpty()

        address.getTownType().isEmpty()
        address.getTownShortType().isEmpty()

        address.getInCityDistType().isEmpty()
        address.getInCityDistShortType().isEmpty()

        address.getStreetType().isEmpty()
        address.getStreetShortType().isEmpty()

        address.getAdditionalAreaType().isEmpty()
        address.getAdditionalAreaShortType().isEmpty()

        address.getAdditionalStreetType().isEmpty()
        address.getAdditionalStreetShortType().isEmpty()

        address.getHouseType().isEmpty()
        address.getHouseShortType().isEmpty()

        address.getBuilding1Type().isEmpty()
        address.getBuilding1ShortType().isEmpty()

        address.getBuilding2Type().isEmpty()
        address.getBuilding2ShortType().isEmpty()

        address.getApartmentType().isEmpty()
        address.getApartmentShortType().isEmpty()
    }
}