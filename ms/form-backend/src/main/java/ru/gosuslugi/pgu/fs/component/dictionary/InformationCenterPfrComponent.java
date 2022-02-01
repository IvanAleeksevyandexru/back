package ru.gosuslugi.pgu.fs.component.dictionary;

import com.fasterxml.jackson.core.type.TypeReference;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import ru.gosuslugi.pgu.dto.ApplicantAnswer;
import ru.gosuslugi.pgu.dto.ScenarioDto;
import ru.gosuslugi.pgu.common.core.json.JsonProcessingUtil;
import ru.gosuslugi.pgu.dto.descriptor.FieldComponent;
import ru.gosuslugi.pgu.dto.descriptor.types.ComponentType;
import ru.gosuslugi.pgu.components.descriptor.types.RegistrationAddress;
import ru.gosuslugi.pgu.fs.common.component.AbstractComponent;
import ru.gosuslugi.pgu.fs.common.component.validation.RequiredNotBlankValidation;
import ru.gosuslugi.pgu.fs.common.component.validation.ValidationRule;
import ru.gosuslugi.pgu.fs.common.service.JsonProcessingService;
import ru.gosuslugi.pgu.fs.common.utils.AnswerUtil;
import ru.gosuslugi.pgu.fs.component.dictionary.dto.ToPfrServiceCode;
import ru.gosuslugi.pgu.fs.service.LkNotifierService;
import ru.gosuslugi.pgu.fs.utils.ParseAttrValuesHelper;
import ru.gosuslugi.pgu.pgu_common.nsi.dto.NsiDictionary;
import ru.gosuslugi.pgu.pgu_common.nsi.dto.NsiDictionaryItem;
import ru.gosuslugi.pgu.pgu_common.nsi.dto.filter.*;
import ru.gosuslugi.pgu.pgu_common.nsi.service.NsiDictionaryService;

import java.util.*;
import java.util.stream.Collectors;

import static org.springframework.util.StringUtils.hasText;
import static ru.gosuslugi.pgu.components.ComponentAttributes.ATTRIBUTE_VALUES;
import static ru.gosuslugi.pgu.components.ComponentAttributes.DICTIONARY_NAME_ATTR;

@Slf4j
@Component
@RequiredArgsConstructor
public class InformationCenterPfrComponent extends AbstractComponent<String> {

    private static final String TO_PFR_DICTIONARY_NAME = "TO_PFR";
    private static final String ADDRESS_ATTRIBUTE_NAME = "addressString";
    private static final String SERVICE_CODE_ATTRIBUTE_NAME = "serviceCode";
    private static final String OKATO_8_DICRIONARY_FILTER_PREFIX = "okato8_";
    private static final String OKATO_5_DICRIONARY_FILTER_PREFIX = "okato5_";

    private final NsiDictionaryService nsiDictionaryService;
    private final ParseAttrValuesHelper parseAttrValuesHelper;
    private final JsonProcessingService jsonProcessingService;
    private final LkNotifierService lkNotifierService;


    @Override
    public ComponentType getType() {
        return ComponentType.InformationCenterPfr;
    }


    @Override
    protected void preProcess(FieldComponent component, ScenarioDto scenarioDto) {
        if (Objects.nonNull(component.getAttrs()) && component.getAttrs().containsKey(ADDRESS_ATTRIBUTE_NAME)) {
            ToPfrServiceCode serviceCode = JsonProcessingUtil.getObjectMapper().convertValue(component.getAttrs().get(SERVICE_CODE_ATTRIBUTE_NAME), ToPfrServiceCode.class);
            String dictionaryName = getDictionaryName(component);
            String okato = getOkatoFromAddress(component, scenarioDto);

            // ищем отделение у которого первые 8 цифр окато совпадают с нужным
            NsiDictionaryItem pfrDepartment = findPfrByOkatoAndServiceCode(dictionaryName, okato, serviceCode, 8);

            if (Objects.isNull(pfrDepartment)) {
                // по 8 цифрам единственное отделение не нашлось, ищем по 5 первым цифрам окато
                pfrDepartment = findPfrByOkatoAndServiceCode(dictionaryName, okato, serviceCode, 5);
            }

            putPfrInComponentAttrs(pfrDepartment, component);
        }
    }

    private NsiDictionaryItem findPfrByOkatoAndServiceCode(String dictionaryName, String okato, ToPfrServiceCode serviceCode, int numberOfDigits) {
        NsiDictionaryFilterRequest requestBody = getFilterRequestForOkatoX(okato, numberOfDigits);
        NsiDictionary dictionary = nsiDictionaryService.getDictionary(dictionaryName, requestBody);
        filterDictionaryByServiceCode(dictionary, serviceCode);

        if (dictionary.getItems().size() == 1) {
            return dictionary.getItems().get(0);
        }
        return null;
    }

    private void filterDictionaryByServiceCode(NsiDictionary dictionary, ToPfrServiceCode serviceCode) {
        if (Objects.nonNull(serviceCode) && StringUtils.hasText(serviceCode.getValue()) && StringUtils.hasText(serviceCode.getType())) {
            dictionary.setItems(dictionary.getItems().stream().filter(item -> serviceCode.getValue().equals(item.getAttributeValues().get(serviceCode.getType()))).collect(Collectors.toList()));
        }
    }

    private void putPfrInComponentAttrs(NsiDictionaryItem pfrDepartment, FieldComponent component) {
        if (Objects.nonNull(pfrDepartment)) {
            Map<String, Object> simpleEntry = (LinkedHashMap<String, Object>) component.getAttrs().get("simple");
            if (Objects.isNull(simpleEntry)) {
                simpleEntry = new LinkedHashMap<>();
                component.getAttrs().put("simple", simpleEntry);
            }
            simpleEntry.put("items", Collections.singletonList(pfrDepartment));
        }
    }

