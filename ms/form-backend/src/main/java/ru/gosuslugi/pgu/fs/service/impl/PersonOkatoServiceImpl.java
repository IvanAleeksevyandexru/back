package ru.gosuslugi.pgu.fs.service.impl;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.context.annotation.RequestScope;
import ru.atc.carcass.security.rest.model.EsiaAddress;
import ru.gosuslugi.pgu.common.core.service.OkatoHolder;
import ru.gosuslugi.pgu.common.esia.search.dto.UserPersonalData;
import ru.gosuslugi.pgu.fs.service.PersonOkatoService;
import ru.gosuslugi.pgu.pgu_common.nsi.dto.DadataAddressResponse;
import ru.gosuslugi.pgu.pgu_common.nsi.service.NsiDadataService;

import java.util.Comparator;
import java.util.Optional;

import static java.util.Objects.isNull;

/**
 * Вычисление ОКАТО по адресам из профиля пользователя
 * Механизм взят из ленивого Singleton-а в многопоточной среде
 * @see <a href="https://habr.com/ru/post/129494/">Правильный Singleton в Java</a>
 */
@Slf4j
@Component
@RequestScope
public class PersonOkatoServiceImpl implements OkatoHolder, PersonOkatoService {

    private final UserPersonalData userPersonalData;
    private final NsiDadataService nsiDadataService;

    public PersonOkatoServiceImpl(UserPersonalData userPersonalData, NsiDadataService nsiDadataService) {
        this.userPersonalData = userPersonalData;
        this.nsiDadataService = nsiDadataService;
    }

    /** Вычисляемый ОКАТО: Singleton значение */
    private volatile String instance;

    @Override
    public String getOkato() {
        String result = instance;
        if (isNull(result)) {
            synchronized (this) {
                result = instance;
                if (isNull(result)) {
                    instance = result = calculate();
                }
            }
        }
        return result;
    }

    /**
     * Пакетный доступ для тестирования
     * @return ненулевое вычисленное значение
     */
    String calculate() {
        String result = null;
        Optional<EsiaAddress> esiaAddress = getEsiaAddress();
        if (esiaAddress.isPresent()) {
            String address = esiaAddress.get().getAddressStr();
            try {
                DadataAddressResponse addressResponse = nsiDadataService.getAddress(address, DEFAULT_OKATO);
                result = addressResponse.getOkato();
            } catch (Exception e) {
                if (log.isWarnEnabled()) {
                    log.warn("При очистке адреса \"" + address + "\" произошла ошибка", e);
                }
            }
        }
        if (isNull(result)) {
            result = OkatoHolder.DEFAULT_OKATO;
        }
        return result;
    }

    private Optional<EsiaAddress> getEsiaAddress() {
        return userPersonalData.getAddresses().stream()
                .filter(
                        a ->
                                a.getType().equals(EsiaAddress.Type.REGISTRATION.getCode())
                                        || a.getType().equals(EsiaAddress.Type.LOCATION.getCode())
                )
                // REGISTRATION to first (Сортируем так, чтобы регистрационные адреса были выбраны первыми)
                .sorted(Comparator.comparingInt(a -> a.getType().equals(EsiaAddress.Type.REGISTRATION.getCode()) ? 0 : 1))
                .findFirst();
    }
}
