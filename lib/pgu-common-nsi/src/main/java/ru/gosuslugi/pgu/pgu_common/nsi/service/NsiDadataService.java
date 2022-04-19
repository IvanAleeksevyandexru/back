package ru.gosuslugi.pgu.pgu_common.nsi.service;


import ru.gosuslugi.pgu.pgu_common.nsi.dto.DadataAddressResponse;

/**
 * Сервис для работы с сервисом Dadata
 */
public interface NsiDadataService {
    DadataAddressResponse getAddress(String address);

    /**
     * Специальный метод очистки в случае, когда окато еще не известен
     * @see <a href="https://jira.egovdev.ru/browse/EPGUCORE-52512">EPGUCORE-52512</a>
     *
     * @param address адрес
     * @param okato ОКАТО
     * @return результат очистки
     */
    DadataAddressResponse getAddress(String address, String okato);

    DadataAddressResponse getAddressByFiasCode(String fiasCode);

    String getAddressOkato(String address);
}
