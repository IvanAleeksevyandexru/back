package ru.gosuslugi.pgu.fs.esia.dto;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class EsiaConfirmContactDto {

    private String cnfCode;

    private String type;

    private List<String> stateFacts = new ArrayList<>();
}
