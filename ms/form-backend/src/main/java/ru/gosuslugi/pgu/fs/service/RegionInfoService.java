package ru.gosuslugi.pgu.fs.service;

import ru.gosuslugi.pgu.fs.component.dictionary.dto.kindergarten.EpguRegionResponse;

public interface RegionInfoService {
    /**
     * Определение версии СМЭВ через вызов api/nsi/v1/epgu/region/
     *
     * @param regionCode  код региона
     * @param serviceCode код услуги
     * @return версия СМЭВ
     */
    int getIntegerSmevVersion(String regionCode, String serviceCode);

    /**
     * Определение версии СМЭВ через вызов api/nsi/v1/epgu/region/
     *
     * @param regionCode  код региона
     * @param serviceCode код услуги
     * @return информация по услуге по
     */
    EpguRegionResponse getRegion(String regionCode, String serviceCode, int timeout);
}