    private NsiDictionaryFilterRequest getFilterRequestForOkatoX(String okato, int numberOfDigits) {
        if (numberOfDigits != 5 && numberOfDigits != 8) {
            // по сути никогда не должно происходить, просто на всякий случай
            log.warn("Trying to construct filter for not existed property for dictionary " + TO_PFR_DICTIONARY_NAME);
            return null;
        }
        if (!StringUtils.hasText(okato) || okato.length() < numberOfDigits) {
            // можно было бы кинуть эксепшн, но лучше дать пользователю в таком случае выбрать отделение вручную на фронте
            log.warn("Trying to construct filter for not existed property for dictionary " + TO_PFR_DICTIONARY_NAME);
            return null;
        }
        String okatoFilterValue = okato.substring(0, numberOfDigits);
        List<NsiDictionaryFilter> subs = new ArrayList<>();
        int numberOfFilters = numberOfDigits == 5 ? 6 : 7;
        for (int i = 1; i <= numberOfFilters; i++) {
            // в словаре для 5 первых цифр окато есть 6 параметров okato5_1 ... okato5_6
            // в словаре для 8 первых цифр окато есть 7 параметров okato8_1 ... okato8_7
            String attributeName = numberOfDigits == 5 ? OKATO_5_DICRIONARY_FILTER_PREFIX + i : OKATO_8_DICRIONARY_FILTER_PREFIX + i;
            NsiDictionaryFilterSimple simple = new NsiDictionaryFilterSimple.Builder()
                    .setAttributeName(attributeName)
                    .setCondition(NsiFilterCondition.EQUALS.toString())
                    .setStringValue(okatoFilterValue)
                    .build();

            NsiSimpleDictionaryFilterContainer simpleContainer = new NsiSimpleDictionaryFilterContainer();
            simpleContainer.setSimple(simple);
            subs.add(simpleContainer);
        }

        NsiDictionaryFilterUnion union = new NsiDictionaryFilterUnion();
        union.setUnionKind(NsiDictionaryUnionType.OR);
        union.setSubs(subs);

        NsiUnionDictionaryFilterContainer dictionaryFilter = new NsiUnionDictionaryFilterContainer();
        dictionaryFilter.setUnion(union);

        return new NsiDictionaryFilterRequest.Builder()
                .setTreeFiltering("ONELEVEL")
                .setPageNum("1")
                .setPageSize("1000")
                .setSelectAttributes(List.of("*"))
                .setFilter(dictionaryFilter)
                .build();
    }

    private String getOkatoFromAddress(FieldComponent component, ScenarioDto scenarioDto) {
        var addressAttr = component.getAttrs().get(ADDRESS_ATTRIBUTE_NAME);
        if (Objects.nonNull(addressAttr)) {
            Map<String, String> addressRefMap = (Map<String, String>) addressAttr;
            String address = parseAttrValuesHelper.getAttributeValue(addressRefMap, scenarioDto);
            RegistrationAddress registrationAddress = JsonProcessingUtil.fromJson(address, RegistrationAddress.class);
            if (Objects.nonNull(registrationAddress) && Objects.nonNull(registrationAddress.getRegAddr())) {
                return registrationAddress.getRegAddr().getOkato();
            }
        }
        return null;
    }

    @Override
    public List<ValidationRule> getValidations() {
        return List.of(
                new RequiredNotBlankValidation("Поле обязательно для заполнения")
        );
    }

    @Override
    protected void validateAfterSubmit(Map<String, String> incorrectAnswers, Map.Entry<String, ApplicantAnswer> entry, FieldComponent fieldComponent) {
        String answer = AnswerUtil.getValue(entry);
        String dictionaryName = getDictionaryName(fieldComponent);
        Map<String, Object> answerMap = jsonProcessingService.fromJson(answer, new TypeReference<>() {});
        Map<String, Object> pfr = (Map<String, Object>) answerMap.getOrDefault("territory", answerMap);
        String id = (String) pfr.get("id");
        Optional<NsiDictionaryItem> dictionaryItem = nsiDictionaryService.getDictionaryItemByValue(dictionaryName, "value", id);
        if (dictionaryItem.isEmpty()) {
            incorrectAnswers.put(entry.getKey(),
                    String.format("NSI dictionary %s doesn't contain %s item", dictionaryName, entry.getValue().getValue()));
        }

    }

    private String getDictionaryName(FieldComponent component) {
        if (component.getAttrs() != null) {
            return component.getAttrs().getOrDefault(DICTIONARY_NAME_ATTR, TO_PFR_DICTIONARY_NAME).toString();
        }
        return TO_PFR_DICTIONARY_NAME;
    }

    @Override
    protected void postProcess(Map.Entry<String, ApplicantAnswer> entry, ScenarioDto scenarioDto, FieldComponent fieldComponent) {
        if(fieldComponent.isSendAnalytics()){
            String answer = AnswerUtil.getValue(entry);
            Map<String, Object> answerMap = jsonProcessingService.fromJson(answer, new TypeReference<>() {});
            Map<String, Object> territory = (Map<String, Object>) answerMap.getOrDefault("territory", answerMap);
            Map<String, Object> originalItem = (Map<String, Object>) territory.getOrDefault("originalItem", answerMap);
            Map<String, Object> attributeValues = (Map<String, Object>) originalItem.get(ATTRIBUTE_VALUES);
            lkNotifierService.updateOrderRegion(scenarioDto.getOrderId(),
                    String.valueOf(attributeValues.get(OKATO_8_DICRIONARY_FILTER_PREFIX + 1)));
        }
    }
}
