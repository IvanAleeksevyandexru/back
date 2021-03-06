package ru.gosuslugi.pgu.fs.component.address;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.jayway.jsonpath.PathNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import ru.gosuslugi.pgu.common.core.exception.ValidationException;
import ru.gosuslugi.pgu.components.ValidationUtil;
import ru.gosuslugi.pgu.components.descriptor.types.FullAddress;
import ru.gosuslugi.pgu.components.descriptor.types.ValidationFieldDto;
import ru.gosuslugi.pgu.components.dto.ErrorDto;
import ru.gosuslugi.pgu.dto.ApplicantAnswer;
import ru.gosuslugi.pgu.dto.ScenarioDto;
import ru.gosuslugi.pgu.dto.descriptor.FieldComponent;
import ru.gosuslugi.pgu.fs.common.component.AbstractComponent;
import ru.gosuslugi.pgu.fs.service.LkNotifierService;
import ru.gosuslugi.pgu.pgu_common.nsi.dto.DadataAddressResponse;
import ru.gosuslugi.pgu.pgu_common.nsi.dto.NsiDictionary;
import ru.gosuslugi.pgu.pgu_common.nsi.dto.filter.NsiDictionaryFilterRequest;
import ru.gosuslugi.pgu.pgu_common.nsi.dto.filter.NsiDictionaryFilterSimple;
import ru.gosuslugi.pgu.pgu_common.nsi.dto.filter.NsiDictionaryFilterUnion;
import ru.gosuslugi.pgu.pgu_common.nsi.dto.filter.NsiDictionaryUnionType;
import ru.gosuslugi.pgu.pgu_common.nsi.dto.filter.NsiSimpleDictionaryFilterContainer;
import ru.gosuslugi.pgu.pgu_common.nsi.dto.filter.NsiUnionDictionaryFilterContainer;
import ru.gosuslugi.pgu.pgu_common.nsi.service.NsiDadataService;
import ru.gosuslugi.pgu.pgu_common.nsi.service.NsiDictionaryService;

import java.util.List;
import java.util.Map;
import java.util.Objects;

import static java.util.Objects.isNull;
import static org.apache.commons.lang.StringUtils.isBlank;
import static org.apache.commons.lang.StringUtils.isNumeric;
import static ru.gosuslugi.pgu.components.FieldComponentUtil.FIELDS_KEY;

@Slf4j
public abstract class AbstractAddressComponent<PreSetModel> extends AbstractComponent<PreSetModel> {

    protected Logger logger = LoggerFactory.getLogger(AbstractFullAddressComponent.class);

    /**
     * ?????????? ?????????????????? ?????????????? ?? ????.
     */
    private static final int ZIP_CODE_LENGTH = 6;

    private static final String OKTMO_FULL_ADDR = "oktmoName";
    private static final String OKTMO_DB_NAME = "ADM_MEGAPOLIS_OKTMO";

    @Autowired
    private NsiDadataService nsiDadataService;

    @Autowired
    private NsiDictionaryService nsiDictionaryService;

    @Autowired
    protected LkNotifierService lkNotifierService;


    @Override
    protected void postProcess(Map.Entry<String, ApplicantAnswer> entry, ScenarioDto scenarioDto, FieldComponent fieldComponent) {
        FullAddress fullAddress = getFullAddress(entry);
        if (Objects.nonNull(fullAddress) && fieldComponent.isSendAnalytics()) {
            lkNotifierService.updateOrderRegion(scenarioDto.getOrderId(), fullAddress.getOkato());
        }
    }

    @Override
    protected void validateAfterSubmit(Map<String, String> incorrectAnswers, Map.Entry<String, ApplicantAnswer> entry, FieldComponent fieldComponent) {
        FullAddress fullAddress = null;
        try {
            fullAddress = getFullAddress(entry);
        } catch (PathNotFoundException ex) {
            log.error("???? ?????????????? ???????????????????? ???????????? ?????????? ???? ????????????????: {}", entry.getValue());
        }

        updateIncorrectAnswers(incorrectAnswers,fullAddress,entry,fieldComponent);

        if (incorrectAnswers.isEmpty()) {
            if (fieldComponent.getAttrs() != null
                    && fieldComponent.getAttrs().containsKey(OKTMO_FULL_ADDR)
                    && (boolean) fieldComponent.getAttrs().get(OKTMO_FULL_ADDR)) {

                NsiDictionaryFilterRequest nsiDictionaryFilterRequest = formFilter(
                        fullAddress.getOktmo(),
                        "OKTMO",
                        "EQUALS",
                        NsiDictionaryUnionType.AND,
                        "1",
                        "258",
                        "",
                        List.of("*"),
                        "ONELEVEL"
                );

                NsiDictionary dictionary = nsiDictionaryService.getDictionary(OKTMO_DB_NAME, nsiDictionaryFilterRequest);
                if (dictionary != null && Objects.nonNull(dictionary.getItems()) && dictionary.getItems().size() != 0) {
                    addOktmoNameInfo(entry, dictionary.getItems().get(0).getTitle());
                }
            }
            enrichEntry(entry);
        }
    }

