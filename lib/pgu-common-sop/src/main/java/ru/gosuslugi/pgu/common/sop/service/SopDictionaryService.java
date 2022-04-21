package ru.gosuslugi.pgu.common.sop.service;

import ru.gosuslugi.pgu.common.sop.dto.SopDictionaryRequest;
import ru.gosuslugi.pgu.common.sop.dto.SopDictionaryResponse;

/**
 * REST API сервис, предоставляющий справочные сведения по поисковому запросу из ЕСНСИ – системы.
 * @see <a href="https://confluence.egovdev.ru/pages/viewpage.action?pageId=173634888">СОП - инструкция по применению.</a>
 */
public interface SopDictionaryService {

    /**
     * Поиск элемента в справочнике по имени справочника и атрибуту.
     * @param sopDictionaryRequest тело запроса к сервису облачных подсказок (СОП).
     * @param okato ОКАТО
     * @return найденный элемент справочника со всеми атрибутами
     */
    SopDictionaryResponse findByRequest(SopDictionaryRequest sopDictionaryRequest, String okato);
}
