package ru.gosuslugi.pgu.components;

import java.util.Set;

/**
 * Атрибуты компонентов
 */
public final class ComponentAttributes {

    private ComponentAttributes() {
    }

    public static final String TYPE_ATTR = "type";
    public static final String GENDER_ATTR = "gender";
    public static final String GENDER_FULL_ATTR = "genderFull";
    public static final String FIRST_NAME_ATTR = "firstName";
    public static final String SNILS = "snils";
    public static final String DOC_TYPE = "docType";
    public static final String EMAIL_ATTR = "email";
    public static final String LAST_NAME_ATTR = "lastName";
    public static final String MIDDLE_NAME_ATTR = "middleName";
    public static final String BIRTH_DATE_ATTR = "birthDate";
    public static final String BIRTH_PLACE_ATTR = "birthPlace";
    public static final String RF_PASSPORT_SERIES_ATTR = "rfPasportSeries";
    public static final String RF_PASSPORT_NUMBER_ATTR = "rfPasportNumber";
    public static final String FOREIGN_PASSPORT_SERIES_ATTR = "foreignPasportSeries";
    public static final String FOREIGN_PASSPORT_NUMBER_ATTR = "foreignPasportNumber";
    public static final String FOREIGN_PASSPORT_ISSUE_DATE_ATTR = "foreignPasportIssueDate";
    public static final String RF_PASSPORT_ISSUE_DATE_ATTR = "rfPasportIssueDate";
    public static final String RF_PASSPORT_ISSUED_BY_ATTR = "rfPasportIssuedBy";
    public static final String FOREIGN_PASSPORT_ISSUED_BY_ATTR = "foreignPasportIssuedBy";
    public static final String RF_PASSPORT_ISSUED_BY_ID_ATTR = "rfPasportIssuedById";
    public static final String RF_PASSPORT_ISSUED_BY_ID_FORMATTED_ATTR = "rfPasportIssuedByIdFormatted";
    public static final String WARN_ATTR = "warn";
    public static final String ERROR_ATTR = "error";
    public static final String TITLE_ATTR = "title";
    public static final String DESC_ATTR = "desc";
    public static final String REG_ADDR_ATTR = "regAddr";
    public static final String REG_DATE_ATTR = "regDate";
    public static final String REG_ADDR_ZIP_CODE_ATTR = "regZipCode";
    public static final String ADDR_TYPE = "addrType";
    public static final String FIAS_ATTR = "fias";
    public static final String REGISTRATION_ATTR = "regAddr";
    public static final String REGISTRATION_DATE_ATTR = "regDate";
    public static final String CHILDREN_ID_ATTR = "id";
    public static final String CHILDREN_FIRST_NAME_ATTR = "firstName";
    public static final String CHILDREN_MIDDLE_NAME_ATTR = "middleName";
    public static final String CHILDREN_LAST_NAME_ATTR = "lastName";
    public static final String CHILDREN_BIRTH_DATE_ATTR = "birthDate";
    public static final String CHILDREN_GENDER_ATTR = "gender";
    public static final String CHILDREN_RF_BIRTH_CERTIFICATE_SERIES_ATTR = "rfBirthCertificateSeries";
    public static final String CHILDREN_RF_BIRTH_CERTIFICATE_NUMBER_ATTR = "rfBirthCertificateNumber";
    public static final String CHILDREN_RF_BIRTH_CERTIFICATE_ACT_NUMBER_ATTR = "rfBirthCertificateActNumber";
    public static final String CHILDREN_RF_BIRTH_CERTIFICATE_ISSUE_DATE_ATTR = "rfBirthCertificateIssueDate";
    public static final String CHILDREN_RF_BIRTH_CERTIFICATE_ISSUED_BY_ATTR = "rfBirthCertificateIssuedBy";
    public static final String CHILDREN_OMS_NUMBER_ATTR = "omsNumber";
    public static final String CHILDREN_OMS_SERIES_ATTR = "omsSeries";
    public static final String CHILDREN_BIRTH_CERTIFICATE_TYPE = "type";

    public static final String CHILDREN_ACT_DATE_ATTR = "actDate";
    public static final String FRGN_PASSPORT_PASSPORT_SERIES_ATTR = "frgnPasportSeries";
    public static final String FRGN_PASSPORT_NUMBER_ATTR = "frgnPasportNumber";
    public static final String FRGN_PASSPORT_ISSUE_DATE_ATTR = "frgnPasportIssueDate";
    public static final String FRGN_PASSPORT_ISSUED_BY_ATTR = "frgnPasportIssuedBy";
    public static final String FRGN_PASSPORT_EXPIRY_DATE_ATTR = "frgnPasportExpiryDate";
    public static final String FRGN_PASSPORT_LAST_NAME_ATTR = "frgnPasportLastName";
    public static final String FRGN_PASSPORT_FIRST_NAME_ATTR = "frgnPasportFirstName";
    //опекун/отец
    public static final String CHILDREN_RELATION_ATTR = "relationshipToChild";
    public static final String CHILDREN_IS_NEW_ATTR = "isNew";
    public static final String CHILDREN_LIST_REF_ATTR = "childListRef";

