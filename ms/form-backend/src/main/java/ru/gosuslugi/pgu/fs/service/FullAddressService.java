package ru.gosuslugi.pgu.fs.service;

import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import ru.atc.carcass.security.rest.model.EsiaAddress;
import ru.gosuslugi.pgu.components.descriptor.types.FullAddress;
import ru.gosuslugi.pgu.fs.component.confirm.util.FullAddressMapperUtil;
import ru.gosuslugi.pgu.fs.utils.FullAddressEnrichUtil;
import ru.gosuslugi.pgu.pgu_common.nsi.dto.DadataAddressResponse;
import ru.gosuslugi.pgu.pgu_common.nsi.service.NsiDadataService;

@Service
@RequiredArgsConstructor
public class FullAddressService {

    private final NsiDadataService nsiDadataService;

    public FullAddress fromEsiaAddress(EsiaAddress esiaAddress) {
        var dadataResponse = nsiDadataService
            .getAddress(joinNonBlank(", ", esiaAddress.getZipCode(), esiaAddress.getAddressStr()));
        return fromEsiaAddressAndAddressResponse(esiaAddress, dadataResponse);
    }

    public FullAddress fromEsiaAddressAndAddressResponse(EsiaAddress esiaAddress, DadataAddressResponse addressResponse) {

        final var fullAddress = new FullAddress();
        if (addressResponse != null && addressResponse.getDadataQc() != null && addressResponse.getDadataQc() != 2) {

            fullAddress.setGeoLat(emptyIfBlank(addressResponse.getGeo_lat()));
            fullAddress.setGeoLon(emptyIfBlank(addressResponse.getGeo_lon()));
            fullAddress.setLat(emptyIfBlank(addressResponse.getGeo_lat()));
            fullAddress.setLng(emptyIfBlank(addressResponse.getGeo_lon()));
            fullAddress.setOkato(emptyIfBlank(addressResponse.getOkato()));
            fullAddress.setOktmo(emptyIfBlank(addressResponse.getOktmo()));
            setIndexAndPostalCode(fullAddress, emptyIfBlank(addressResponse.getPostalCode()));

            final var address = addressResponse.getAddress();
            if (address != null) {

                fullAddress.setFiasCode(emptyIfBlank(FullAddressMapperUtil.getFiasCode(address)));
                fullAddress.setKladrCode(emptyIfBlank(FullAddressMapperUtil.getKladrCode(address)));
                fullAddress.setRegionCode(emptyIfBlank(FullAddressMapperUtil.getRegionCode(address)));

                fullAddress.setAddressStr(emptyIfBlank(address.getFullAddress()));
                fullAddress.setFiasCode(emptyIfBlank(address.getFiasCode()));

                if (StringUtils.isBlank(fullAddress.getPostalCode())) {
                    setIndexAndPostalCode(fullAddress, emptyIfBlank(address.getPostIndex()));
                }

                if (address.getElements() != null) {
                    FullAddressEnrichUtil.setAddressParts(fullAddress, address.getElements());
                }
            }
        }

        if (esiaAddress != null) {

            if (StringUtils.isBlank(fullAddress.getPostalCode())) {
                setIndexAndPostalCode(fullAddress, emptyIfBlank(esiaAddress.getZipCode()));
            }

            if (StringUtils.isBlank(fullAddress.getFiasCode())) {
                fullAddress.setFiasCode(emptyIfBlank(esiaAddress.getFiasCode()));
            }

            // Если адрес не удалось распарсить хотя бы в каком-то виде информацию по адресу берем полностью из ЛК
            if (addressResponse == null || addressResponse.getDadataQc() == 2) {
                fullAddress.setRegion(emptyIfBlank(esiaAddress.getRegion()));
                fullAddress.setDistrict(emptyIfBlank(esiaAddress.getArea()));
                fullAddress.setCity(emptyIfBlank(esiaAddress.getCity()));
                fullAddress.setInCityDist(emptyIfBlank(esiaAddress.getDistrict()));
                fullAddress.setTown(emptyIfBlank(esiaAddress.getSettlement()));
                fullAddress.setStreet(emptyIfBlank(esiaAddress.getStreet()));
                fullAddress.setAdditionalArea(emptyIfBlank(esiaAddress.getAdditionArea()));
                fullAddress.setAdditionalStreet(emptyIfBlank(esiaAddress.getAdditionAreaStreet()));
            }

            // Информация по дому, строению, корпусу, квартире берем полностью из ЛК (см. EPGUCORE-88127)
            fullAddress.setHouse(emptyIfBlank(esiaAddress.getHouse()));
            fullAddress.setBuilding1(emptyIfBlank(esiaAddress.getFrame()));
            fullAddress.setBuilding2(emptyIfBlank(esiaAddress.getBuilding()));
            fullAddress.setApartment(emptyIfBlank(esiaAddress.getFlat()));

        }

        fullAddress.setHouseCheckbox(StringUtils.isBlank(fullAddress.getHouse()));
        fullAddress.setApartmentCheckbox(StringUtils.isBlank(fullAddress.getApartment()));
        fullAddress.setHasErrors(0);

        fullAddress.setAddressStr(joinNonBlank(", ",
            joinAddressPart(fullAddress.getRegion(), fullAddress.getRegionShortType()),
            joinAddressPart(fullAddress.getDistrict(), fullAddress.getDistrictShortType()),
            joinAddressPart(fullAddress.getAdditionalArea(), fullAddress.getAdditionalAreaShortType()),
            joinAddressPart(fullAddress.getCity(), fullAddress.getCityShortType()),
            joinAddressPart(fullAddress.getInCityDist(), fullAddress.getInCityDistShortType()),
            joinAddressPart(fullAddress.getTown(), fullAddress.getTownShortType()),
            joinAddressPart(fullAddress.getStreet(), fullAddress.getStreetShortType()),
            joinAddressPart(fullAddress.getAdditionalStreet(), fullAddress.getAdditionalStreetShortType()),
            joinAddressPart(fullAddress.getHouse(), fullAddress.getHouseShortType()),
            joinAddressPart(fullAddress.getBuilding1(), fullAddress.getBuilding1ShortType()),
            joinAddressPart(fullAddress.getBuilding2(), fullAddress.getBuilding2ShortType()),
            joinAddressPart(fullAddress.getApartment(), fullAddress.getApartmentShortType())
        ));

        fullAddress.setFullAddress(joinNonBlank(", ", fullAddress.getPostalCode(), fullAddress.getAddressStr()));
        return fullAddress;

    }

    public static String emptyIfBlank(String value) {
        return StringUtils.isBlank(value) ? "" : value;
    }

    public static String joinNonBlank(String separator, String... parts) {
        final var result = new StringBuilder();
        for (String v: parts) {
            if (StringUtils.isBlank(v)) {
                continue;
            }
            if (result.length() > 0) {
                result.append(separator);
            }
            result.append(v);
        }
        return result.toString();
    }

    public static String joinAddressPart(String part, String prefix) {
        return StringUtils.isBlank(part) ? "" : StringUtils.isBlank(prefix) ? part : prefix + ". " + part;
    }

    private void setIndexAndPostalCode(FullAddress fullAddress, String postalCode) {
        fullAddress.setIndex(postalCode);
        fullAddress.setPostalCode(postalCode);
    }

}