    protected void updateIncorrectAnswers(Map<String, String> incorrectAnswers,
                                          FullAddress fullAddress,
                                          Map.Entry<String, ApplicantAnswer> entry,
                                          FieldComponent fieldComponent
    ){
        DadataAddressResponse addressResponse;

        if (isNull(fullAddress) || isBlank(fullAddress.getFullAddress())) {
            if (fieldComponent.isRequired()) {
                incorrectAnswers.put(entry.getKey(), "?????????? ???? ??????????");
            }
            return;
        }
        String zipCode = fullAddress.getIndex();
        if (!isNumeric(zipCode) || zipCode.length() != ZIP_CODE_LENGTH) {
            incorrectAnswers.put(entry.getKey(), "???????????? ??????????????????????, ?????????????????? " + ZIP_CODE_LENGTH + " ????????");
            return;
        }
        if (isBlank(fullAddress.getFiasCode()) || isBlank(fullAddress.getOktmo())) {
            addressResponse = nsiDadataService.getAddress(fullAddress.getFullAddress());

            if (!addMetaInfo(entry, addressResponse)) {
                incorrectAnswers.put(entry.getKey(), "?????????? ???? ??????????????????");
                return;
            }
            fullAddress.setOktmo(addressResponse.getOktmo());
        }

        List<ValidationFieldDto> validationFieldDto = objectMapper.convertValue(
                fieldComponent.getAttrs().get(FIELDS_KEY),
                new TypeReference<>() {}
        );
        Map<String, ErrorDto> errors;
        try {
            errors = ValidationUtil.validateFieldsByRegExp(
                    incorrectAnswers,
                    entry.getValue().getValue(),
                    validationFieldDto
            );
        } catch (JsonProcessingException e) {
            throw new ValidationException("???????????? ?????? ?????????????? ?????????????????? ????????????", e);
        }
        if (!errors.isEmpty()) {
            incorrectAnswers.put(entry.getKey(), jsonProcessingService.toJson(errors));
        }
    }

    public abstract boolean addMetaInfo(Map.Entry<String, ApplicantAnswer> entry, DadataAddressResponse addressResponse);

    public abstract void addOktmoNameInfo(Map.Entry<String, ApplicantAnswer> entry, String oktmoName);

    public abstract void enrichEntry(Map.Entry<String, ApplicantAnswer> entry);

    public abstract FullAddress getFullAddress(Map.Entry<String, ApplicantAnswer> entry);

    public NsiDictionaryFilterRequest formFilter(String oktmo, String attribName, String condition,
                                                 NsiDictionaryUnionType dictionaryUnionType, String pageNum,
                                                 String pageSize, String parentRefItemValue, List<String> selectAttributes,
                                                 String treeFiltering) {
        NsiDictionaryFilterSimple nsiDictionaryFilterSimple = new NsiDictionaryFilterSimple.Builder()
                .setAttributeName(attribName)
                .setCondition(condition)
                .setStringValue(oktmo)
                .build();

        NsiSimpleDictionaryFilterContainer nsiSimpleDictionaryFilterContainer = new NsiSimpleDictionaryFilterContainer();
        nsiSimpleDictionaryFilterContainer.setSimple(nsiDictionaryFilterSimple);

        NsiDictionaryFilterUnion union = new NsiDictionaryFilterUnion();
        union.setSubs(List.of(nsiSimpleDictionaryFilterContainer));
        union.setUnionKind(dictionaryUnionType);

        NsiUnionDictionaryFilterContainer nsiDictionaryFilter = new NsiUnionDictionaryFilterContainer();
        nsiDictionaryFilter.setUnion(union);

        NsiDictionaryFilterRequest nsiDictionaryFilterRequest = new NsiDictionaryFilterRequest();
        nsiDictionaryFilterRequest.setFilter(nsiDictionaryFilter);
        nsiDictionaryFilterRequest.setPageNum(pageNum);
        nsiDictionaryFilterRequest.setPageSize(pageSize);
        nsiDictionaryFilterRequest.setParentRefItemValue(parentRefItemValue);
        nsiDictionaryFilterRequest.setSelectAttributes(selectAttributes);
        nsiDictionaryFilterRequest.setTreeFiltering(treeFiltering);

        return nsiDictionaryFilterRequest;
    }

}
