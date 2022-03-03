package ru.gosuslugi.pgu.fs.utils;

import lombok.experimental.UtilityClass;
import ru.gosuslugi.pgu.components.descriptor.types.FullAddress;
import ru.gosuslugi.pgu.fs.component.confirm.util.FullAddressMapperUtil;
import ru.gosuslugi.pgu.pgu_common.nsi.dto.DadataAddressResponse;

import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;

@UtilityClass
public class FullAddressFiasUtil {

    public static final int ERROR_COUNT = 0;

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
        String okato = addressResponse.getOkato();
        String oktmo = addressResponse.getOktmo();
        String fiasCode = FullAddressMapperUtil.getFiasCode(addressResponse.getAddress());
        String kladrCode = FullAddressMapperUtil.getKladrCode(addressResponse.getAddress());
        String regionCode = FullAddressMapperUtil.getRegionCode(addressResponse.getAddress());
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

}
