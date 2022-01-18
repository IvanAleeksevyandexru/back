package ru.gosuslugi.pgu.fs.utils;

import lombok.experimental.UtilityClass;
import ru.gosuslugi.pgu.dto.descriptor.FieldComponent;
import ru.gosuslugi.pgu.fs.component.userdata.model.MedicalItemsFilter;
import ru.gosuslugi.pgu.fs.component.userdata.model.MedicalItemsRequest;
import ru.gosuslugi.pgu.fs.component.userdata.model.MedicalItemsUnion;
import ru.gosuslugi.pgu.fs.component.userdata.model.MedicalSimpleFilter;
import ru.gosuslugi.pgu.fs.component.userdata.model.MedicalSimpleItem;
import ru.gosuslugi.pgu.fs.component.userdata.model.ParamsItem;

import java.util.Arrays;
import java.util.List;

import static ru.gosuslugi.pgu.components.ComponentAttributes.ESERVICE_ID;
import static ru.gosuslugi.pgu.components.ComponentAttributes.MEDICAL_POS_ATTRIBUTES;
import static ru.gosuslugi.pgu.components.ComponentAttributes.MEDICAL_POS_REFNAME;
import static ru.gosuslugi.pgu.components.ComponentAttributes.SESSION_ID;

/**
 *  Утилитарный класс для вспомогательных методов рабоботающих с {@link MedicalItemsRequest} и другими классами пакета.
 *  @see ru.gosuslugi.pgu.fs.component.userdata.model
 */
@UtilityClass
public class MedicineServiceUtils {

    private final String REFERRAL_ID_ARGUMENT_KEY = "Referral_Id";
    private final String REFERRAL_MO_OID_ARGUMENT_KEY = "MO_OID";
    private final String SERVICE_ID_ARGUMENT_KEY = "Service_id";

    private MedicalSimpleItem  createSimpleItem(String key, String value, Boolean checkAllValues){
        return  MedicalSimpleItem.builder()
                .attributeName(key)
                .checkAllValues(checkAllValues)
                .condition("EQUALS")
                .value(value)
                .build();
    }

    private MedicalItemsRequest createRequestWithSimpleFilters(MedicalItemsFilter filters, FieldComponent component){
        return MedicalItemsRequest.builder()
                .eserviceId(component.getArgument(ESERVICE_ID))
                .refName(MEDICAL_POS_REFNAME)
                .parentRefItemValue(null)
                .treeFiltering("ONELEVEL")
                .filter(filters)
                .params(List.of(
                        ParamsItem
                                .builder()
                                .name("region")
                                .value(component.getArguments().get("regionCode"))
                                .build()
                        ,
                        ParamsItem
                                .builder()
                                .name("orderId")
                                .value(component.getArguments().get("orderId"))
                                .build()))
                .selectAttributes(MEDICAL_POS_ATTRIBUTES)
                .userSelectedRegion(component.getArgument("checkRegionCode"))
                .pageSize(0)
                .pageNum(0)
                .build();

    }

    public MedicalItemsRequest createRequestByReferral(FieldComponent component){
        var referralItem = createSimpleItem(REFERRAL_ID_ARGUMENT_KEY, component.getArgument(REFERRAL_ID_ARGUMENT_KEY), Boolean.FALSE);
        var sessionIdItem = createSimpleItem(SESSION_ID, component.getArgument(SESSION_ID), Boolean.FALSE);
        var serviceIdItem = createSimpleItem(SERVICE_ID_ARGUMENT_KEY, component.getArgument(SERVICE_ID_ARGUMENT_KEY), Boolean.TRUE);
        var moOidItem = createSimpleItem(REFERRAL_MO_OID_ARGUMENT_KEY, component.getArgument(REFERRAL_MO_OID_ARGUMENT_KEY), Boolean.TRUE);

        var simpleFilters = Arrays.asList(
                MedicalSimpleFilter
                        .builder()
                        .simple(referralItem)
                        .simple(sessionIdItem)
                        .simple(serviceIdItem)
                        .simple(moOidItem)
                        .build());
        MedicalItemsUnion medicalItemsUnion = MedicalItemsUnion.builder().subs(simpleFilters).unionKind("AND").build();
        MedicalItemsFilter medicalItemsFilter = MedicalItemsFilter.builder().union(medicalItemsUnion).build();
        return createRequestWithSimpleFilters(medicalItemsFilter, component);

    }

    public MedicalItemsRequest createRequestNonReferral(FieldComponent component){
        var sessionIdItem = createSimpleItem(SESSION_ID, component.getArgument(SESSION_ID), Boolean.FALSE);
        var serviceIdItem = createSimpleItem(SERVICE_ID_ARGUMENT_KEY, component.getArgument(SERVICE_ID_ARGUMENT_KEY), Boolean.FALSE);

        var simpleFilters = Arrays.asList(
                MedicalSimpleFilter
                        .builder()
                        .simple(sessionIdItem)
                        .simple(serviceIdItem)
                        .build());
        MedicalItemsUnion medicalItemsUnion = MedicalItemsUnion.builder().subs(simpleFilters).unionKind("AND").build();
        MedicalItemsFilter medicalItemsFilter = MedicalItemsFilter.builder().union(medicalItemsUnion).build();
        return createRequestWithSimpleFilters(medicalItemsFilter, component);
    }

}
