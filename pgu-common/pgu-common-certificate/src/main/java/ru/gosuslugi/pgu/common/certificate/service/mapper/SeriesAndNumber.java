package ru.gosuslugi.pgu.common.certificate.service.mapper;

import lombok.Getter;

@Getter
public class SeriesAndNumber {
    private final String series;
    private final String number;

    public SeriesAndNumber(String series, String number) {
        this.series = series;
        this.number = number;
    }
}
