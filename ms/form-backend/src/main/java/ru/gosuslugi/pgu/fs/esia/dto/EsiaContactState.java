package ru.gosuslugi.pgu.fs.esia.dto;

import lombok.Data;

@Data
public class EsiaContactState {

    private boolean state;

    private boolean result;

    private String verifiedOn;
}
