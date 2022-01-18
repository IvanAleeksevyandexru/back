package ru.gosuslugi.pgu.pgu_common.gibdd.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;
import ru.gosuslugi.pgu.pgu_common.gibdd.dto.Owner;
import ru.gosuslugi.pgu.pgu_common.gibdd.dto.RegAction;
import ru.gosuslugi.pgu.pgu_common.gibdd.dto.RestrictionsInformation;
import ru.gosuslugi.pgu.pgu_common.gibdd.dto.SearchingSpec;
import ru.gosuslugi.pgu.pgu_common.gibdd.dto.VehicleFullInfo;
import ru.gosuslugi.pgu.pgu_common.nsi.dto.NsiDictionaryItem;

@Mapper
public interface VehicleFullInfoMapper {

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
        @Mapping(expression = "java(item.getAttributeValue(\"UniqueRowNumber\"))", target = "uniqueRowNumber"),
        @Mapping(expression = "java(item.getAttributeValue(\"VIN2\"))", target = "vin2"),
        @Mapping(expression = "java(item.getAttributeValue(\"Manufacturer\"))", target = "manufacturer"),
        @Mapping(expression = "java(item.getAttributeValue(\"Price\"))", target = "price"),
        @Mapping(expression = "java(item.getAttributeValue(\"MaxWeight\"))", target = "maxWeight"),
        @Mapping(expression = "java(item.getAttributeValue(\"WeightWithoutLoading\"))", target = "weightWithoutLoading"),
        @Mapping(expression = "java(item.getAttributeValue(\"WheelLocation\"))", target = "wheelLocation"),
        @Mapping(expression = "java(ru.gosuslugi.pgu.pgu_common.gibdd.util.VehicleInfoMapperUtil.getWheelLocationDesc(item.getAttributeValue(\"WheelLocation\")))", target = "wheelLocationDesc"),
        @Mapping(expression = "java(item.getAttributeValue(\"TransmissionType\"))", target = "transmissionType"),
        @Mapping(expression = "java(ru.gosuslugi.pgu.pgu_common.gibdd.util.VehicleInfoMapperUtil.getTransmissionType(item.getAttributeValue(\"TransmissionType\")))", target = "transmissionTypeDesc"),
        @Mapping(expression = "java(item.getAttributeValue(\"DriveUnitType\"))", target = "driveUnitType"),
        @Mapping(expression = "java(ru.gosuslugi.pgu.pgu_common.gibdd.util.VehicleInfoMapperUtil.getDriveUnitType(item.getAttributeValue(\"DriveUnitType\")))", target = "driveUnitTypeDesc"),
        @Mapping(expression = "java(item.getAttributeValue(\"VehicleTypeTAM\"))", target = "vehicleTypeTAM"),
        @Mapping(expression = "java(item.getAttributeValue(\"EcologyClass\"))", target = "ecologyClass"),
        @Mapping(expression = "java(item.getAttributeValue(\"SpecTargetName\"))", target = "specTargetName"),
        @Mapping(expression = "java(item.getAttributeValue(\"EngineType\"))", target = "engineType"),
        @Mapping(expression = "java(item.getAttributeValue(\"EngineVolume\"))", target = "engineVolume"),
        @Mapping(expression = "java(item.getAttributeValue(\"EngineModel\"))", target = "engineModel"),
        @Mapping(expression = "java(item.getAttributeValue(\"EngineNum\"))", target = "engineNum"),
        @Mapping(expression = "java(item.getAttributeValue(\"EnginePowerHorse\"))", target = "enginePowerHorse"),
        @Mapping(expression = "java(item.getAttributeValue(\"EnginePowerkVt\"))", target = "enginePowerVt"),
        @Mapping(expression = "java(item.getAttributeValue(\"EndRegDate\"))", target = "endRegDate"),
        @Mapping(expression = "java(item.getAttributeValue(\"ApproveDate\"))", target = "approveDate"),
        @Mapping(expression = "java(item.getAttributeValue(\"ApproveSerNum\"))", target = "approveSerNum"),
        @Mapping(expression = "java(item.getAttributeValue(\"ApproveIssueBy\"))", target = "approveIssueBy"),
        @Mapping(expression = "java(item.getAttributeValue(\"UtilizStatus\"))", target = "utilizStatus"),
        @Mapping(expression = "java(item.getAttributeValue(\"TDTPO\"))", target = "tdtpo"),
        @Mapping(expression = "java(item.getAttributeValue(\"TDTPOIssueDate\"))", target = "tdtpoIssueDate"),
        @Mapping(expression = "java(item.getAttributeValue(\"TDTPOIssueBy\"))", target = "tdtpoIssueBy"),
        @Mapping(expression = "java(item.getAttributeValue(\"Restrictions\"))", target = "customsRestrictions"),
        @Mapping(expression = "java(item.getAttributeValue(\"IssueCountryCode\"))", target = "issueCountryCode"),
        @Mapping(expression = "java(item.getAttributeValue(\"CountryName\"))", target = "countryName"),
        @Mapping(expression = "java(item.getAttributeValue(\"RegDocumentType\"))", target = "stsDocumentType"),
        @Mapping(expression = "java(item.getAttributeValue(\"RegDocSeriesNumber\"))", target = "stsSeriesNumber"),
        @Mapping(expression = "java(ru.gosuslugi.pgu.pgu_common.gibdd.util.VehicleInfoMapperUtil.getFormattedNumber(item.getAttributeValue(\"RegDocSeriesNumber\")))", target = "stsSeriesNumberFormatted"),
        @Mapping(expression = "java(ru.gosuslugi.pgu.pgu_common.gibdd.util.DateUtils.formatDate(item.getAttributeValue(\"RegDocDocumentDate\")))", target = "stsDocumentDate"),
        @Mapping(expression = "java(item.getAttributeValue(\"RegDocIssueAgency\"))", target = "stsIssueAgency"),
        @Mapping(expression = "java(item.getAttributeValue(\"RegDocSpecialTags\"))", target = "stsSpecialTags"),
        @Mapping(expression = "java(item.getAttributeValue(\"PTSType\"))", target = "ptsType"),
        @Mapping(expression = "java(item.getAttributeValue(\"PTSNum\"))", target = "ptsNum"),
        @Mapping(expression = "java(ru.gosuslugi.pgu.pgu_common.gibdd.util.VehicleInfoMapperUtil.getFormattedNumber(item.getAttributeValue(\"PTSNum\")))", target = "ptsNumFormatted"),
        @Mapping(expression = "java(ru.gosuslugi.pgu.pgu_common.gibdd.util.DateUtils.formatDate(item.getAttributeValue(\"PTSRegDate\")))", target = "ptsRegDate"),
        @Mapping(expression = "java(item.getAttributeValue(\"PTSIssueAgency\"))", target = "ptsIssueAgency"),
        @Mapping(expression = "java(item.getAttributeValue(\"PTSSpecialTags\"))", target = "ptsSpecialTags")
    })
    VehicleFullInfo toVehicleInfo(NsiDictionaryItem item);

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
        @Mapping(expression = "java(item.getAttributeValue(\"RecordStatus\"))", target = "recordStatus"),
        @Mapping(expression = "java(item.getAttributeValue(\"VIN\"))", target = "vin"),
        @Mapping(expression = "java(item.getAttributeValue(\"ChassisNumber\"))", target = "chassisNumber"),
        @Mapping(expression = "java(item.getAttributeValue(\"CarcaseNumber\"))", target = "carcaseNumber"),
        @Mapping(expression = "java(item.getAttributeValue(\"VenicleType\"))", target = "vehicleType"),
        @Mapping(expression = "java(item.getAttributeValue(\"UniqueRowNumber\"))", target = "uniqueRowNumber"),
        @Mapping(expression = "java(item.getAttributeValue(\"VIN2\"))", target = "vin2"),
        @Mapping(expression = "java(item.getAttributeValue(\"Manufacturer\"))", target = "manufacturer"),
        @Mapping(expression = "java(item.getAttributeValue(\"MaxWeight\"))", target = "maxWeight"),
        @Mapping(expression = "java(item.getAttributeValue(\"WeightWithoutLoading\"))", target = "weightWithoutLoading"),
        @Mapping(expression = "java(item.getAttributeValue(\"WheelLocation\"))", target = "wheelLocation"),
        @Mapping(expression = "java(ru.gosuslugi.pgu.pgu_common.gibdd.util.VehicleInfoMapperUtil.getWheelLocationDesc(item.getAttributeValue(\"WheelLocation\")))", target = "wheelLocationDesc"),
        @Mapping(expression = "java(item.getAttributeValue(\"TransmissionType\"))", target = "transmissionType"),
        @Mapping(expression = "java(ru.gosuslugi.pgu.pgu_common.gibdd.util.VehicleInfoMapperUtil.getTransmissionType(item.getAttributeValue(\"TransmissionType\")))", target = "transmissionTypeDesc"),
        @Mapping(expression = "java(item.getAttributeValue(\"DriveUnitType\"))", target = "driveUnitType"),
        @Mapping(expression = "java(ru.gosuslugi.pgu.pgu_common.gibdd.util.VehicleInfoMapperUtil.getDriveUnitType(item.getAttributeValue(\"DriveUnitType\")))", target = "driveUnitTypeDesc"),
        @Mapping(expression = "java(item.getAttributeValue(\"VehicleTypeTAM\"))", target = "vehicleTypeTAM"),
        @Mapping(expression = "java(item.getAttributeValue(\"EcologyClass\"))", target = "ecologyClass"),
        @Mapping(expression = "java(item.getAttributeValue(\"SpecTargetName\"))", target = "specTargetName"),
        @Mapping(expression = "java(item.getAttributeValue(\"EngineType\"))", target = "engineType"),
        @Mapping(expression = "java(item.getAttributeValue(\"EngineVolume\"))", target = "engineVolume"),
        @Mapping(expression = "java(item.getAttributeValue(\"EngineModel\"))", target = "engineModel"),
        @Mapping(expression = "java(item.getAttributeValue(\"EngineNum\"))", target = "engineNum"),
        @Mapping(expression = "java(item.getAttributeValue(\"EnginePowerHorse\"))", target = "enginePowerHorse"),
        @Mapping(expression = "java(item.getAttributeValue(\"EnginePowerkVt\"))", target = "enginePowerVt"),
        @Mapping(expression = "java(item.getAttributeValue(\"EndRegDate\"))", target = "endRegDate"),
        @Mapping(expression = "java(item.getAttributeValue(\"ApproveDate\"))", target = "approveDate"),
        @Mapping(expression = "java(item.getAttributeValue(\"ApproveSerNum\"))", target = "approveSerNum"),
        @Mapping(expression = "java(item.getAttributeValue(\"ApproveIssueBy\"))", target = "approveIssueBy"),
        @Mapping(expression = "java(item.getAttributeValue(\"UtilizStatus\"))", target = "utilizStatus"),
        @Mapping(expression = "java(item.getAttributeValue(\"TDTPO\"))", target = "tdtpo"),
        @Mapping(expression = "java(item.getAttributeValue(\"TDTPOIssueDate\"))", target = "tdtpoIssueDate"),
        @Mapping(expression = "java(item.getAttributeValue(\"TDTPOIssueBy\"))", target = "tdtpoIssueBy"),
        @Mapping(expression = "java(item.getAttributeValue(\"Restrictions\"))", target = "customsRestrictions"),
        @Mapping(expression = "java(item.getAttributeValue(\"IssueCountryCode\"))", target = "issueCountryCode"),
        @Mapping(expression = "java(item.getAttributeValue(\"CountryName\"))", target = "countryName")
    })
    VehicleFullInfo toProtectedVehicleInfo(NsiDictionaryItem item);

    @Mappings({
        @Mapping(expression = "java(item.getAttributeValue(\"RegActionName\"))", target = "regActionName"),
        @Mapping(expression = "java(item.getAttributeValue(\"RegDepartment\"))", target = "regDepartment"),
        @Mapping(expression = "java(item.getAttributeValue(\"RegDate\"))", target = "regDate"),
        @Mapping(expression = "java(Boolean.valueOf(item.getAttributeValues().getOrDefault(\"ConfidentSign\", \"\").toString()))", target = "confidentSign"),
        @Mapping(expression = "java(ru.gosuslugi.pgu.pgu_common.gibdd.util.VehicleInfoMapperUtil.getLeasingFlagAsText(item.getAttributeValue(\"LeasingFlag\")))", target = "leasingFlag")
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
        @Mapping(expression = "java(item.getAttributeValue(\"EnforcementProceedingsName\"))", target = "enforcementProceedingsName"),
        @Mapping(expression = "java(item.getAttributeValue(\"DocumentAgency\"))", target = "documentAgency"),
        @Mapping(expression = "java(item.getAttributeValue(\"EnforcementProceedingsDocumentNumber\"))", target = "enforcementProceedingsDocumentNumber"),
        @Mapping(expression = "java(item.getAttributeValue(\"EnforcementProceedingsIssueDate\"))", target = "enforcementProceedingsIssueDate"),
        @Mapping(expression = "java(item.getAttributeValue(\"OSPaddress\"))", target = "ospAddress"),
    })
    RestrictionsInformation toRestrictionsInformation(NsiDictionaryItem item);

    @Mappings({
        @Mapping(expression = "java(item.getAttributeValue(\"RestrictionType\"))", target = "restrictionType"),
        @Mapping(expression = "java(ru.gosuslugi.pgu.pgu_common.gibdd.util.VehicleInfoMapperUtil.getRestrictionStatusAsText(item.getAttributeValue(\"Status\")))", target = "status"),
        @Mapping(expression = "java(item.getAttributeValue(\"Status\"))", target = "statusIntValue"),
        @Mapping(expression = "java(ru.gosuslugi.pgu.pgu_common.gibdd.util.DateUtils.formatDate(item.getAttributeValue(\"RestrictionDate\")))", target = "restrictionDate"),
        @Mapping(expression = "java(item.getAttributeValue(\"InitiateRegion\"))", target = "initiateRegion"),
        @Mapping(expression = "java(item.getAttributeValue(\"MainReason\"))", target = "mainReason"),
        @Mapping(expression = "java(item.getAttributeValue(\"RestrictionDesc\"))", target = "restrictionDesc"),
        @Mapping(expression = "java(item.getAttributeValue(\"EnforcementProceedingsNumber\"))", target = "enforcementProceedingsNumber"),
        @Mapping(expression = "java(ru.gosuslugi.pgu.pgu_common.gibdd.util.DateUtils.formatDate(item.getAttributeValue(\"EnforcementProceedingsDate\")))", target = "enforcementProceedingsDate"),
        @Mapping(expression = "java(item.getAttributeValue(\"EnforcementProceedingsName\"))", target = "enforcementProceedingsName"),
        @Mapping(expression = "java(item.getAttributeValue(\"DocumentAgency\"))", target = "documentAgency"),
        @Mapping(expression = "java(item.getAttributeValue(\"EnforcementProceedingsDocumentNumber\"))", target = "enforcementProceedingsDocumentNumber"),
        @Mapping(expression = "java(item.getAttributeValue(\"EnforcementProceedingsIssueDate\"))", target = "enforcementProceedingsIssueDate"),
        @Mapping(expression = "java(item.getAttributeValue(\"OSPaddress\"))", target = "ospAddress"),
    })
    RestrictionsInformation toProtectedRestrictionsInformation(NsiDictionaryItem item);

    @Mappings({
        @Mapping(expression = "java(item.getAttributeValue(\"LastName\"))", target = "lastName"),
        @Mapping(expression = "java(item.getAttributeValue(\"FirstName\"))", target = "firstName"),
        @Mapping(expression = "java(item.getAttributeValue(\"MiddleName\"))", target = "middleName"),
        @Mapping(expression = "java(item.getAttributeValue(\"BirthDay\"))", target = "birthDay"),
        @Mapping(expression = "java(item.getAttributeValue(\"BirthPlace\"))", target = "birthPlace"),
        @Mapping(expression = "java(item.getAttributeValue(\"INN\"))", target = "inn"),
        @Mapping(expression = "java(item.getAttributeValue(\"OGRNIP\"))", target = "ogrnip"),
        @Mapping(expression = "java(item.getAttributeValue(\"SNILS\"))", target = "snils"),
        @Mapping(expression = "java(item.getAttributeValue(\"RegAddress\"))", target = "regAddress"),
        @Mapping(expression = "java(item.getAttributeValue(\"IDDocumentType\"))", target = "idDocumentType"),
        @Mapping(expression = "java(item.getAttributeValue(\"DocumentNumSer\"))", target = "documentNumSer"),
        @Mapping(expression = "java(ru.gosuslugi.pgu.pgu_common.gibdd.util.VehicleInfoMapperUtil.getFormattedNumber(item.getAttributeValue(\"DocumentNumSer\")))", target = "documentNumSerFormatted"),
        @Mapping(expression = "java(item.getAttributeValue(\"DocumentDate\"))", target = "documentDate"),
        @Mapping(expression = "java(item.getAttributeValue(\"IssueAgency\"))", target = "issueAgency")
    })
    Owner toOwner(NsiDictionaryItem item);

    @Mappings({
        @Mapping(expression = "java(Boolean.valueOf(item.getAttributeValues().getOrDefault(\"SearchingSpecFlag\", \"\").toString()))", target = "searchingSpecFlag"),
        @Mapping(expression = "java(item.getAttributeValue(\"SpecProductType\"))", target = "specProductType"),
        @Mapping(expression = "java(ru.gosuslugi.pgu.pgu_common.gibdd.util.DateUtils.formatDate(item.getAttributeValue(\"OperationDate\")))", target = "operationDate"),
        @Mapping(expression = "java(item.getAttributeValue(\"TechOperation\"))", target = "techOperation"),
        @Mapping(expression = "java(item.getAttributeValue(\"SubDivision\"))", target = "subDivision")
    })
    SearchingSpec toSearchingSpec(NsiDictionaryItem item);
}
