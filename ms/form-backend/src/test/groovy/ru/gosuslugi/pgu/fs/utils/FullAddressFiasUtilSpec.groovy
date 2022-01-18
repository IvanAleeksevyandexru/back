package ru.gosuslugi.pgu.fs.utils

import ru.gosuslugi.pgu.common.core.json.JsonFileUtil
import ru.gosuslugi.pgu.common.core.json.JsonProcessingUtil
import ru.gosuslugi.pgu.components.descriptor.types.FullAddress
import ru.gosuslugi.pgu.components.descriptor.types.RegistrationAddress
import ru.gosuslugi.pgu.pgu_common.nsi.dto.DadataAddressResponse
import spock.lang.Shared
import spock.lang.Specification

class FullAddressFiasUtilSpec extends Specification {

    @Shared
    RegistrationAddress regAddress
    @Shared
    DadataAddressResponse dadataResponse

    def setupSpec() {
        regAddress = JsonProcessingUtil.fromJson(
                JsonFileUtil.getJsonFromFile(this.getClass(), "_FullAddress.json"),
                RegistrationAddress.class)
        dadataResponse = JsonProcessingUtil.fromJson(
                JsonFileUtil.getJsonFromFile(this.getClass(), "_DadataResponse.json"),
                DadataAddressResponse.class)
    }

    def 'test input'() {
        when:
        regAddress.getRegAddr()

        then:
        dadata.getGeo_lat() == address.getGeoLat()
        dadata.getGeo_lon() == address.getGeoLon()
        dadata.getGeo_lat() == address.getLat()
        dadata.getGeo_lon() == address.getLng()
        dadata.getOkato() == address.getOkato()
        dadata.getAddress().getFiasCode() == address.getFiasCode()
        address.getKladrCode() == '5000002400003820014'
        address.getRegionCode() == '50'

        where:
        dadata         | address
        dadataResponse | regAddress.getRegAddr()
        dadataResponse | FullAddressFiasUtil.addMetaInfo(new FullAddress(), dadataResponse)
    }

    def 'test error'() {
        when:
        regAddress.getRegAddr()

        then:
        address == null

        where:
        dadata                                      | address
        setDadataQc(dadataResponse, 2)              | FullAddressFiasUtil.addMetaInfo(new FullAddress(), dadata)
        new DadataAddressResponse()                 | FullAddressFiasUtil.addMetaInfo(new FullAddress(), dadata)
        setDadataQc(new DadataAddressResponse(), 2) | FullAddressFiasUtil.addMetaInfo(new FullAddress(), dadata)
    }

    static DadataAddressResponse setDadataQc(DadataAddressResponse dadataResponse, int qc) {
        dadataResponse.setDadataQc(qc)
        return dadataResponse
    }
}
