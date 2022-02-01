package ru.gosuslugi.pgu.pgu_common.payment.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import ru.gosuslugi.pgu.pgu_common.payment.dto.pay.PaymentRequisites;
import ru.gosuslugi.pgu.pgu_common.payment.dto.pay.OrganizationRequisites;

import java.util.Map;

@Mapper
public interface DictionaryResponseMapper {

    @Mapping(expression = "java( createContainer(\"Name\",attrs.get(\"DIV_NAME\")) )", target = "name")
    @Mapping(expression = "java( createContainer(\"PayeeINN\",attrs.get(\"INN\")) )", target = "payeeINN")
    @Mapping(expression = "java( createContainer(\"KPP\",attrs.get(\"KPP\")) )", target = "kpp")
    @Mapping(expression = "java( createContainer(\"BIC\",attrs.get(\"BIK\")) )", target = "bic")
    @Mapping(expression = "java( createContainer(\"BankName\",attrs.get(\"BANK\")) )", target = "bankName")
    @Mapping(expression = "java( createContainer(\"PersonalAcc\",attrs.get(\"RS\")) )", target = "personalAcc")
    @Mapping(expression = "java( createContainer(\"oktmo\",attrs.get(\"OKTMO\")) )", target = "oktmo")
    @Mapping(expression = "java( createContainer(\"CorrAccount\",attrs.get(\"CORR\")) )", target = "corr")
    OrganizationRequisites mapOrganizationRequisites(Map<String, String> attrs);

    @Mapping(expression = "java( createContainer(\"Name\",attrs.get(\"Naimenovanie_podrazdelenia\")) )", target = "name")
    @Mapping(expression = "java( createContainer(\"PayeeINN\",attrs.get(\"INN\")) )", target = "payeeINN")
    @Mapping(expression = "java( createContainer(\"KPP\",attrs.get(\"KPP\")) )", target = "kpp")
    @Mapping(expression = "java( createContainer(\"BIC\",attrs.get(\"BIK\")) )", target = "bic")
    @Mapping(expression = "java( createContainer(\"BankName\",attrs.get(\"Naimenovanie_banka\")) )", target = "bankName")
    @Mapping(expression = "java( createContainer(\"PersonalAcc\",attrs.get(\"KS\")) )", target = "personalAcc")
    @Mapping(expression = "java( createContainer(\"oktmo\",attrs.get(\"OKTMO\")) )", target = "oktmo")
    @Mapping(expression = "java( createContainer(\"CorrAccount\",attrs.get(\"ES\")) )", target = "corr")
    OrganizationRequisites mapOrganizationRequisitesV2(Map<String, String> attrs);

    @Mapping(expression = "java( createContainer(\"CBC\",attrs.get(\"KBK\")) )", target = "cbc")
    @Mapping(expression = "java( createContainer(\"Purpose\",attrs.get(\"PURPORSE\")) )", target = "purpose")
    @Mapping(expression = "java( createContainer(\"DrawerStatus\",attrs.get(\"DRAWER_STATUS\")) )", target = "drawerStatus")
    @Mapping(expression = "java( createContainer(\"PaytReason\",attrs.get(\"PAYT_REASON\")) )", target = "paytReason")
    @Mapping(expression = "java( createContainer(\"TaxPeriod\",attrs.get(\"TAX_PERIOD\")) )", target = "taxPeriod")
    @Mapping(expression = "java( createContainer(\"DocDate\",attrs.get(\"DOC_DATE\")) )", target = "docDate")
    @Mapping(expression = "java( createContainer(\"TaxPaytKind\",attrs.get(\"TAX_PAY_KIND\")) )", target = "taxPaytKind")
    @Mapping(expression = "java( createContainer(\"DocNumber\",attrs.get(\"DOC_NUMBER\")) )", target = "docNumber")
    PaymentRequisites mapPaymentRequisites(Map<String, String> attrs);

    default NameValueContainer createContainer(String name, String value) {
        return new NameValueContainer(name, value);
    }
}