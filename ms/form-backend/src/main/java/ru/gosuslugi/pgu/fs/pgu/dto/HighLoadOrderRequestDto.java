package ru.gosuslugi.pgu.fs.pgu.dto;

import lombok.Data;

@Data
public class HighLoadOrderRequestDto {

    private String userSelectedRegion;

    private String sourceSystem;

    private String location;

    private String eServiceCode;

    private String serviceTargetCode;

    private String orderType = "ORDER";

    private Long orderStatusId = 17L;

    private String eServiceFullName;

    private String eServiceStateStructureId;

    private String eServiceStateStructureName;

    private String eserviceAttrServiceNameForOrder;

    private String eServicePassportExtId;

    private String eServiceAttrEpguCode;

    private String userId;

    private Boolean eserviceAttrDeleteDraft = false;

}
