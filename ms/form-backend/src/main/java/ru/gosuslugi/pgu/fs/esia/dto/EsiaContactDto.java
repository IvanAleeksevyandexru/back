package ru.gosuslugi.pgu.fs.esia.dto;

import lombok.Data;

@Data
public class EsiaContactDto {

    String etag;

    Long id;

    String type;

    String value;

    String verifyingValue;

    String vrfStu = "NOT_VERIFIED";

    String vrfValStu = "NOT_VERIFIED";
}
