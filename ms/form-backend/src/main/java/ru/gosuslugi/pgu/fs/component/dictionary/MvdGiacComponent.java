package ru.gosuslugi.pgu.fs.component.dictionary;

import com.fasterxml.jackson.core.type.TypeReference;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import ru.gosuslugi.pgu.common.core.exception.JsonParsingException;
import ru.gosuslugi.pgu.common.core.json.JsonProcessingUtil;
import ru.gosuslugi.pgu.components.descriptor.types.RegistrationAddress;
import ru.gosuslugi.pgu.dto.ApplicantAnswer;
import ru.gosuslugi.pgu.dto.ScenarioDto;
import ru.gosuslugi.pgu.dto.descriptor.FieldComponent;
import ru.gosuslugi.pgu.dto.descriptor.types.ComponentType;
import ru.gosuslugi.pgu.fs.common.component.AbstractComponent;
import ru.gosuslugi.pgu.fs.common.component.validation.NotBlankValidation;
import ru.gosuslugi.pgu.fs.common.component.validation.ValidationRule;
import ru.gosuslugi.pgu.fs.common.service.JsonProcessingService;
import ru.gosuslugi.pgu.fs.common.utils.AnswerUtil;
import ru.gosuslugi.pgu.fs.service.LkNotifierService;
import ru.gosuslugi.pgu.fs.utils.ParseAttrValuesHelper;
import ru.gosuslugi.pgu.pgu_common.nsi.dto.NsiDictionary;
import ru.gosuslugi.pgu.pgu_common.nsi.dto.NsiDictionaryItem;
import ru.gosuslugi.pgu.pgu_common.nsi.dto.filter.NsiDictionaryFilterRequest;
import ru.gosuslugi.pgu.pgu_common.nsi.dto.filter.NsiDictionaryFilterSimple;
import ru.gosuslugi.pgu.pgu_common.nsi.dto.filter.NsiFilterCondition;
import ru.gosuslugi.pgu.pgu_common.nsi.dto.filter.NsiSimpleDictionaryFilterContainer;
import ru.gosuslugi.pgu.pgu_common.nsi.service.NsiDictionaryService;

import java.util.*;

import static org.springframework.util.StringUtils.hasText;
import static ru.gosuslugi.pgu.components.ComponentAttributes.*;
import static ru.gosuslugi.pgu.components.FieldComponentUtil.DICTIONARY_LIST_KEY;

@Slf4j
@Component
@RequiredArgsConstructor
public class MvdGiacComponent extends AbstractComponent<String> {

    private static final String REG_ADDRESS_ATTRIBUTE_NAME = "regAddressString";
    private static final String FACT_ADDRESS_ATTRIBUTE_NAME = "factAddressString";
    private static final String IS_ON_PAPER_ARGUMENT_NAME = "isOnPaper";
    private static final String INFCENTRE_MVD_ADRESS = "INFCENTRE_MVD_ADRESS";
    private static final String CODE_INFCENTRE = "CODE_INFCENTRE";
    private static final String REGION_CODE = "REGION_CODE";
    private static final String ID = "ID";
    public static final String BAIKONUR = "Байконур";
    public static final String MOSCOW_REGION_CODE = "099";
    public static final String MVD_GIAC_REGION_CODE = "000";

    private final NsiDictionaryService nsiDictionaryService;
    private final ParseAttrValuesHelper parseAttrValuesHelper;
    private final JsonProcessingService jsonProcessingService;
    private final LkNotifierService lkNotifierService;

    @Override
    public ComponentType getType() {
        return ComponentType.MvdGiac;
    }

    @Override
    protected void preProcess(FieldComponent component, ScenarioDto scenarioDto) {
        List<NsiDictionaryItem> mvdDepartments = getMvdDepartments(component, scenarioDto);
        component.getAttrs().put(DICTIONARY_LIST_KEY, mvdDepartments);
    }

