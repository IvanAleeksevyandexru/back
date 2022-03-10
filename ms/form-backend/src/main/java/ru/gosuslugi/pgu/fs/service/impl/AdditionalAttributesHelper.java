package ru.gosuslugi.pgu.fs.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;
import ru.atc.carcass.security.rest.model.EsiaContact;
import ru.atc.carcass.security.rest.model.orgs.OrgType;
import ru.gosuslugi.pgu.common.esia.search.utils.UserDataUtils;
import ru.gosuslugi.pgu.common.esia.search.dto.UserOrgData;
import ru.gosuslugi.pgu.common.esia.search.dto.UserPersonalData;
import ru.gosuslugi.pgu.dto.ApplicantAnswer;
import ru.gosuslugi.pgu.dto.ApplicantRole;
import ru.gosuslugi.pgu.dto.ScenarioDto;
import ru.gosuslugi.pgu.fs.common.service.UserCookiesService;
import ru.gosuslugi.pgu.fs.common.variable.ServiceIdVariable;
import ru.gosuslugi.pgu.fs.common.variable.TargetIdVariable;
import ru.gosuslugi.pgu.fs.service.EmpowermentService;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Supplier;

import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;
import static ru.gosuslugi.pgu.components.ComponentAttributes.BIRTHDATE_CODE_ATTR;
import static ru.gosuslugi.pgu.components.ComponentAttributes.BIRTH_DATE_ATTR;
import static ru.gosuslugi.pgu.components.ComponentAttributes.CITIZENSHIP_ATTR;
import static ru.gosuslugi.pgu.components.ComponentAttributes.CITIZENSHIP_CODE_ATTR;
import static ru.gosuslugi.pgu.components.ComponentAttributes.CONTACT_PHONE;
import static ru.gosuslugi.pgu.components.ComponentAttributes.EMAIL_ATTR;
import static ru.gosuslugi.pgu.components.ComponentAttributes.ESIA_CONTACT_PHONE;
import static ru.gosuslugi.pgu.components.ComponentAttributes.FIRST_NAME_ATTR;
import static ru.gosuslugi.pgu.components.ComponentAttributes.GENDER_ATTR;
import static ru.gosuslugi.pgu.components.ComponentAttributes.HOME_PHONE;
import static ru.gosuslugi.pgu.components.ComponentAttributes.LAST_NAME_ATTR;
import static ru.gosuslugi.pgu.components.ComponentAttributes.MIDDLE_NAME_ATTR;
import static ru.gosuslugi.pgu.components.ComponentAttributes.MOBILE_PHONE;
import static ru.gosuslugi.pgu.components.ComponentAttributes.ORG_INN_ATTR;
import static ru.gosuslugi.pgu.components.ComponentAttributes.REUSE_PAYMENT_UIN;
import static ru.gosuslugi.pgu.components.ComponentAttributes.SNILS;
import static ru.gosuslugi.pgu.components.ComponentAttributes.TIMEZONE_ATTR;
import static ru.gosuslugi.pgu.components.ComponentAttributes.VERIFIED_ATTR;