    public static final String FRGN_PASSPORT_CHECK_ATTR = "checkFRGN";
    public static final String RF_PASSPORT_SKIP_CHECK_ATTR = "skipCheckRf";

    public static final String BILL_NAME_ATTR = "billName";
    public static final String BILL_NUMBER_ATTR = "billNumber";
    public static final String BILL_DATE_ATTR = "billDate";
    public static final String FULL_AMOUNT_ATTR = "amount";
    public static final String SALE_AMOUNT_ATTR = "saleAmount";
    public static final String BILL_ID_ATTR = "billId";

    public static final String SEND_AS_ANOTHER_REQUEST_ATTR = "imaginaryOidBase";

    public static final String SERIES_ATTR = "series";
    public static final String NUMBER_ATTR = "number";
    public static final String DATE_ATTR = "date";
    public static final String EMITTER_ATTR = "emitter";
    public static final String ISSUE_ID_ATTR = "issueId";
    public static final String EXPIRATION_DATE_ATTR = "expirationDate";
    public static final String CURRENT_TIME_ATTR = "currentTime";

    /** Наименование справочника НСИ для выбора объекта на карте. */
    public static final String DICTIONARY_NAME_ATTR = "dictionaryType";
    public static final String DICTIONARY_OPTIONS = "dictionaryOptions";
    /** Список простых условий фильтра с правилами получения значений для выбора данных из справочника. */
    public static final String DICTIONARY_FILTER_NAME_ATTR = "dictionaryFilter";
    public static final String SECONDARY_DICTIONARY_FILTER_NAME_ATTR = "secondaryDictionaryFilter";
    public static final String DICTIONARY_FILTERS_NAME_ATTR = "dictionaryFilters";
    public static final String PREV_STEP_CLEAN_CACHE_ATTR = "prevStepCleanCache";
    public static final String REF_ATTR = "ref";
    public static final String DICTIONARY_FILTER_IN_REF = "dictionaryFilterInRef";
    public static final String DICTIONARY_FILTER_VALUE_TYPE_PRESET = "preset";
    public static final String DICTIONARY_FILTER_RELATION = "relation";
    public static final String DICTIONARY_FILTER_RELATION_FILTER_ON = "filterOn";

    public static final String MVD_SOURCE_ATTR_NAME = "mvd_source";

    /** Атрибут управляет baseurl у Lookup-компонента. */
    public static final String DICT_URL_TYPE = "dictionaryUrlType";

    /** Тип значения в {@link #DICTIONARY_FILTER_NAME_ATTR}. */
    public static final String ATTRIBUTE_TYPE = "attributeType";

    public static final String VALUE_NOT_FOUND_MESSAGE = "Выбранное значение не найдено в справочнике %s";

    public static final String ORIGINAL_ITEM = "originalItem";
    public static final String ATTRIBUTE_VALUES = "attributeValues";


    /** Атрибут с описанием типа получения значения для подстановки в фильтр. */
    public static final String DICT_FILTER_VALUE_TYPE = "valueType";
    /**
     * Атрибут, содержащий значение в соответствие с типом значения фильтра
     * для получения окончательного значения для подстановки в фильтр
     */
    public static final String DICT_FILTER_VALUE_NAME = "value";

    /** Название атрибута для передачи значения в фильтр. */
    public static final String AS_STRING_FILTER_ATTRIBUTE_NAME = "asString";
    public static final String NOT_CORRECT_CONDITION_PARSING = "Невозможно сформировать условие фильтрации. Описание фильтра некорректно";
    public static final String NOT_CORRECT_FILTER_VALUE_TYPE = "Тип получения значения фильтра не определен, тип в json %s";

    /** Наименование атрибута, по которому производится фильтрация. */
    public static final String DICT_FILTER_ATTRIBUTE_NAME = "attributeName";

    public static final String YEARS_ATTR = "years";
    public static final String NON_STOP_ATTR = "nonStop";

    public static final String REG_CODE_ATTR = "regCode";
    public static final String GEO_LON_ATTR = "geo_lon";
    public static final String GEO_LAT_ATTR = "geo_lat";
    public static final String REG_OKATO_ATTR = "regOkato";
    public static final String REG_OKTMO_ATTR = "regOktmo";

