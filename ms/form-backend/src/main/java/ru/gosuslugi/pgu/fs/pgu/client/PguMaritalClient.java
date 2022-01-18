package ru.gosuslugi.pgu.fs.pgu.client;




import ru.gosuslugi.pgu.fs.pgu.dto.MaritalResponseItem;

import java.util.List;

/**
 * Клиент для получения свидетельства о браке из ЛК
 */
public interface PguMaritalClient {
    List<MaritalResponseItem> getMaritalStatusCertificate(String token, Long oid, String documentType);
}