/**
 * Заполняет доп. атрибуты в черновике.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class AdditionalAttributesHelper {
    private final UserPersonalData userPersonalData;
    private final UserOrgData userOrgData;
    private final UserCookiesService userCookiesService;
    private final ServiceIdVariable serviceIdVariable;
    private final TargetIdVariable targetIdVariable;
    private final EmpowermentService empowermentService;

    /**
     * Property that gives opportunity to control SMEV env for request to send
     */
    static final String SMEV_ENV_ATTR_NAME = "smevEnv";
    static final String ORDER_ATTR_NAME = "orderId";
    static final String OID_ATTR_NAME = "oid";

    static final String MASTER_ORDER_ID_ATTR_NAME = "masterId";

    static final String LEG_ATTR = "leg";
    static final String LEG_CODE_ATTR = "legCode";
    static final String SERVICE_ID_ATTR_NAME = "serviceId";
    static final String TARGET_ID_ATTR_NAME = "targetId";
    static final String ORG_ID_ATTR = "orgId";
    public static final String ORG_TYPE_ATTR = "orgType";
    static final String USER_ORG_CHIEF_ATTR = "userOrgChief";
    static final String SYSTEM_AUTHORITY_ATTR_NAME = "systemAuthority";
    static final String ORG_EMPOWERMENTS_ATTR_NAME = "orgEmpowerments";
    static final String AUTHORITY_ID_ATTR_NAME = "authorityId";

    /**
     * Устанавливает/обновляет дополнительные атрибуты в DTO
     * @param scenarioDto DTO для обновления
     */
    public void updateAdditionalAttributes(ScenarioDto scenarioDto, String smevEnv) {
        log.info("Additional parameters preparation process started. User is main applicant: {}", isMainApplicant(scenarioDto));

        // общие атрибуты
        addNullableAttribute(scenarioDto, ORDER_ATTR_NAME, MASTER_ORDER_ID_ATTR_NAME, scenarioDto.getOrderId()::toString);
        addNullableAttribute(scenarioDto, OID_ATTR_NAME, userPersonalData.getUserId()::toString);
        if (Objects.nonNull(userPersonalData.getPerson())) {
            addNullableAttribute(scenarioDto, SNILS, userPersonalData.getPerson()::getSnils);
            addNullableAttribute(scenarioDto, FIRST_NAME_ATTR, userPersonalData.getPerson()::getFirstName);
            addNullableAttribute(scenarioDto, LAST_NAME_ATTR, userPersonalData.getPerson()::getLastName);
            addNullableAttribute(scenarioDto, MIDDLE_NAME_ATTR, userPersonalData.getPerson()::getMiddleName);
            addNullableAttribute(scenarioDto, BIRTH_DATE_ATTR, userPersonalData.getPerson()::getBirthDate);
            addNullableAttribute(scenarioDto, GENDER_ATTR, userPersonalData.getPerson()::getGender);
            addNullableAttribute(scenarioDto, CITIZENSHIP_ATTR, userPersonalData.getPerson()::getCitizenship);
            addNullableAttribute(scenarioDto, CITIZENSHIP_CODE_ATTR, userPersonalData.getPerson()::getCitizenshipCode);
            addNullableAttribute(scenarioDto, BIRTHDATE_CODE_ATTR, userPersonalData.getPerson()::getBirthCountryCode);
            addNullableAttribute(scenarioDto, AUTHORITY_ID_ATTR_NAME, userPersonalData::getAuthorityId);
        }
        addNullableAttribute(scenarioDto, TIMEZONE_ATTR, userCookiesService::getUserTimezone);

        addConditionalAttribute(scenarioDto, SYSTEM_AUTHORITY_ATTR_NAME, () -> !StringUtils.isEmpty(userOrgData.getSystemAuthority()), userOrgData::getSystemAuthority);
        // TODO: аккуратней с написанием Supplier-ов вида userPersonalData.getCurrentRole()::getChief
        // неявно будет вызываться requiredNonNull для userPersonalData.getCurrentRole() в рантайме и будем выхватывать внезапные NPE
        addConditionalAttribute(scenarioDto, USER_ORG_CHIEF_ATTR,
                () -> UserDataUtils.nonNullChief(userPersonalData, userOrgData),
                () -> String.valueOf(UserDataUtils.isChief(userPersonalData, userOrgData))
        );
        addConditionalAttribute(scenarioDto, ORG_TYPE_ATTR, () -> Objects.nonNull(userOrgData.getOrg()), this::getOrgType);
        addConditionalAttribute(scenarioDto, LEG_ATTR, () -> Objects.nonNull(userOrgData.getOrg()) && !StringUtils.isBlank(userOrgData.getOrg().getLeg()), () -> userOrgData.getOrg().getLeg());
        addConditionalAttribute(scenarioDto, LEG_CODE_ATTR, () -> Objects.nonNull(userOrgData.getOrg()) && !StringUtils.isBlank(userOrgData.getOrg().getLeg()), () -> userOrgData.getOrg().getLegCode());

        // атрибуты текущего пользователя
        // TODO вероятно, их тоже нужно тиражировать на основного заявителя, надо оценить сайдэффекты
        Map<String, String> additionalMap = scenarioDto.getAdditionalParameters();
        if (Objects.nonNull(userPersonalData.getPerson())) additionalMap.put(ORG_INN_ATTR, userPersonalData.getPerson().getInn());
        additionalMap.put(SERVICE_ID_ATTR_NAME, serviceIdVariable.getValue(scenarioDto));
        additionalMap.put(TARGET_ID_ATTR_NAME, targetIdVariable.getValue(scenarioDto));
        if (Objects.nonNull(userPersonalData.getOrgId())) {
            additionalMap.put(ORG_ID_ATTR, userPersonalData.getOrgId().toString());
        }
        if (Objects.nonNull(userOrgData.getOrg())
                && Objects.nonNull(userOrgData.getOrgRole())
                && !Boolean.parseBoolean(userOrgData.getOrgRole().getChief())
        ) {
            additionalMap.put(ORG_EMPOWERMENTS_ATTR_NAME, String.join(",", empowermentService.getUserEmpowerments()));
        }
        if(smevEnv!=null) {
            additionalMap.put(SMEV_ENV_ATTR_NAME, smevEnv);
        }
        String homePhone = getEsiaContactByType(EsiaContact.Type.PHONE.getCode());
        if (StringUtils.isNotBlank(homePhone)) additionalMap.put(HOME_PHONE, homePhone);

        String mobilePhone = getEsiaContactByType(EsiaContact.Type.MOBILE_PHONE.getCode());
        if (StringUtils.isNotBlank(mobilePhone)) additionalMap.put(MOBILE_PHONE, mobilePhone);

        String contactPhone = getEsiaContactByType(ESIA_CONTACT_PHONE);
        if (StringUtils.isNotBlank(contactPhone)) additionalMap.put(CONTACT_PHONE, contactPhone);


        // заполняем email
        Optional<EsiaContact> userEmail = userPersonalData.getContacts().stream()
                .filter(c -> (c.getType().equals(EsiaContact.Type.EMAIL.getCode())) && c.getVrfStu().equals(VERIFIED_ATTR))
                .findFirst();
        userEmail.ifPresent(esiaContact -> additionalMap.put(EMAIL_ATTR, esiaContact.getValue()));

        ApplicantAnswer reusePaymentUin = scenarioDto.getCachedAnswers().get(REUSE_PAYMENT_UIN);
        if (reusePaymentUin != null) {
            String uin = reusePaymentUin.getValue();
            additionalMap.put(REUSE_PAYMENT_UIN, uin);
        }

        scenarioDto.setAdditionalParameters(additionalMap);
    }

    /**
     * Добавляет значение атрибута без выполнения условий проверки. Название атрибута основного заявителя берется по
     * умолчанию
     * @param scenarioDto дто
     * @param attributeName название атрибута
     * @param attributeValue функция-значение атрибута
     * @see #addConditionalAttribute(ScenarioDto, String, Supplier, Supplier)
     */
    private void addNullableAttribute(ScenarioDto scenarioDto, String attributeName, Supplier<String> attributeValue) {
        addNullableAttribute(scenarioDto, attributeName, getMasterAttributeName(attributeName), attributeValue);
    }

    /**
     * Добавляет значение атрибута без выполнения условий проверки
     * @param scenarioDto дто
     * @param attributeName название атрибута
     * @param masterAttributeName название атрибута основного заявителя
     * @param attributeValue функция-значение атрибута
     * @see #addConditionalAttribute(ScenarioDto, String, Supplier, Supplier)
     */
    private void addNullableAttribute(ScenarioDto scenarioDto, String attributeName, String masterAttributeName, Supplier<String> attributeValue) {
        addConditionalAttribute(scenarioDto, attributeName, masterAttributeName, Boolean.TRUE::booleanValue, attributeValue);
    }

    /**
     * Добавляет значение атрибута, если выполняется условие проверки, передаваемое в параметрах
     * @param scenarioDto дто
     * @param attributeName название атрибута
     * @param condition функция-условие добавления значения атрибута
     * @param attributeValue функция-значение атрибута
     */
    private void addConditionalAttribute(ScenarioDto scenarioDto, String attributeName, Supplier<Boolean> condition, Supplier<String> attributeValue) {
        addConditionalAttribute(scenarioDto, attributeName, getMasterAttributeName(attributeName), condition, attributeValue);
    }

    /**
     * Добавляет значение атрибута, если выполняется условие проверки, передаваемое в параметрах. Название атрибута
     * основного заявителя берется по умолчанию
     * @param scenarioDto дто
     * @param attributeName название атрибута
     * @param masterAttributeName название атрибута основного заявителя
     * @param condition функция-условие добавления значения атрибута
     * @param attributeValue функция-значение атрибута
     */
    private void addConditionalAttribute(ScenarioDto scenarioDto, String attributeName, String masterAttributeName, Supplier<Boolean> condition, Supplier<String> attributeValue) {
        Map<String, String> additionalMap = Objects.requireNonNullElse(scenarioDto.getAdditionalParameters(), new HashMap<>());

        if(condition.get()) {
            additionalMap.put(attributeName, attributeValue.get());
        }
        if(isMainApplicant(scenarioDto) && condition.get()) {
            additionalMap.put(masterAttributeName, attributeValue.get());
        }
        scenarioDto.setAdditionalParameters(additionalMap);
    }

    /**
     * Проверяет, что текущий пользователь является основным заявителем
     * @param scenarioDto дто
     * @return {@code true}, если основной заявитель
     */
    private boolean isMainApplicant(ScenarioDto scenarioDto) {
        return !scenarioDto.getParticipants().containsKey(userPersonalData.getUserId().toString())
                || scenarioDto.getParticipants().get(userPersonalData.getUserId().toString()).getRole() == ApplicantRole.Applicant;
    }

    /**
     * Возвращает название атрибута для основного заявителя
     * @param attributeName название атрибута
     * @return название атрибута с префиксом {@code master}
     */
    String getMasterAttributeName(String attributeName) {
        return "master" + StringUtils.capitalize(attributeName);
    }

    /**
     * Тип юр. лица. Для AGENCY возвращаем LEGAL
     * @return Тип юр. лица
     */
    private String getOrgType() {
        OrgType orgType = userOrgData.getOrg().getType();
        if (userOrgData.getOrg().getType() == OrgType.AGENCY) {
            orgType = OrgType.LEGAL;
        }
        return String.valueOf(orgType);
    }

    public void fillUserData(ScenarioDto scenarioDto) {
        if (nonNull(userPersonalData.getUserId()) && nonNull(userPersonalData.getPerson())) {
            if(isNull(scenarioDto.getGender()) || !scenarioDto.getGender().equals(userPersonalData.getPerson().getGender()))
                scenarioDto.setGender(userPersonalData.getPerson().getGender());
        }

        if (userOrgData != null && userOrgData.getOrg() != null && userOrgData.getOrg().getType() != null) {
            scenarioDto.getAdditionalParameters().put(ORG_TYPE_ATTR, userOrgData.getOrg().getType().name());
        }
    }

    public void cleanUpAdditionalParameters(ScenarioDto scenarioDto) {
        scenarioDto.getAdditionalParameters().clear();
        fillUserData(scenarioDto);
    }

    private String getEsiaContactByType(String contactType) {
        String result = null;
        if (userPersonalData.getContacts() != null && contactType != null) {
            Optional<EsiaContact> esiaContact = userPersonalData.getContacts().stream().filter(contact -> contactType.equals(contact.getType()) && VERIFIED_ATTR.equals(contact.getVrfStu())).findFirst();
            if (esiaContact.isPresent()) {
                result = esiaContact.get().getValue();
            }
        }
        return result;
    }
}
