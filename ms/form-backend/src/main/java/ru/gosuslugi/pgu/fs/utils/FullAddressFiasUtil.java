package ru.gosuslugi.pgu.fs.utils;

import com.google.common.collect.Lists;
import ru.gosuslugi.pgu.components.descriptor.types.FullAddress;
import ru.gosuslugi.pgu.fs.component.confirm.mapper.FullAddressMapper;
import ru.gosuslugi.pgu.pgu_common.nsi.dto.DadataAddress;
import ru.gosuslugi.pgu.pgu_common.nsi.dto.DadataAddressElement;
import ru.gosuslugi.pgu.pgu_common.nsi.dto.DadataAddressResponse;

import java.util.Optional;

import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;

public class FullAddressFiasUtil {


    public static final int ERROR_COUNT = 0;

    private FullAddressFiasUtil() {
    }

    public static FullAddress addMetaInfo(FullAddress fullAddress, DadataAddressResponse addressResponse) {
        if (
            isNull(addressResponse.getDadataQc())
            || (
                addressResponse.getDadataQc() != 0
                && addressResponse.getDadataQc() != 3
            )
        ) {
            return null;
        }

        String geoLat = addressResponse.getGeo_lat();
        String geoLon = addressResponse.getGeo_lon();
        String lat = addressResponse.getGeo_lat();
        String lng = addressResponse.getGeo_lon();
        String fiasCode = Optional.ofNullable(addressResponse.getAddress()).map(DadataAddress::getFiasCode).orElse(null);
        String okato = addressResponse.getOkato();
        String oktmo = addressResponse.getOktmo();
        // See epgu2-form-frontend\dist\epgu-constructor\bundles\epgu-constructor.umd.js
        //   regionKladrId = normalAddress.address.elements.slice(-1)[0].kladrCode;
        String kladrCode = Optional.ofNullable(addressResponse.getAddress())
                .map(DadataAddress::getElements)
                .map(list -> Lists.reverse(list).stream().findFirst().orElse(null))
                .map(DadataAddressElement::getKladrCode)
                .orElse(null);
        // See epgu2-form-frontend\dist\epgu-constructor\bundles\epgu-constructor.umd.js
        //   regionCode = regionKladrId.toString().substring(0, 2);
        String regionCode = Optional.ofNullable(kladrCode).filter(str -> str.length() >=2).map(str -> str.substring(0, 2)).orElse(null);
        if (
            nonNull(geoLat)
            && nonNull(geoLon)
            && nonNull(lat)
            && nonNull(lng)
            && nonNull(fiasCode)
            && nonNull(okato)
            && nonNull(oktmo)
            && nonNull(kladrCode)
            && nonNull(regionCode)
        )  {
            fullAddress.setGeoLat(geoLat);
            fullAddress.setGeoLon(geoLon);
            fullAddress.setLat(lat);
            fullAddress.setLng(lng);
            fullAddress.setFiasCode(fiasCode);
            fullAddress.setOkato(okato);
            fullAddress.setOktmo(oktmo);
            fullAddress.setKladrCode(kladrCode);
            fullAddress.setRegionCode(regionCode);
            fullAddress.setHasErrors(ERROR_COUNT);
            return fullAddress;
        }
        return null;
    }

    public static FullAddress addMetaInfoWithOptionalGeoPoints(DadataAddressResponse addressResponse, FullAddressMapper fullAddressMapper){
        if (isNull(addressResponse.getDadataQc())
                || ( addressResponse.getDadataQc() != 0 && addressResponse.getDadataQc() != 3) ) return null;

        FullAddress fullAddress = fullAddressMapper.createMetaInfo(addressResponse);

        if (nonNull(fullAddress.getFiasCode())
                && nonNull(fullAddress.getOkato())
                && nonNull(fullAddress.getOktmo())
                && nonNull(fullAddress.getKladrCode())
                && nonNull(fullAddress.getRegionCode()))
            return fullAddress;
        return null;
    }

}
