package ru.gosuslugi.pgu.fs.utils;

import lombok.experimental.UtilityClass;
import ru.atc.carcass.security.rest.model.person.Kids;
import ru.atc.carcass.security.rest.model.person.PersonDoc;
import ru.gosuslugi.pgu.common.core.date.util.DateUtil;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static ru.gosuslugi.pgu.components.ComponentAttributes.*;

/** Вспомогательный класс для методов в компонентах, связанных с детьми. */
@UtilityClass
public class ChildrenInfoUtils {

    /**
     * Получает информацию по ребёнку и ЕСИА и превращает в Map.
     *
     * @param kid данные по ребёнку из ЕСИА
     * @return мапка, где ключ - это наименование атрибута, значение - информация по полю из ЕСИА
     */
    public static Map<String, Object> getKidInfo(Kids kid) {
        Map<String, Object> kidInfo = new HashMap<>();
        kidInfo.put(CHILDREN_ID_ATTR, kid.getId());
        kidInfo.put(CHILDREN_IS_NEW_ATTR, false);
        kidInfo.put(CHILDREN_FIRST_NAME_ATTR, kid.getFirstName());
        kidInfo.put(CHILDREN_MIDDLE_NAME_ATTR, Optional.ofNullable(kid.getMiddleName()).orElse(""));
        kidInfo.put(CHILDREN_LAST_NAME_ATTR, kid.getLastName());
        kidInfo.put(CHILDREN_BIRTH_DATE_ATTR, DateUtil.toOffsetDateTimeString(kid.getBirthDate(), DateUtil.ESIA_DATE_FORMAT));
        kidInfo.put(CHILDREN_GENDER_ATTR, kid.getGender());
        kidInfo.put(SNILS, kid.getSnils());
        Optional<PersonDoc> brthCertOptional = kid.getDocuments().getDocs().stream().filter(x -> x.getType().contains("BRTH_CERT")).findFirst();
        if (brthCertOptional.isPresent()) {
            PersonDoc birthCert = brthCertOptional.get();
            kidInfo.put(CHILDREN_RF_BIRTH_CERTIFICATE_SERIES_ATTR, birthCert.getSeries());
            kidInfo.put(CHILDREN_RF_BIRTH_CERTIFICATE_NUMBER_ATTR, birthCert.getNumber());
            kidInfo.put(CHILDREN_RF_BIRTH_CERTIFICATE_ACT_NUMBER_ATTR, birthCert.getActNo() == null ? "-" : birthCert.getActNo());
            kidInfo.put(CHILDREN_RF_BIRTH_CERTIFICATE_ISSUE_DATE_ATTR, DateUtil.toOffsetDateTime(birthCert.getIssueDate(), DateUtil.ESIA_DATE_FORMAT));
            kidInfo.put(CHILDREN_RF_BIRTH_CERTIFICATE_ISSUED_BY_ATTR, birthCert.getIssuedBy());
            kidInfo.put(CHILDREN_ACT_DATE_ATTR, birthCert.getActDate());
            kidInfo.put(CHILDREN_BIRTH_CERTIFICATE_TYPE, birthCert.getType());
        }
        Optional<PersonDoc> rfPassportOptional = kid.getDocuments().getDocs().stream().filter(x -> x.getType().equals(RF_PASSPORT_ATTR)).findFirst();
        if (rfPassportOptional.isPresent()) {
            PersonDoc rfPassport = rfPassportOptional.get();
            kidInfo.put(RF_PASSPORT_SERIES_ATTR, rfPassport.getSeries());
            kidInfo.put(RF_PASSPORT_NUMBER_ATTR, rfPassport.getNumber());
            kidInfo.put(CHILDREN_RF_BIRTH_CERTIFICATE_ACT_NUMBER_ATTR, rfPassport.getActNo() == null ? "-" : rfPassport.getActNo());
            kidInfo.put(RF_PASSPORT_ISSUE_DATE_ATTR, rfPassport.getIssueDate());
            kidInfo.put(RF_PASSPORT_ISSUED_BY_ID_ATTR, rfPassport.getIssueId());
            kidInfo.put(CHILDREN_ACT_DATE_ATTR, rfPassport.getActDate());
        }

        Optional<PersonDoc> omsOptional = kid.getDocuments().getDocs().stream().filter(x -> x.getType().equals(MDCL_PLCY_ATTR)).findFirst();
        if (omsOptional.isPresent()) {
            PersonDoc oms = omsOptional.get();
            kidInfo.put(CHILDREN_OMS_SERIES_ATTR, oms.getSeries());
            kidInfo.put(CHILDREN_OMS_NUMBER_ATTR, oms.getNumber());
        }

        kidInfo.put(CHILDREN_RELATION_ATTR, "");
        kidInfo.put(REGISTRATION_ATTR, "");
        kidInfo.put(REGISTRATION_DATE_ATTR, "");
        return kidInfo;
    }
}
