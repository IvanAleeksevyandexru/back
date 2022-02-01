package ru.gosuslugi.pgu.fs.pgu.client;

import ru.gosuslugi.pgu.fs.pgu.dto.PguServiceCodes;

public interface PguUtilsClient {
    boolean checkAuthorityForService(String authorityId, String targetId);

    /**
     * Получить по serviceId и targetId значения соответствующие значения EPGU
     * @return объект {@link PguServiceCodes} с 2 полями passport и target, которые нужны для кооректных запросов в ЛК
     */
    PguServiceCodes getPguServiceCodes(String serviceCode, String targetCode);
}