    private List<NsiDictionaryItem> getMvdDepartments(FieldComponent component, ScenarioDto scenarioDto) {
        String dictionaryName = getDictionaryName(component);
        if (Objects.nonNull(component.getAttrs()) && component.getAttrs().containsKey(REG_ADDRESS_ATTRIBUTE_NAME)) {

            String regAddressRegion = getRegionCodeFromAddress(REG_ADDRESS_ATTRIBUTE_NAME, component, scenarioDto);
            String factAddressRegion = getRegionCodeFromAddress(FACT_ADDRESS_ATTRIBUTE_NAME, component, scenarioDto);
            if (Objects.isNull(factAddressRegion)) {
                factAddressRegion = regAddressRegion;
            }
            boolean isPaperDocumentRequested = isPaperDocumentRequested(component);

            if (Objects.nonNull(regAddressRegion)) {
                if (regAddressRegion.equals(factAddressRegion) && !isPaperDocumentRequested) {
                    // один и тот же регион, эллектронная справка -> выдаём только отделение в этом регионе
                    NsiSimpleDictionaryFilterContainer filter = new NsiSimpleDictionaryFilterContainer();
                    NsiDictionaryFilterSimple simpleFilter =
                            new NsiDictionaryFilterSimple.Builder()
                                    .setAttributeName(CODE_INFCENTRE)
                                    .setStringValue(regAddressRegion)
                                    .setCondition(NsiFilterCondition.EQUALS.toString()).build();
                    filter.setSimple(simpleFilter);
                    NsiDictionaryFilterRequest requestBody = new NsiDictionaryFilterRequest.Builder()
                            .setFilter(filter)
                            .setPageNum("1")
                            .setPageSize("1")
                            .build();
                    NsiDictionary dictionary = nsiDictionaryService.getDictionary(dictionaryName, requestBody);
                    if (!dictionary.getItems().isEmpty()) {
                        return dictionary.getItems();
                    }
                }
                // выдаём весь список, где первые элементы это отделения по месту регистрации и месту пребывания

                NsiDictionaryFilterRequest requestBody = new NsiDictionaryFilterRequest.Builder()
                        .setPageNum("1")
                        .setPageSize("100")
                        .build();
                NsiDictionary dictionary = nsiDictionaryService.getDictionary(dictionaryName, requestBody);
                if (isPaperDocumentRequested && regAddressRegion.equals(factAddressRegion) && MOSCOW_REGION_CODE.equals(regAddressRegion)) {
                    // при бумажной справке и совпадающих регионах, где регион == Москва на 2 позицию ставим ГИАЦ МВД России
                    return sortDepartments(dictionary.getItems(), factAddressRegion, MVD_GIAC_REGION_CODE);
                }
                return sortDepartments(dictionary.getItems(), factAddressRegion, regAddressRegion);
            }
        }
        // отдаём всё
        NsiDictionaryFilterRequest requestBody = new NsiDictionaryFilterRequest.Builder()
                .setPageNum("1")
                .setPageSize("100")
                .build();
        NsiDictionary dictionary = nsiDictionaryService.getDictionary(dictionaryName, requestBody);
        return dictionary.getItems();
    }

    private List<NsiDictionaryItem> sortDepartments(List<NsiDictionaryItem> departments, String factAddressRegion, String regAddressRegion) {
        departments.sort((item1, item2) -> {
            String regionCodes1 = item1.getAttributeValues().get(REGION_CODE);
            String regionCodes2 = item2.getAttributeValues().get(REGION_CODE);
            if (regionCodes1.equals(regionCodes2)) {
                return 0;
            }
            if (regionCodes1.contains(factAddressRegion)) {
                return -1;
            }
            if (regionCodes2.contains(factAddressRegion)) {
                return 1;
            }
            if (regionCodes1.contains(regAddressRegion)) {
                return -1;
            }
            if (regionCodes2.contains(regAddressRegion)) {
                return 1;
            }
            return 0;

        });
        return departments;
    }

