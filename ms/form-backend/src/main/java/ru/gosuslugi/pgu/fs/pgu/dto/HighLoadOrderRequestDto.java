package ru.gosuslugi.pgu.fs.pgu.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class HighLoadOrderRequestDto {

    private String userSelectedRegion;

    private String sourceSystem;

    private String location;

    @JsonProperty("eServiceCode")
    private String eServiceCode;

    private String serviceTargetCode;

    private String orderType = "ORDER";

    private Long orderStatusId = 17L;

    private String eserviceFullName;

    private String eserviceStateStructureId;

    private String eserviceStateStructureName;

    private String eserviceStateOrgCode;

    private String eserviceAttrServiceNameForOrder;

    private String eservicePassportExtId;

    private String eserviceAttrEpguCode;

    private String userId;

    private Boolean eserviceAttrDeleteDraft = false;

    @JsonProperty("eserviceAttrPassCode")
    private String eserviceAttrPassCode;

}
