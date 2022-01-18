package ru.gosuslugi.pgu.fs.component.confirm.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import ru.gosuslugi.pgu.components.descriptor.types.FullAddress;
import ru.gosuslugi.pgu.pgu_common.nsi.dto.DadataAddressResponse;

@Mapper
public interface FullAddressMapper {
    
    @Mapping(expression = "java(addressResponse.getGeo_lat())", target = "geoLat")
    @Mapping(expression = "java(addressResponse.getGeo_lon())", target = "geoLon")
    @Mapping(expression = "java(addressResponse.getGeo_lat())", target = "lat")
    @Mapping(expression = "java(addressResponse.getGeo_lon())", target = "lng")
    @Mapping(expression = "java(addressResponse.getOkato())", target = "okato")
    @Mapping(expression = "java(addressResponse.getOktmo())", target = "oktmo")
    @Mapping(expression = "java(ru.gosuslugi.pgu.fs.component.confirm.util.FullAddressMapperUtil.getKladrCode(addressResponse.getAddress()))", target = "kladrCode")
    @Mapping(expression = "java(ru.gosuslugi.pgu.fs.component.confirm.util.FullAddressMapperUtil.getRegionCode(addressResponse.getAddress()))", target = "regionCode")
    @Mapping(expression = "java(ru.gosuslugi.pgu.fs.component.confirm.util.FullAddressMapperUtil.getFiasCode(addressResponse.getAddress()))", target = "fiasCode")
    @Mapping(constant = "0", target = "hasErrors")
    @Mapping(expression = "java(ru.gosuslugi.pgu.fs.component.confirm.util.FullAddressMapperUtil.getAddressElementFiasCode(addressResponse.getAddress(), 1))", target = "regionFias")
    @Mapping(expression = "java(ru.gosuslugi.pgu.fs.component.confirm.util.FullAddressMapperUtil.getAddressElementFiasCode(addressResponse.getAddress(), 4))", target = "townFias")
    @Mapping(expression = "java(ru.gosuslugi.pgu.fs.component.confirm.util.FullAddressMapperUtil.getAddressElementFiasCode(addressResponse.getAddress(), 7))", target = "streetFias")
    @Mapping(expression = "java(ru.gosuslugi.pgu.fs.component.confirm.util.FullAddressMapperUtil.getAddressElementFiasCode(addressResponse.getAddress(), 11))", target = "houseFias")
    FullAddress createMetaInfo(DadataAddressResponse addressResponse);
}
