package ru.gosuslugi.pgu.fs.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.minidev.json.JSONArray;
import org.springframework.stereotype.Service;
import ru.atc.carcass.security.rest.model.EsiaAddress;
import ru.atc.carcass.security.rest.model.EsiaContact;
import ru.atc.carcass.security.rest.model.person.Person;
import ru.atc.carcass.security.rest.model.person.PersonDoc;
import ru.gosuslugi.pgu.common.esia.search.dto.UserOrgData;
import ru.gosuslugi.pgu.common.esia.search.dto.UserPersonalData;
import ru.gosuslugi.pgu.fs.common.service.ProtectedFieldService;
import ru.gosuslugi.pgu.fs.service.EmpowermentService;
import ru.gosuslugi.pgu.fs.service.UserDataService;

import javax.annotation.PostConstruct;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import static ru.gosuslugi.pgu.common.core.date.util.DateUtil.ESIA_DATE_FORMAT;
import static ru.gosuslugi.pgu.components.ComponentAttributes.ADDRESS_TYPE_PLV;
import static ru.gosuslugi.pgu.components.ComponentAttributes.ADDRESS_TYPE_PRG;
import static ru.gosuslugi.pgu.components.ComponentAttributes.CONTACT_PHONE;
import static ru.gosuslugi.pgu.components.ComponentAttributes.ESIA_CONTACT_PHONE;
import static ru.gosuslugi.pgu.components.ComponentAttributes.ESIA_EMAIL;
import static ru.gosuslugi.pgu.components.ComponentAttributes.ESIA_MOBILE_PHONE;
import static ru.gosuslugi.pgu.components.ComponentAttributes.FRGN_PASSPORT_ATTR;
import static ru.gosuslugi.pgu.components.ComponentAttributes.MOBILE_PHONE;
import static ru.gosuslugi.pgu.components.ComponentAttributes.ORG_EMAIL_TYPE_ATTR;
import static ru.gosuslugi.pgu.components.ComponentAttributes.ORG_PHONE_TYPE_ATTR;
import static ru.gosuslugi.pgu.components.ComponentAttributes.RF_DRIVING_LICENSE_ATTR;
import static ru.gosuslugi.pgu.components.ComponentAttributes.RF_PASSPORT_ATTR;
import static ru.gosuslugi.pgu.components.ComponentAttributes.VERIFIED_ATTR;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProtectedFieldServiceImpl implements ProtectedFieldService {

    private static final String ORGANIZATION_TYPE = "orgType";
    private static final String ORGANIZATION_USER_ROLE = "userRole";
    private static final String ORGANIZATION_PHONE = "orgPhone";
    private static final String ORGANIZATION_EMAIL = "orgEmail";
    private static final String ORGANIZATION_NAME = "orgName";
    private static final String ORGANIZATION_INN = "orgInn";
    private static final String ORGANIZATION_OGRN = "orgOgrn";
    private static final String ORGANIZATION_LEGAL_ADDRESS = "orgLegalAddress";
    private static final String ORGANIZATION_FACT_ADDRESS = "orgFactAddress";


    private final UserPersonalData userPersonalData;
    private final UserOrgData userOrgData;
    private final UserDataService userDataService;
    private final EmpowermentService empowermentService;

    private final Map<String, Function<UserPersonalData, Object>> methodMap = new HashMap<>();

    @PostConstruct
    public void init() {
        methodMap.put("userId", userPersonalData -> String.valueOf(userPersonalData.getUserId()));
        methodMap.put("lastName", userPersonalData -> userPersonalData.getPerson().getLastName());
        methodMap.put("firstName", userPersonalData -> userPersonalData.getPerson().getFirstName());
        methodMap.put("middleName", userPersonalData -> userPersonalData.getPerson().getMiddleName());
        methodMap.put("gender", userPersonalData -> userPersonalData.getPerson().getGender());
        methodMap.put("birthPlace", userPersonalData-> userPersonalData.getPerson().getBirthPlace());
        methodMap.put("birthDate", userPersonalData -> {
            String birthDate = userPersonalData.getPerson().getBirthDate();
            return birthDate != null
                    ? LocalDate.parse(birthDate, DateTimeFormatter.ofPattern(ESIA_DATE_FORMAT)).format(DateTimeFormatter.ISO_DATE)
                    : null;
        });
        methodMap.put("citizenshipCode", userPersonalData -> userPersonalData.getPerson().getCitizenshipCode());
        methodMap.put("snils", userPersonalData -> userPersonalData.getPerson().getSnils());

        methodMap.put("livingAddress", userPersonalData -> {
            Optional<EsiaAddress> address = getAddressByType(userPersonalData, ADDRESS_TYPE_PLV);
            return address.isPresent() ?   address.get().getAddressStr() : null;
        });
        methodMap.put("registrationAddress", userPersonalData -> {
            Optional<EsiaAddress> address = getAddressByType(userPersonalData, ADDRESS_TYPE_PRG);
            return address.isPresent() ?   address.get().getAddressStr() : null;
        });

        methodMap.put("omsNumber", userPersonalData -> {
            Optional<PersonDoc> doc = userPersonalData.getOmsDocument();
            return doc.isPresent() ? doc.get().getNumber()  : null;
        });
        methodMap.put("omsSeries", userPersonalData -> {
            Optional<PersonDoc> doc = userPersonalData.getOmsDocument();
            return doc.isPresent() ? doc.get().getSeries()  : null;
        });

        methodMap.put("medicalOrg", userPersonalData -> {
            Optional<PersonDoc> doc = userPersonalData.getOmsDocument();
            return doc.isPresent() ? doc.get().getMedicalOrg()  : null;
        });

        methodMap.put("unitedNumber", userPersonalData -> {
            Optional<PersonDoc> doc = userPersonalData.getOmsDocument();
            return doc.isPresent() ? doc.get().getUnitedNumber() : null;
        });

        methodMap.put("omsIssuePlace", userPersonalData -> {
            Optional<PersonDoc> doc = userPersonalData.getOmsDocument();
            return doc.isPresent() ? doc.get().getIssuePlace()  : null;
        });

        methodMap.put("omsIssueId", userPersonalData -> {
            Optional<PersonDoc> doc = userPersonalData.getOmsDocument();
            return doc.isPresent() ? doc.get().getIssueId()  : null;
        });

        methodMap.put("omsIssuedBy", userPersonalData -> {
            Optional<PersonDoc> doc = userPersonalData.getOmsDocument();
            return doc.isPresent() ? doc.get().getIssuedBy()  : null;
        });

        methodMap.put("rfPassportNumber", userPersonalData -> {
            PersonDoc doc = getDocumentByType(userPersonalData, RF_PASSPORT_ATTR);
            return doc != null ? doc.getNumber() : null;
        });
        methodMap.put("rfPassportSeries", userPersonalData -> {
            PersonDoc doc = getDocumentByType(userPersonalData, RF_PASSPORT_ATTR);
            return doc != null ? doc.getSeries() : null;
        });
        methodMap.put("rfPassportIssueDate", userPersonalData -> {
            PersonDoc doc = getDocumentByType(userPersonalData, RF_PASSPORT_ATTR);
            return doc != null ? doc.getIssueDate() : null;
        });
        methodMap.put("rfPassportIssuedBy", userPersonalData -> {
            PersonDoc doc = getDocumentByType(userPersonalData, RF_PASSPORT_ATTR);
            return doc != null ? doc.getIssuedBy() : null;
        });

        methodMap.put("foreignPassportNumber", userPersonalData -> {
            PersonDoc doc = getDocumentByType(userPersonalData, FRGN_PASSPORT_ATTR);
            return doc != null ? doc.getNumber() : null;
        });
        methodMap.put("foreignPassportSeries", userPersonalData -> {
            PersonDoc doc = getDocumentByType(userPersonalData, FRGN_PASSPORT_ATTR);
            return doc != null ? doc.getSeries() : null;
        });
        methodMap.put("foreignPassportIssueDate", userPersonalData -> {
            PersonDoc doc = getDocumentByType(userPersonalData, FRGN_PASSPORT_ATTR);
            return doc != null ? doc.getIssueDate() : null;
        });
        methodMap.put("foreignPassportIssuedBy", userPersonalData -> {
            PersonDoc doc = getDocumentByType(userPersonalData, FRGN_PASSPORT_ATTR);
            return doc != null ? doc.getIssuedBy() : null;
        });
        methodMap.put("foreignPassportVerified", userPersonalData -> {
            PersonDoc doc = getDocumentByType(userPersonalData, FRGN_PASSPORT_ATTR);
            return doc != null ? doc.getVrfStu() : null;
        });
        methodMap.put("drivingLicenseSeries", userPersonalData -> {
            PersonDoc doc = getDocumentByType(userPersonalData, RF_DRIVING_LICENSE_ATTR);
            return doc != null ? doc.getSeries() : null;
        });
        methodMap.put("drivingLicenseNumber", userPersonalData -> {
            PersonDoc doc = getDocumentByType(userPersonalData, RF_DRIVING_LICENSE_ATTR);
            return doc != null ? doc.getNumber() : null;
        });
        methodMap.put("drivingLicenseIssueDate", userPersonalData -> {
            PersonDoc doc = getDocumentByType(userPersonalData, RF_DRIVING_LICENSE_ATTR);
            return doc != null ? doc.getIssueDate() : null;
        });
        methodMap.put("drivingLicenseExpireDate", userPersonalData -> {
            PersonDoc doc = getDocumentByType(userPersonalData, RF_DRIVING_LICENSE_ATTR);
            return doc != null ? doc.getExpiryDate() : null;
        });
        methodMap.put("drivingLicenseIssuedBy", userPersonalData -> {
            PersonDoc doc = getDocumentByType(userPersonalData, RF_DRIVING_LICENSE_ATTR);
            return doc != null ? doc.getIssuedBy() : null;
        });
        methodMap.put(CONTACT_PHONE, userPersonalData -> {
            EsiaContact contact = getContactByType(userPersonalData, ESIA_CONTACT_PHONE);
            return contact != null ? contact.getValue() : null;
        });
        methodMap.put(MOBILE_PHONE, userPersonalData -> {
            EsiaContact contact = getContactByType(userPersonalData, ESIA_MOBILE_PHONE);
            return contact != null ? contact.getValue() : null;
        });
        methodMap.put("email", userPersonalData -> {
            EsiaContact contact = getContactByType(userPersonalData, ESIA_EMAIL);
            return contact != null ? contact.getValue() : null;
        });

        methodMap.put("assuranceLevel", userPersonalData -> userPersonalData.getPerson().getAssuranceLevel());
        methodMap.put("powers", userPersonalData -> {
            if (userPersonalData.getOrgId() != null) {
                JSONArray arr = new JSONArray();
                arr.addAll(empowermentService.getUserEmpowerments());
                return arr;
            }
            return null;
        });
    }

    @Override
    public Object getValue(String name) {
        Person person = userPersonalData.getPerson();
        if (methodMap.containsKey(name) && Objects.nonNull(person)) {
            return methodMap.get(name).apply(userPersonalData);
        }

        if (Objects.nonNull(userOrgData.getOrg())) {
            return getOrgValue(name);
        }

        return null;
    }

    @Override
    public Map<String, Object> getAllValues() {
        return methodMap.entrySet().stream()
                .collect(HashMap::new, (map, e) -> map.put(e.getKey(), getValue(e.getKey())), HashMap::putAll);
    }

    private Object getOrgValue(String name) {
        Object result = null;
        if (ORGANIZATION_TYPE.equals(name)) {
            if (Objects.nonNull(userOrgData.getOrg())) {
                result = userOrgData.getOrg().getType().toString();
            }
        }
        if (ORGANIZATION_USER_ROLE.equals(name)) {
            result = userDataService.isChief();
        }
        if (ORGANIZATION_PHONE.equals(name)) {
            result = userOrgData.getVerifiedContactValue(ORG_PHONE_TYPE_ATTR);
        }
        if (ORGANIZATION_EMAIL.equals(name)) {
            result = userOrgData.getVerifiedContactValue(ORG_EMAIL_TYPE_ATTR);
        }
        if (ORGANIZATION_NAME.equals(name)) {
            result = userOrgData.getOrg().getFullName();
        }
        if (ORGANIZATION_INN.equals(name)) {
            result = userOrgData.getOrg().getInn();
        }
        if (ORGANIZATION_OGRN.equals(name)) {
            result = userOrgData.getOrg().getOgrn();
        }
        if (ORGANIZATION_LEGAL_ADDRESS.equals(name)) {
            List<EsiaAddress> addresses = userOrgData.getAddresses();
            if (addresses != null && !addresses.isEmpty()) {
                result = getOrgAddressByType(userOrgData, "OLG");
            }
        }
        if (ORGANIZATION_FACT_ADDRESS.equals(name)) {
            List<EsiaAddress> addresses = userOrgData.getAddresses();
            if (addresses != null && !addresses.isEmpty()) {
                result = getOrgAddressByType(userOrgData, "OPS");
            }
        }
        return result;
    }

    private PersonDoc getDocumentByType(UserPersonalData userPersonalData, String docType) {
        List<PersonDoc> docs = userPersonalData.getDocs().stream().filter(x -> (docType.equals(x.getType()))).collect(Collectors.toList());
        return docs.stream().filter(doc -> doc.getVrfStu().equals(VERIFIED_ATTR)).findFirst().orElse(docs.stream().findFirst().orElse(null));
    }

    private EsiaContact getContactByType(UserPersonalData userPersonalData, String contactType) {
        List<EsiaContact> contacts = userPersonalData.getContacts().stream().filter(x -> (contactType.equals(x.getType()))).collect(Collectors.toList());
        return contacts.stream().filter(contact -> contact.getVrfStu().equals(VERIFIED_ATTR)).findFirst().orElse(contacts.stream().findFirst().orElse(null));
    }

    private Optional<EsiaAddress> getAddressByType(UserPersonalData userPersonalData, String addressType) {
        return Optional.ofNullable(userPersonalData.getAddresses().stream().filter(address -> address.getType().equals(addressType)).findFirst().orElse(null));
    }

    private String getOrgAddressByType(UserOrgData userOrgData, String addressType) {
        String addrStr = null;
        List<EsiaAddress> addresses = userOrgData.getAddresses();
        if (addresses != null && !addresses.isEmpty()) {
            EsiaAddress address = addresses.stream().filter(addr -> addr.getType().equals(addressType)).findFirst().orElse(null);
            if (address != null) {
                addrStr = address.getAddressStr();
            }
        }
        return addrStr;
    }
}
