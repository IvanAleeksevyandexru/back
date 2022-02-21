package ru.gosuslugi.pgu.fs.component.payment;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.atc.carcass.security.rest.model.person.Person;
import ru.gosuslugi.pgu.common.esia.search.dto.UserOrgData;
import ru.gosuslugi.pgu.common.esia.search.dto.UserPersonalData;
import ru.gosuslugi.pgu.dto.ScenarioDto;
import ru.gosuslugi.pgu.dto.descriptor.FieldComponent;
import ru.gosuslugi.pgu.dto.descriptor.types.ComponentType;
import ru.gosuslugi.pgu.fs.common.component.ComponentResponse;
import ru.gosuslugi.pgu.fs.common.service.JsonProcessingService;
import ru.gosuslugi.pgu.fs.component.payment.model.PayerInfo;
import ru.gosuslugi.pgu.pgu_common.payment.dto.bill.BillInfoResponseWrapper;
import ru.gosuslugi.pgu.pgu_common.payment.dto.bill.ImportBillNewResponse;
import ru.gosuslugi.pgu.pgu_common.payment.dto.bill.ImportBillStatusResponse;
import ru.gosuslugi.pgu.pgu_common.payment.service.BillingService;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

/**
 * Компонент Интеграции с инд.платежным шлюзом для получения квитанции с уник.идентиф.номером
 */
@Slf4j
@Component
public class CreateBillComponent extends AbstractPaymentComponent<String> {

    protected final BillingService billingService;

    public CreateBillComponent(UserOrgData userOrgData, JsonProcessingService jsonProcessingService,
                               UserPersonalData userPersonalData, BillingService billingService) {
        super(userOrgData, jsonProcessingService, userPersonalData);
        this.billingService = billingService;
    }

    @Override
    public ComponentType getType() {
        return ComponentType.CreateBill;
    }

    public ComponentResponse<String> getInitialValue(FieldComponent component, ScenarioDto scenarioDto) {
        String applicantType = component.getArgument("applicantType");
        applicantType = applicantType.isEmpty() ? "FL" : applicantType;
        final Map<String, Object> parameters = fillParameters(component, scenarioDto, applicantType);
        return createNewBill(parameters, 0);
    }

    Map<String, Object> fillParameters(FieldComponent component, ScenarioDto scenarioDto, String applicantType) {
        Map<String, String> arguments = component.getArguments();
        final Map<String, Object> parameters = new HashMap<>();
        parameters.put("orderId", scenarioDto.getOrderId());

        boolean applicantTypeFL = FL_APPLICANT_TYPE.equalsIgnoreCase(applicantType);
        boolean applicantTypeIP = IP_APPLICANT_TYPE.equalsIgnoreCase(applicantType);
        parameters.put("personalType", applicantTypeFL || applicantTypeIP ? "P" : "O");
        parameters.put("senderIdentifier", arguments.get("SENDER_IDENTIFIER"));
        parameters.put("originatorId", arguments.get("ORIGINATOR_ID"));
        parameters.put("senderRole", arguments.get("SENDER_ROLE"));
        parameters.put("totalAmount", arguments.get("STATE_DUTY"));
        parameters.put("validUntil", LocalDate.now().plusDays(30));
        parameters.put("purpose", "Оплата госпошлины за получение лицензии");
        parameters.put("kbk", arguments.get("KBK"));
        parameters.put("oktmo", arguments.get("OKTMO"));
        parameters.put("payeeName", arguments.get("RECIPIENT_NAME"));
        parameters.put("inn", arguments.get("INN"));
        parameters.put("kpp", arguments.get("KPP"));
        parameters.put("accountNumber", arguments.get("TREASURY_ACCOUNT"));
        parameters.put("bank", arguments.get("BANK_NAME"));
        parameters.put("bik", arguments.get("BIK"));
        parameters.put("correspondentBankAccount", arguments.get("SINGLE_TREASURY_ACCOUNT"));

        PayerInfo payerInfo = getPayerIdTypeAndPayerIdNum(component, applicantType, scenarioDto.getCurrentValue(), scenarioDto.getApplicantAnswers());
        parameters.put("payerIdType", payerInfo.getIdType());
        parameters.put("payerIdNum", payerInfo.getIdNum());

        Person person = userPersonalData.getPerson();
        parameters.put("payerName", person.getLastName() + " " + person.getFirstName() + " " + person.getMiddleName());
        parameters.put("returnUrl", scenarioDto.getCurrentUrl());
        return parameters;
    }

    ComponentResponse<String> createNewBill(Map<String, Object> parameters, int callCount) {
        String token = userPersonalData.getToken();
        ImportBillNewResponse importBillNewResponse =
                billingService.getNewBillNumber(token, parameters);
        if (importBillNewResponse.getErrorCode() != 0) {
            return ComponentResponse.of(jsonProcessingService.toJson(importBillNewResponse));
        }
        String requestId = importBillNewResponse.getRequestId();

        ImportBillStatusResponse importBillStatusResponse = getBillStatus(requestId);
        Integer errorCode = importBillStatusResponse.getErrorCode();

        if (errorCode == 4) {
            importBillStatusResponse = getBillStatus(requestId);
            errorCode = importBillStatusResponse.getErrorCode();
        }
        if (callCount == 0 && (errorCode == 1 || errorCode == 2)) {
            return createNewBill(parameters, 1);
        }
        if (errorCode == 0) {
            BillInfoResponseWrapper billInfo = billingService.getBillInfoByBillNumber(token, importBillStatusResponse.getBillNumber());
            return ComponentResponse.of(jsonProcessingService.toJson(billInfo));
        }
        return ComponentResponse.of(jsonProcessingService.toJson(importBillStatusResponse));
    }

    ImportBillStatusResponse getBillStatus(String requestId) {
        return billingService.getBillStatus(userPersonalData.getToken(), requestId);
    }
}