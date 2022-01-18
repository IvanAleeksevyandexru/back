package ru.gosuslugi.pgu.fs.pgu.service;

import ru.gosuslugi.pgu.dto.catalog.CatalogInfoDtoList;

public interface CatalogService {

    CatalogInfoDtoList getCatalogInfoByTargetId(String targetId);
}
