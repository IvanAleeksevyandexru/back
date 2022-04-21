package ru.gosuslugi.pgu.fs.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import ru.atc.carcass.security.rest.model.person.Person;
import ru.gosuslugi.pgu.common.esia.search.dto.UserOrgData;
import ru.gosuslugi.pgu.common.esia.search.dto.UserPersonalData;
import ru.gosuslugi.pgu.fs.service.UserDataService;

import java.util.Objects;
import java.util.Optional;
import java.util.function.Supplier;

import static ru.gosuslugi.pgu.common.core.logger.LoggerUtil.debug;
import static ru.gosuslugi.pgu.common.core.logger.LoggerUtil.warn;

/**
 * Сервис, объединяющий логику обработки персональных данных пользователя и юр.лица.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserDataServiceImpl implements UserDataService {

    /**
     * Хранилище персональных данных пользователя
     */
    private final UserPersonalData userPersonalData;
    /**
     * Хранилище данных юридического лица
     */
    private final UserOrgData userOrgData;

    /**
     * Получение признака руководителя из ФЛ или ЮЛ хранилища данных.
     *
     * @return признак руководителя в виде строки {@code true} или {@code false}.
     */
    @Override
    public String isChief() {
        return checkOrgOrPersonal(this::getIsOrgChiefString, this::getIsChiefString);
    }

    /**
     * Возвращает данные руководителя, если в списке руководителей присутствует текущий пользователь
     *
     * @return {@link Optional} с данными пользователя
     */
    @Override
    public Optional<Person> searchChiefIdenticalToCurrentUser() {
        return userOrgData.getChiefs().stream()
                .filter(person -> userPersonalData.getUserId() == Long.parseLong(person.getUserId()))
                .findFirst();
    }

    /**
     * Возвращает данные первого попавшегося руководителя организации
     *
     * @return {@link Optional} с данными пользователя
     */
    @Override
    public Optional<Person> searchAnyChief() {
        return userOrgData.getChiefs().stream().findAny();
    }

    /**
     * Определяет, какой из supplier выполнить и вернуть из него результат - для руководителя или ФЛ.
     * Разделение идёт по сравнению userId.
     *
     * @param orgSupplier      supplier для руководителя
     * @param personalSupplier supplier для ФЛ или сотрудника организации
     * @param <T>              результат вычисления выбранного supplier, кладётся в возвращаемое значение
     * @return результат вычисления в одном из supplier
     */
    private <T> T checkOrgOrPersonal(Supplier<T> orgSupplier, Supplier<T> personalSupplier) {
        if (userPersonalData.getOrgId() != null && !CollectionUtils.isEmpty(userOrgData.getChiefs())) {
            return orgSupplier.get();
        }
        return personalSupplier.get();
    }

    /**
     * Получение признака руководителя из Person текущего пользователя
     *
     * @return Признак руководителя в строке в виде {@code true} или {@code false}.
     */
    // Приходится возвращать строку из-за приведения типа к строке в ConditionCheckerHelper#checkPredicate
    private String getIsChiefString() {
        if (Objects.nonNull(userPersonalData.getCurrentRole())
                && Objects.nonNull(userPersonalData.getCurrentRole().getChief())) {
            debug(log, () -> String.format("Get person chief from currentRole=%s, userId=%s", userPersonalData.getCurrentRole(), userPersonalData.getUserId()));
            return userPersonalData.getCurrentRole().getChief();
        }
        if (Objects.nonNull(userPersonalData.getPerson())
                && Objects.nonNull(userPersonalData.getPerson().isChief())) {
            debug(log, () -> String.format("Get person chief=%s, userId=%s",  userPersonalData.getPerson(), userPersonalData.getUserId()));
            return userPersonalData.getPerson().isChief().toString();
        }
        warn(log, () -> String.format("Chief attribute was null in user person and role with userId=%s", userPersonalData.getUserId()));
        return "false";
    }

    /**
     * Получение признака руководителя из Person руководителя.
     *
     * @return Признак руководителя в строке в виде {@code true} или {@code false}.
     */
    // Приходится возвращать строку из-за приведения типа к строке в ConditionCheckerHelper#checkPredicate
    private String getIsOrgChiefString() {
        if (!CollectionUtils.isEmpty(userOrgData.getChiefs())) {
            final Optional<Person> personOptional = searchChiefIdenticalToCurrentUser();

            if (personOptional.isPresent() && Objects.nonNull(personOptional.get().isChief())) {
                debug(log, () -> String.format("Get org person chief=%s, oid=%s",  userOrgData.getChiefs(), userOrgData.getOrg().getOid()));
                return personOptional.get().isChief().toString();
            }
        }
        if (Objects.nonNull(userOrgData.getOrgRole()) && Objects.nonNull(userOrgData.getOrgRole().getChief())) {
            debug(log, () -> String.format("Get org chief from orgRole=%s, oid=%s", userOrgData.getOrgRole(), userOrgData.getOrg().getOid()));
            return userOrgData.getOrgRole().getChief();
        }
        warn(log, () -> String.format("Chief attribute was null in org person and role with oid=%s", userOrgData.getOrg().getOid()));
        return "false";
    }
}
