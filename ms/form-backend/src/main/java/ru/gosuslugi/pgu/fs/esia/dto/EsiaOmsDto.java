package ru.gosuslugi.pgu.fs.esia.dto;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class EsiaOmsDto {

    private List<String> stateFacts = new ArrayList<>();

    Long id;

    String type = "MDCL_PLCY";

    String vrfStu = "NOT_VERIFIED";

    String number;

    String etag;


}
