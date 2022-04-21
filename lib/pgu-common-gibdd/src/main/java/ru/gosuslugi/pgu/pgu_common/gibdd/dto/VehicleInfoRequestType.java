package ru.gosuslugi.pgu.pgu_common.gibdd.dto;

/**
 * https://jira.egovdev.ru/browse/EPGUCORE-90200 - расширение для 1.4+
 */
public enum VehicleInfoRequestType {

    VIN("VIN"),
    ChassisNumber("ChassisNumber"),
    CarcaseNumber("CarcaseNumber"),
    GovRegNumber("GovRegNumber"),
    GRZ_RegistrationDocNumber("GRZ_RegistrationDocNumber");

    private String id;

    VehicleInfoRequestType(String id) {
        this.id = id;
    }
    public String getId() {
        return id;
    }
}
