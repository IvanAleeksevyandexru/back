package ru.gosuslugi.pgu.fs.component.medicine.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;
import ru.gosuslugi.pgu.fs.component.medicine.model.ReferralDto;

import java.util.Map;

@Mapper
public interface ReferralMapper {

    @Mappings({
        @Mapping(expression = "java(map.get(\"referralId\"))", target = "referralId"),
        @Mapping(expression = "java(map.get(\"referralNumber\"))", target = "referralNumber"),
        @Mapping(expression = "java(map.get(\"referralTypeId\"))", target = "referralTypeId"),
        @Mapping(expression = "java(map.get(\"referralStartDate\"))", target = "referralStartDate"),
        @Mapping(expression = "java(map.get(\"referralEndDate\"))", target = "referralEndDate"),
        @Mapping(expression = "java(map.get(\"paymentSourceId\"))", target = "paymentSourceId"),
        @Mapping(expression = "java(map.get(\"toMoOid\"))", target = "toMoOid"),
        @Mapping(expression = "java(map.get(\"toMoName\"))", target = "toMoName"),
        @Mapping(expression = "java(map.get(\"toServiceId\"))", target = "toServiceId"),
        @Mapping(expression = "java(map.get(\"toServiceName\"))", target = "toServiceName"),
        @Mapping(expression = "java(map.get(\"toSpecsId\"))", target = "toSpecsId"),
        @Mapping(expression = "java(map.get(\"toSpecsName\"))", target = "toSpecsName"),
        @Mapping(expression = "java(map.get(\"toResourceName\"))", target = "toResourceName"),
        @Mapping(expression = "java(map.get(\"fromMoOid\"))", target = "fromMoOid"),
        @Mapping(expression = "java(map.get(\"fromMoName\"))", target = "fromMoName"),
        @Mapping(expression = "java(map.get(\"fromSpecsId\"))", target = "fromSpecsId"),
        @Mapping(expression = "java(map.get(\"fromSpecsName\"))", target = "fromSpecsName"),
        @Mapping(expression = "java(map.get(\"fromResourceName\"))", target = "fromResourceName")
    })
    ReferralDto toReferral(Map<String, String> map);
}
