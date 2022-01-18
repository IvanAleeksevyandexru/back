package ru.gosuslugi.pgu.common.certificate.service.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;
import ru.gosuslugi.pgu.common.certificate.xsd.dto.RegAddress;
import ru.gosuslugi.pgu.common.core.json.JsonProcessingUtil;
import ru.gosuslugi.pgu.components.descriptor.types.FullAddress;
import ru.gosuslugi.pgu.components.descriptor.types.RegistrationAddress;

@Mapper
public abstract class RegAddressMapper {
    public static final RegAddressMapper INSTANCE = Mappers.getMapper(RegAddressMapper.class);


    protected FullAddress getFullAddress(String regAddressString) {
        var regAddrDto = JsonProcessingUtil.fromJson(regAddressString, RegistrationAddress.class);
        return regAddrDto.getRegAddr();
    }

    @Mapping(target = "postIndex", source = "index")
    @Mapping(target = "area", source = "additionalArea")
    @Mapping(target = "fiasCode", source = "regionFias")
    protected abstract RegAddress map(FullAddress fullAddress);


    public RegAddress convert(String regAddressString) {
        return map(getFullAddress(regAddressString));
    }

}