    public static final String OKATO_ATTR_NAME = "okato";
    public static final String OKTMO_ATTR_NAME = "oktmo";
    public static final String OKTMO_TERRITORY_8_ATTR_NAME = "oktmo_territory_8";
    public static final String OKTMO_TERRITORY_11_ATTR_NAME = "oktmo_territory_11";
    public static final String ADDRESS = "address";
    public static final String ADDRESS_FIAS = "fias";
    public static final String ADDRESS_UPPERCASE = "ADDRESS";

    public static final String TIME_SLOT_ATTR = "ts";
    public static final String START_TIME_ATTR = "startTime";
    public static final String EXPIRATION_TIME_ATTR = "expirationTime";
    public static final String TIMER_CODE_ATTR = "timerCode";
    public static final String TIME_SLOT_TYPE_ATTR = "timeSlotType";
    public static final String START_DATE_ATTR = "Startdate";
    public static final String TS_START_TIME_ATTR = "Starttime";
    public static final String END_DATE_ATTR = "Enddate";
    public static final String TS_END_TIME_ATTR = "Endtime";
    public static final String DEPARTMENT_ATTR = "department";
    public static final String DEPARTMENT_REGION_ATTR = "departmentRegion";
    public static final String SOLEMN_ATTR = "solemn";
    public static final String SLOTS_PERIOD_ATTR = "slotsPeriod";
    public static final String BOOK_ATTRS_ATTR = "bookAttributes";
    public static final String DOCTOR_ATTR = "doctor";
    public static final String ANOTHER_PERSON_ATTR = "anotherperson";
    public static final String GENDER_PERSON_ATTR = "genderperson";
    public static final String AGE_PERSON_ATTR = "ageperson";
    public static final String PATIENT_NAME_ATTR = "pacientname";
    public static final String TIME_START_ATTR = "timeStart";
    public static final String TIME_FINISH_ATTR = "timeFinish";
    public static final String SERVICE_ID_ATTR = "serviceId";
    public static final String SERVICE_CODE_ATTR = "serviceCode";
    public static final String TS_SERVICE_ID_ATTR = "Service_Id";
    public static final String SERVICE_SPEC_ID_ATTR = "ServiceSpec_Id";
    public static final String USER_SELECTED_REGION_ATTR = "userSelectedRegion";

    public static final String CALCULATIONS_ATTR = "calculations";

    public static final String REFS_ATTR = "refs";

    public static final String ORG_CODE_ATTR = "orgCode";
    public static final String ORG_PHONE_TYPE_ATTR = "OPH";
    public static final String ORG_EMAIL_TYPE_ATTR = "OEM";
    public static final String ORG_FULL_NAME_ATTR = "fullName";
    public static final String ORG_SHORT_NAME_ATTR = "shortName";
    public static final String ORG_OGRN_ATTR = "ogrn";
    public static final String ORG_INN_ATTR = "inn";
    public static final String ORG_KPP_ATTR = "kpp";

    public static final String ORG_CHIEF_FIRST_NAME_ATTR = "chiefFirstName";
    public static final String ORG_CHIEF_LAST_NAME_ATTR = "chiefLastName";
    public static final String ORG_CHIEF_MIDDLE_NAME_ATTR = "chiefMiddleName";
    public static final String ORG_CHIEF_BIRTH_DATE_ATTR = "chiefBirthDate";
    public static final String ORG_CHIEF_POSITION_ATTR = "chiefPosition";

    public static final String PASSPORT_TS_ARG = "passportTS";

    public static final String MIN_AGE_ATTR = "minAge";
    public static final String MAX_AGE_ATTR = "maxAge";
    public static final String BORN_AFTER_DATE_ATTR = "bornAfterDate";
    public static final String BORN_BEFORE_DATE_ATTR = "bornBeforeDate";

    // ESIA address attributes
    /** адрес регистрации */
    public static final String ADDRESS_TYPE_PRG = "PRG";
    /** адрес проживания */
    public static final String ADDRESS_TYPE_PLV = "PLV";

    // ESIA document attributes
    public static final String RF_PASSPORT_ATTR = "RF_PASSPORT";
    public static final String FID_DOC_ATTR = "FID_DOC";
    public static final String FRGN_PASSPORT_ATTR = "FRGN_PASS";
    public static final String MDCL_PLCY_ATTR = "MDCL_PLCY";
    public static final String VERIFIED_ATTR = "VERIFIED";
    public static final String IGNORE_VERIFICATION_ATTR = "ignoreVerification";
    public static final String ESIA_DATE_FORMAT = "dd.M.yyyy";

    // ESIA contact attributes
    public static final String ESIA_CONTACT_PHONE = "CPH";
    public static final String ESIA_MOBILE_PHONE = "MBT";
    public static final String ESIA_EMAIL = "EML";

    public static final String TIMEZONE_ATTR = "timezone";