    private String getRegionCodeFromAddress(String addressAttrName, FieldComponent component, ScenarioDto scenarioDto) {
        var addressAttr = component.getAttrs().get(addressAttrName);
        if (Objects.nonNull(addressAttr)) {
            Map<String, String> addressRefMap = (Map<String, String>) addressAttr;
            String address = parseAttrValuesHelper.getAttributeValue(addressRefMap, scenarioDto);
            RegistrationAddress registrationAddress = getRegistrationAddressFromJsonString(address);
            if (Objects.nonNull(registrationAddress) && Objects.nonNull(registrationAddress.getRegAddr())) {
                if (BAIKONUR.equals(registrationAddress.getRegAddr().getRegion())) {
                    return MVD_GIAC_REGION_CODE;
                }
                String regionCode = registrationAddress.getRegAddr().getRegionCode();
                if ("77".equals(registrationAddress.getRegAddr().getRegionCode())) {
                    return MOSCOW_REGION_CODE;
                }
                if ("78".equals(registrationAddress.getRegAddr().getRegionCode())) {
                    return "047";
                }
                if ("91".equals(registrationAddress.getRegAddr().getRegionCode())) {
                    return "082";
                }
                if (Objects.nonNull(regionCode) && regionCode.length() <= 3 && regionCode.length() > 0) {
                    char[] missingZeroes = new char[3 - regionCode.length()];
                    Arrays.fill(missingZeroes, '0');
                    return new StringBuilder(regionCode).insert(0, missingZeroes).toString();
                }
            }
        }
        return null;
    }

    private boolean isPaperDocumentRequested(FieldComponent component) {
        return Optional.ofNullable(component.getArgument(IS_ON_PAPER_ARGUMENT_NAME))
                .map(Boolean::parseBoolean)
                .orElse(false);
    }

    private String getDictionaryName(FieldComponent component) {
        if (component.getAttrs() != null) {
            return component.getAttrs().getOrDefault(DICTIONARY_NAME_ATTR, INFCENTRE_MVD_ADRESS).toString();
        }
        return INFCENTRE_MVD_ADRESS;
    }

    @Override
    public List<ValidationRule> getValidations() {
        return List.of(
                new NotBlankValidation("Введите код подтверждения")
        );
    }

    @Override
    protected void validateAfterSubmit(Map<String, String> incorrectAnswers, Map.Entry<String, ApplicantAnswer> entry, FieldComponent fieldComponent) {
        String value = AnswerUtil.getValue(entry);
        Map<String, Object> answerMap = jsonProcessingService.fromJson(value, new TypeReference<>() {});
        String departmentId = (String) answerMap.get("id");
        Optional<NsiDictionaryItem> department = nsiDictionaryService.getDictionaryItemByValue(INFCENTRE_MVD_ADRESS, ID, departmentId);
        if (department.isEmpty()) {
            incorrectAnswers.put(fieldComponent.getId(), "Отделение не найдено");
        }
    }

    @Override
    public String getDefaultAnswer(FieldComponent component) {
        Object firstElement = ((List)component.getAttrs().get(DICTIONARY_LIST_KEY)).get(0);
        Map<String, Object> answer = Collections.singletonMap(ORIGINAL_ITEM, firstElement);
        return jsonProcessingService.toJson(answer);
    }

    /**
     * Конвертирует адрес регистрации из json объекта в объект класса {@link RegistrationAddress}
     * @param fullRegAddrJson json объекта адреса регистрации
     * @return адрес регистрации
     */
    private RegistrationAddress getRegistrationAddressFromJsonString(String fullRegAddrJson) {
        RegistrationAddress result = null;
        if (!StringUtils.hasText(fullRegAddrJson)) {
            return null;
        }
        try {
            result = JsonProcessingUtil.fromJson(fullRegAddrJson, RegistrationAddress.class);
        } catch (JsonParsingException e) {
            if (log.isWarnEnabled()) log.warn("Error by json parsing \"{}\" address to RegistrationAddress class. Details: {}", fullRegAddrJson, e.getMessage());
        }
        return result;
    }

    @Override
    protected void postProcess(Map.Entry<String, ApplicantAnswer> entry, ScenarioDto scenarioDto, FieldComponent fieldComponent) {
        if(fieldComponent.isSendAnalytics()){
            String address = Optional.ofNullable(entry)
                    .map(Map.Entry::getValue)
                    .map(ApplicantAnswer::getValue)
                    .map(this::getAddressFromValue)
                    .orElse(null);
            lkNotifierService.updateOrderRegionByAddress(scenarioDto.getOrderId(), address);
        }
    }

    private String getAddressFromValue(String value) {
        Map<String, Object> valueMap = jsonProcessingService.fromJson(value, new TypeReference<>() {});
        Map<String, Object> originalItem = (Map<String, Object>) valueMap.get(ORIGINAL_ITEM);
        Map<String, Object> attributeValues = (Map<String, Object>) originalItem.get(ATTRIBUTE_VALUES);
        return String.valueOf(attributeValues.get(ADDRESS_UPPERCASE));
    }
}
