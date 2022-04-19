package ru.gosuslugi.pgu.pgu_common.gibdd.dto;

import lombok.Getter;

@Getter
public enum OwnerDocumentType {
    PASSPORT_RF(5),
    FID_DOC(10);

    private final Integer code;

    OwnerDocumentType(Integer code) {
        this.code = code;
    }
}
