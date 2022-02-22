package ru.gosuslugi.pgu.fs.component.payment;

import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.JsonPath;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import ru.atc.carcass.security.rest.model.person.PersonDoc;
import ru.gosuslugi.pgu.common.core.exception.ValidationException;
import ru.gosuslugi.pgu.common.esia.search.dto.UserOrgData;
import ru.gosuslugi.pgu.common.esia.search.dto.UserPersonalData;
import ru.gosuslugi.pgu.dto.ApplicantAnswer;
import ru.gosuslugi.pgu.dto.descriptor.FieldComponent;
import ru.gosuslugi.pgu.fs.common.component.AbstractComponent;
import ru.gosuslugi.pgu.fs.common.exception.FormBaseException;
import ru.gosuslugi.pgu.fs.common.service.JsonProcessingService;
import ru.gosuslugi.pgu.fs.component.payment.model.PayerInfo;

import java.util.Map;
import java.util.Optional;

/**
 * Компонент по поддержке способа оплаты госпошлины
 */
@Slf4j
@Component
@RequiredArgsConstructor
public abstract class AbstractPaymentComponent<InitialValueModel> extends AbstractComponent<InitialValueModel> {

    private final static String VERIFIED_ATTR_VAL = "VERIFIED";
    private static final String FL_SPECIFIC_PAYERID = "specificPayerIdTypeByOwnerDocumentTypeCode";
    private static final String APPLICANT_STRING_VALUE = "applicantTypeStringValue";

    private final UserOrgData userOrgData;
    protected final JsonProcessingService jsonProcessingService;
    protected final UserPersonalData userPersonalData;

    public static final String FL_APPLICANT_TYPE = "FL";
    public static final String UL_APPLICANT_TYPE = "UL";
    public static final String IP_APPLICANT_TYPE = "IP";

    public static final String DEFAULT_PAYER_ID_TYPE_FL = "01";
    public static final String DEFAULT_PAYER_ID_TYPE_UL = "99";
    public static final String DEFAULT_PAYER_ID_TYPE_IP = "98";
    public static final String DEFAULT_PAYER_ID_TYPE_FOREIGN_CITIZEN = "08";
    public static final String FL_SPECIFIC_PAYERID_VALUE = "08";

    public PayerInfo getPayerIdTypeAndPayerIdNum(FieldComponent component, String applicantType, Map<String, ApplicantAnswer> currentAnswer, Map<String, ApplicantAnswer> previousAnswers) {
        String payerIdType;
        String payerIdNum;
        applicantType = applicantType.isEmpty() ? "FL" : applicantType;
        boolean applicantTypeFL = FL_APPLICANT_TYPE.equalsIgnoreCase(applicantType);
        boolean applicantTypeUL = UL_APPLICANT_TYPE.equalsIgnoreCase(applicantType);
        boolean applicantTypeIP = IP_APPLICANT_TYPE.equalsIgnoreCase(applicantType);

        if (!"RUS".equalsIgnoreCase(userPersonalData.getPerson().getCitizenshipCode())) {
            PersonDoc foreignPassport =
                    userPersonalData.getDocs().stream()
                            .filter(x -> ("FID_DOC".equals(x.getType()) && x.getVrfStu().equals(VERIFIED_ATTR_VAL)))
                            .findFirst()
                            .orElseThrow(() -> new ValidationException("Couldn't find foreign passport"));
            payerIdType = DEFAULT_PAYER_ID_TYPE_FOREIGN_CITIZEN;
            payerIdNum = foreignPassport.getSeries() + foreignPassport.getNumber();
            return new PayerInfo(payerIdType, payerIdNum);
        }
        String applicantTypeStringValue = component.getArgument(APPLICANT_STRING_VALUE);
        DocumentContext currentDocumentContext = JsonPath.parse(currentAnswer);
        DocumentContext previousDocumentContext = JsonPath.parse(previousAnswers);
        if (applicantTypeFL) {
            PersonDoc rfPassport = userPersonalData.getDocs().stream()
                    .filter(x -> ("RF_PASSPORT".equals(x.getType()) && x.getVrfStu().equals(VERIFIED_ATTR_VAL)))
                    .findFirst()
                    .orElseThrow(() -> new ValidationException("Couldn't find russian passport"));
            payerIdType = DEFAULT_PAYER_ID_TYPE_FL;
            payerIdNum = rfPassport.getSeries() + rfPassport.getNumber();
            String specificPayerId = component.getArgument(FL_SPECIFIC_PAYERID);

            PayerInfo payerInfo = getPayerInfoOrDefault(applicantTypeStringValue, currentDocumentContext,
                previousDocumentContext, new PayerInfo(payerIdType, payerIdNum));
            if (StringUtils.hasText(specificPayerId)) {
                payerInfo.setIdType(specificPayerId);
            }
            return payerInfo;
        }

        if (applicantTypeUL || applicantTypeIP) {
            // сначало пытаемся вытянуть значения из аргумента "applicantTypeStringValue"
            PayerInfo payerInfo = getPayerInfo(applicantTypeStringValue, currentDocumentContext, previousDocumentContext);
            if (payerInfo.getIdNum() != null) {
                return payerInfo;
            }

            if (applicantTypeUL) {
                if (userOrgData == null || userOrgData.getOrg() == null) {
                    throw new FormBaseException("Учетная запись пользователь не является организацией");
                }
                return new PayerInfo(DEFAULT_PAYER_ID_TYPE_UL, userOrgData.getOrg().getInn() + userOrgData.getOrg().getKpp());
            }
            return new PayerInfo(DEFAULT_PAYER_ID_TYPE_IP, userPersonalData.getPerson().getInn());
        }

        return new PayerInfo();
    }

    private PayerInfo getPayerInfoOrDefault(String applicantTypeStringValue, DocumentContext current, DocumentContext previous,
                                            PayerInfo defaultPayerInfo) {
        PayerInfo payerInfo = getPayerInfo(applicantTypeStringValue, current, previous);
        if (payerInfo.getIdNum() == null) {
            return defaultPayerInfo;
        }
        return payerInfo;
    }

    private PayerInfo getPayerInfo(String applicantTypeStringValue, DocumentContext current, DocumentContext previous) {
        PayerInfo result = new PayerInfo();
        if (StringUtils.hasText(applicantTypeStringValue)) {
            String[] payerInfo = applicantTypeStringValue.split(",");
            if (payerInfo.length != 2) {
                return result;
            }
            result.setIdType(payerInfo[0]);
            String[] docNumAndSeries = payerInfo[1].split("\\+");
            result.setIdNum(getPayerIdNum(docNumAndSeries, current, previous));
        }

        return result;
    }

    private String getPayerIdNum(String[] values, DocumentContext current, DocumentContext previous) {
        if (values.length == 1) {
            return getValueFromAnswers(values[0], current, previous);
        }
        if (values.length == 2) {
            return getValueFromAnswers(values[0], current, previous) + getValueFromAnswers(values[1], current, previous);
        }
        return "";
    }

    private String getValueFromAnswers(String field, DocumentContext currentAnswer, DocumentContext previousAnswers) {
        String value = Optional.ofNullable(jsonProcessingService.getFieldFromContext(field, currentAnswer, ApplicantAnswer.class)).map(ApplicantAnswer::getValue).orElse("");
        if (StringUtils.hasText(value)) {
            return value;
        }
        return Optional.ofNullable(jsonProcessingService.getFieldFromContext(field, previousAnswers, ApplicantAnswer.class)).map(ApplicantAnswer::getValue).orElse("");
    }
}
