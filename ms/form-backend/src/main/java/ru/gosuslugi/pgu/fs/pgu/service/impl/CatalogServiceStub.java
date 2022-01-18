package ru.gosuslugi.pgu.fs.pgu.service.impl;

import ru.gosuslugi.pgu.dto.catalog.CatalogInfoDtoList;
import ru.gosuslugi.pgu.fs.pgu.service.CatalogService;

public class CatalogServiceStub implements CatalogService {
    @Override
    public CatalogInfoDtoList getCatalogInfoByTargetId(String targetId) {
        return new CatalogInfoDtoList();
    }
}