    public static final String HOME_PHONE = "homePhone";
    public static final String MOBILE_PHONE = "mobilePhoneNumber";
    public static final String CONTACT_PHONE = "contactPhoneNumber";
    public static final String CITIZENSHIP_ATTR = "citizenship";
    public static final String CITIZENSHIP_CODE_ATTR = "citizenshipCode";
    public static final String BIRTHDATE_CODE_ATTR = "birthdateCode";
    public static final String OMS_NUMBER_ATTR = "omsNumber";
    public static final String OMS_SERIES_ATTR = "omsSeries";

    public static final String MASTER_SNILS_ATTR = "masterSnils";
    public static final String MASTER_FIRST_NAME_ATTR = "masterFirstName";
    public static final String MASTER_LAST_NAME_ATTR = "masterLastName";
    public static final String MASTER_MIDDLE_NAME_ATTR = "masterMiddleName";
    public static final String MASTER_BIRTH_DATE_ATTR = "masterBirthDate";
    public static final String MASTER_GENDER_ATTR = "masterGender";
    public static final String MASTER_CITIZENSHIP_ATTR = "masterCitizenship";
    public static final String MASTER_CITIZENSHIP_CODE_ATTR = "masterCitizenshipCode";
    public static final String MASTER_BIRTHDATE_CODE_ATTR = "masterBirthdateCode";
    public static final String MASTER_TIMEZONE_ATTR = "masterTimezone";

    public static final String COMPARE_ROWS_ATTR = "compare_rows";
    public static final String COMPARE_ROWS_SCREENS_ATTR = "compare_rows_screens";

    public static final String METHOD_ATTR = "method";
    public static final String URL_ATTR = "url";
    public static final String PATH_ATTR = "path";
    public static final String BODY_ATTR = "body";
    public static final String QUERY_PARAMETERS = "queryParameters";
    public static final String HEADERS_ATTR = "headers";
    public static final String COOKIES_ATTR = "cookies";
    public static final String FORMDATA_ATTR = "formData";
    public static final String TIMEOUT_ATTR = "timeout";
    public static final String FILENAME_ATTR = "fileName";
    public static final String FILE_MIME_TYPE_ATTR = "fileMimeType";

    public static final String VALUE_ATTR = "value";
    public static final String ERROR_MSG_ATTR = "errorMsg";
    public static final String ERROR_DESC_ATTR = "errorDesc";
    public static final String ENABLE_CUSTOM_VALIDATION_ATTR = "enableCustomValidation";

    public static final String TX_ATTR = "tx";
    public static final String VIN_ATTR = "vin";
    public static final String STS_ATTR = "sts";
    public static final String GOV_REG_NUMBER_ATTR = "govRegNumber";
    public static final String PDF_LINK_ATTR = "pdfLink";
    public static final String TYPE_ID_ATTR = "TypeID";

    public static final String PARSE_DESCRIPTION_ATTR = "parseDescription";

    public static final String REFERRAL_NUMBER = "Referral_Number";
    public static final String SESSION_ID = "Session_Id";
    public static final String MO_ID = "MO_Id";
    public static final String ESERVICE_ID = "eserviceId";
    public static final String MEDICAL_POS_REFNAME = "ServiceOrSpecs";
    public static final String[] MEDICAL_POS_ATTRIBUTES = new String[]{"Service_Id", "Service_Name"};
    public static final Set<String> MEDICAL_POS_ARGS = Set.of(ESERVICE_ID, MO_ID, SESSION_ID);

    public static final String IS_NEED_REMOVE_ATTACH_ATTR = "dntAttach";

    /** Параметр указывается, если в рест-методе api/lk/v3/orders/listpaymentsinfo требуется убрать из тела запроса orgCode. */
    public static final String IGNORE_ORGCODE = "ignoreOrgCode";
    public static final String ORGANIZATION_ID_ARG_KEY = "organizationId";
    public static final String REUSE_PAYMENT_UIN = "reusePaymentUin";
    public static final String USE_PAYMENT_INFO_API_V1 = "usePaymentInfoApiV1";

    /** Аттрибут, описывающий отображающийся по умолчанию элемент ошибки */
    public static final String DEFAULT_HINT = "defaultHint";

    /** Аттрибут, описывающий типы файлов, которые необходимо упаковать в .zip архив */
    public static final String ARCHIVE_TYPES = "archiveTypes";

    /** Аттрибут, описывающий максимально допустимый объем загрузок по компоненту FileUploadComponent */
    public static final String MAX_SIZE = "maxSize";

    /** Аттрибут, описывающий максимально допустимое количество загружаемых файлов по компоненту FileUploadComponent */
    public static final String MAX_FILE_COUNT = "maxFileCount";
}
