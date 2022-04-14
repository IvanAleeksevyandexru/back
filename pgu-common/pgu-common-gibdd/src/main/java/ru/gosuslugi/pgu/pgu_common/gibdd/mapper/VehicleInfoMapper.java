package ru.gosuslugi.pgu.pgu_common.gibdd.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;
import org.springframework.beans.factory.annotation.Qualifier;
import ru.gosuslugi.pgu.pgu_common.gibdd.dto.OwnerPeriod;
import ru.gosuslugi.pgu.pgu_common.gibdd.dto.RegAction;
import ru.gosuslugi.pgu.pgu_common.gibdd.dto.RestrictionsInformation;
import ru.gosuslugi.pgu.pgu_common.gibdd.dto.VehicleInfo;
import ru.gosuslugi.pgu.pgu_common.nsi.dto.NsiDictionaryItem;

/**
 * https://jira.egovdev.ru/browse/EPGUCORE-90200 - расширение для 1.4+, включение нормализованных значений
 */
@Mapper
@Qualifier("vehicleInfoMapper")
public interface VehicleInfoMapper {

    @Mappings({
            @Mapping(expression = "java(item.getAttributeValue(\"RecordStatus\"))", target = "statusIntValue"),
            @Mapping(expression = "java(Boolean.valueOf(item.getAttributeValues().getOrDefault(\"RestrictionsFlag\", \"\").toString()))", target = "restrictionsFlag"),
            @Mapping(expression = "java(item.getAttributeValue(\"Category\"))", target = "category"),
            @Mapping(expression = "java(Boolean.valueOf(item.getAttributeValues().getOrDefault(\"SearchingTransportFlag\", \"\").toString()))", target = "searchingTransportFlag"),
            @Mapping(expression = "java(item.getAttributeValue(\"CarcaseColor\"))", target = "carcaseColor"),
            @Mapping(expression = "java(item.getAttributeValue(\"MarkName\"))", target = "markName"),
            @Mapping(expression = "java(item.getAttributeValue(\"ModelName\"))", target = "modelName"),
            @Mapping(expression = "java(item.getAttributeValue(\"ModelMarkName\"))", target = "modelMarkName"),
            @Mapping(expression = "java(item.getAttributeValue(\"ManufacturedYear\"))", target = "manufacturedYear"),
            @Mapping(expression = "java(item.getAttributeValue(\"GovRegNumber\"))", target = "govRegNumber"),
            @Mapping(expression = "java(item.getAttributeValue(\"RecordStatus\"))", target = "recordStatus"),
            @Mapping(expression = "java(item.getAttributeValue(\"VIN\"))", target = "vin"),
            @Mapping(expression = "java(item.getAttributeValue(\"ChassisNumber\"))", target = "chassisNumber"),
            @Mapping(expression = "java(item.getAttributeValue(\"CarcaseNumber\"))", target = "carcaseNumber"),
            @Mapping(expression = "java(item.getAttributeValue(\"VenicleType\"))", target = "vehicleType"),
            @Mapping(expression = "java(item.getAttributeValue(\"EngineVolume\"))", target = "engineVolume"),
            @Mapping(expression = "java(item.getAttributeValue(\"EnginePowerHorse\"))", target = "enginePowerHorse"),
            @Mapping(expression = "java(item.getAttributeValue(\"EnginePowerkVt\"))", target = "enginePowerVt"),
            @Mapping(expression = "java(item.getAttributeValue(\"EcologyClass\"))", target = "ecologyClass"),
            @Mapping(expression = "java(item.getAttributeValue(\"RegDocSeriesNumber\"))", target = "stsSeriesNumber"),
            @Mapping(expression = "java(item.getAttributeValue(\"PTSType\"))", target = "ptsType"),
            @Mapping(expression = "java(item.getAttributeValue(\"PTSNum\"))", target = "ptsNum"),
            @Mapping(expression = "java(item.getAttributeValue(\"VIN_norm\"))", target = "vinNorm"),
            @Mapping(expression = "java(item.getAttributeValue(\"VIN2_norm\"))", target = "vin2Norm"),
            @Mapping(expression = "java(item.getAttributeValue(\"ChassisNumber_norm\"))", target = "chassisNumberNorm"),
            @Mapping(expression = "java(item.getAttributeValue(\"CarcaseNumber_norm\"))", target = "carcaseNumberNorm")
    })
    VehicleInfo toVehicleInfo(NsiDictionaryItem item);

    @Mappings({
            @Mapping(expression = "java(item.getAttributeValue(\"OwnerType\"))", target = "ownerType"),
            @Mapping(expression = "java(item.getAttributeValue(\"DateStart\"))", target = "dateStart"),
            @Mapping(expression = "java(item.getAttributeValue(\"DateEnd\"))", target = "dateEnd")
    })
    OwnerPeriod toOwnerPeriod(NsiDictionaryItem item);

    @Mappings({
            @Mapping(expression = "java(item.getAttributeValue(\"RegActionName\"))", target = "regActionName"),
            @Mapping(expression = "java(item.getAttributeValue(\"RegDate\"))", target = "regDate")

    })
    RegAction toRegAction(NsiDictionaryItem item);

    @Mappings({
            @Mapping(expression = "java(item.getAttributeValue(\"RestrictionType\"))", target = "restrictionType"),
            @Mapping(expression = "java(ru.gosuslugi.pgu.pgu_common.gibdd.util.VehicleInfoMapperUtil.getRestrictionStatusAsText(item.getAttributeValue(\"Status\")))", target = "status"),
            @Mapping(expression = "java(item.getAttributeValue(\"Status\"))", target = "statusIntValue"),
            @Mapping(expression = "java(ru.gosuslugi.pgu.pgu_common.gibdd.util.DateUtils.formatDate(item.getAttributeValue(\"RestrictionDate\")))", target = "restrictionDate"),
            @Mapping(expression = "java(item.getAttributeValue(\"InitiateRegion\"))", target = "initiateRegion"),
            @Mapping(expression = "java(item.getAttributeValue(\"GIBDDDepart\"))", target = "gibddDepart"),
            @Mapping(expression = "java(item.getAttributeValue(\"MainReason\"))", target = "mainReason"),
            @Mapping(expression = "java(item.getAttributeValue(\"RestrictionDesc\"))", target = "restrictionDesc"),
            @Mapping(expression = "java(item.getAttributeValue(\"EnforcementProceedingsNumber\"))", target = "enforcementProceedingsNumber"),
            @Mapping(expression = "java(ru.gosuslugi.pgu.pgu_common.gibdd.util.DateUtils.formatDate(item.getAttributeValue(\"EnforcementProceedingsDate\")))", target = "enforcementProceedingsDate"),
            @Mapping(expression = "java(item.getAttributeValue(\"EnforcementProceedingsName\"))", target = "enforcementProceedingsName")
    })
    RestrictionsInformation toRestrictionsInformation(NsiDictionaryItem item);
}
