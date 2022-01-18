package ru.gosuslugi.pgu.fs.component.dictionary;

import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.PathNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.minidev.json.JSONArray;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import ru.gosuslugi.pgu.fs.common.exception.FormBaseException;
import ru.gosuslugi.pgu.common.core.json.JsonProcessingUtil;
import ru.gosuslugi.pgu.common.sop.dto.SopDictionaryRequest;
import ru.gosuslugi.pgu.common.sop.dto.SopDictionaryResponse;
import ru.gosuslugi.pgu.common.sop.dto.SopResponseDataItem;
import ru.gosuslugi.pgu.common.sop.service.SopDictionaryService;
import ru.gosuslugi.pgu.common.sop.util.SopDictionaryRequestUtils;
import ru.gosuslugi.pgu.components.descriptor.types.RegistrationAddress;
import ru.gosuslugi.pgu.dto.ApplicantAnswer;
import ru.gosuslugi.pgu.dto.ScenarioDto;
import ru.gosuslugi.pgu.dto.descriptor.FieldComponent;
import ru.gosuslugi.pgu.dto.descriptor.types.ComponentType;
import ru.gosuslugi.pgu.fs.common.component.AbstractComponent;
import ru.gosuslugi.pgu.fs.common.component.validation.RequiredNotBlankValidation;
import ru.gosuslugi.pgu.fs.common.component.validation.ValidationRule;
import ru.gosuslugi.pgu.fs.common.service.JsonProcessingService;
import ru.gosuslugi.pgu.fs.component.dictionary.dto.SopDataItem;
import ru.gosuslugi.pgu.fs.utils.ParseAttrValuesHelper;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

import static ru.gosuslugi.pgu.common.sop.service.SopUid.PFR_DIVISIONS;
import static ru.gosuslugi.pgu.common.sop.service.SopUid.PFR_DIVISIONS_AREAS;

@Slf4j
@Component
@RequiredArgsConstructor
public class InformationCenterPfrSopComponent extends AbstractComponent<SopDataItem> {

    private static final String ADDRESS_ATTRIBUTE_NAME = "addressString";
    private static final String DIVISION_CODE = "divisionCode";
    private static final String AREA_OKATO = "areaOkato";
    private static final String SHORT_NAME = "shortName";

    private final SopDictionaryService sopDictionaryService;
    private final ParseAttrValuesHelper parseAttrValuesHelper;
    private final JsonProcessingService jsonProcessingService;

    @Override
    public ComponentType getType() {
        return ComponentType.InformationCenterPfrSop;
    }

    @Override
    protected void preProcess(FieldComponent component, ScenarioDto scenarioDto) {
        if (Objects.nonNull(component.getAttrs()) && component.getAttrs().containsKey(ADDRESS_ATTRIBUTE_NAME)) {
            // ОКАТО, введённое пользователем
            String okato = getOkatoFromAddress(component, scenarioDto);
            String okato8 = okato.substring(0, okato.length() - 3);

            SopDictionaryRequest request = SopDictionaryRequestUtils.requestWithQuery(PFR_DIVISIONS_AREAS,
                    Set.of(AREA_OKATO, DIVISION_CODE), Set.of(AREA_OKATO), okato8, 100, 0, false, false);
            // ищем дивизион у которого окато из 8 символов совпадает с нужным
            SopDictionaryResponse response = sopDictionaryService.findByRequest(request, okato8);
            List<SopResponseDataItem> dataItems = response.getData();
            String divisionCode = "";
            if(!CollectionUtils.isEmpty(dataItems) && dataItems.size() == 1) {
                SopResponseDataItem divisionItem = dataItems.get(0);
                divisionCode = divisionItem.getAttributeValue(DIVISION_CODE);
            } else {
                String okato5 = okato.substring(0, okato.length() - 6);
                request = SopDictionaryRequestUtils.requestWithQuery(PFR_DIVISIONS_AREAS,
                        Set.of(AREA_OKATO, DIVISION_CODE), Set.of(AREA_OKATO), okato5, 100, 0, false, false);
                response = sopDictionaryService.findByRequest(request, okato5);
                dataItems = response.getData();
                boolean empty = CollectionUtils.isEmpty(dataItems);
                if (!empty && dataItems.size() == 1) {
                    SopResponseDataItem divisionItem = dataItems.get(0);
                    divisionCode = divisionItem.getAttributeValue(DIVISION_CODE);
                } else if (empty) {
                    throw new FormBaseException(String.format("Должно возвращаться всегда единственное подразделение по окато %s, а не нашли ничего.", okato5));
                } else {
                    String okato5_6 = okato5 + "000000";
                    Optional<SopResponseDataItem> responseDataItem = dataItems.stream()
                            .filter(item -> {
                                String okato11 = item.getAttributeValue(AREA_OKATO);
                                String localDivisionCode = item.getAttributeValue(DIVISION_CODE);
                                return okato11.startsWith(okato5_6) && !StringUtils.isEmpty(localDivisionCode);
                            }).findFirst();
                    if (responseDataItem.isPresent()) {
                        divisionCode = responseDataItem.get().getAttributeValue(DIVISION_CODE);
                    }

                    if (StringUtils.isEmpty(divisionCode)) {
                        String okato5_3 = okato5 + "000";
                        responseDataItem = dataItems.stream()
                                .filter(item -> {
                                    String okato11 = item.getAttributeValue(AREA_OKATO);
                                    String localDivisionCode = item.getAttributeValue(DIVISION_CODE);
                                    return okato11.startsWith(okato5_3) && !StringUtils.isEmpty(localDivisionCode);
                                })
                                .findFirst();
                        if (responseDataItem.isPresent()) {
                            divisionCode = responseDataItem.get().getAttributeValue(DIVISION_CODE);
                        }
                    }

                    if (StringUtils.isEmpty(divisionCode)) {
                        responseDataItem = dataItems.stream()
                                .filter(item -> {
                                    String localDivisionCode = item.getAttributeValue(DIVISION_CODE);
                                    return !StringUtils.isEmpty(localDivisionCode);
                                }).findFirst();
                        if (responseDataItem.isPresent()) {
                            divisionCode = responseDataItem.get().getAttributeValue(DIVISION_CODE);
                        }
                        if (StringUtils.isEmpty(divisionCode)) {
                            throw new FormBaseException(String.format("В найденном элементе %s нет атрибута %s", responseDataItem, DIVISION_CODE));
                        }
                    }
                }
            }
            // по коду дивизиона ищем имя подразделения ПФР
            request = SopDictionaryRequestUtils.requestWithFilter(PFR_DIVISIONS, DIVISION_CODE, divisionCode);
            SopResponseDataItem pfrDepartmentItem = findItem(request, okato);
//            Map<String, String> pfrDepartmentItemAttributeValues =;
            String shortName = pfrDepartmentItem.getAttributeValue(SHORT_NAME);
            if (StringUtils.isEmpty(shortName)) {
                throw new FormBaseException(String.format("В найденном элементе нет атрибута %s", divisionCode));
            }
            putPfrInComponentAttrs(pfrDepartmentItem, component);
        }
    }

    private SopResponseDataItem findItem(SopDictionaryRequest request, String okato) {
        SopDictionaryResponse response = sopDictionaryService.findByRequest(request, okato);
        List<SopResponseDataItem> dataItems = response.getData();
        if(dataItems == null || dataItems.size() != 1) {
            throw new FormBaseException(String.format("Должно возвращаться всегда единственное подразделение по окато %s", okato));
        }
        return dataItems.get(0);
    }

    private static void putPfrInComponentAttrs(SopResponseDataItem pfrDepartment, FieldComponent component) {
        @SuppressWarnings("unchecked")
        Map<String, Object> simpleEntry = (Map<String, Object>) component.getAttrs().get("simple");
        if (Objects.isNull(simpleEntry)) {
            simpleEntry = new LinkedHashMap<>();
            component.getAttrs().put("simple", simpleEntry);
        }
        SopDataItem item = new SopDataItem();
        item.setTitle(pfrDepartment.getAttributeValue(SHORT_NAME));
        item.setValue("");
        item.setAttributeValues(pfrDepartment.getAttributeValues());
        simpleEntry.put("items", Collections.singletonList(item));
    }

    private String getOkatoFromAddress(FieldComponent component, ScenarioDto scenarioDto) {
        var addressAttr = component.getAttrs().get(ADDRESS_ATTRIBUTE_NAME);
        if (Objects.nonNull(addressAttr)) {
            @SuppressWarnings("unchecked")
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
    protected void validateAfterSubmit(Map<String,String> incorrectAnswers, Map.Entry<String, ApplicantAnswer> entry,
                                       ScenarioDto scenarioDto, FieldComponent fieldComponent) {
        DocumentContext dc = JsonPath.parse(jsonProcessingService.convertAnswersToJsonString(Map.ofEntries(entry)));
        Object value;
        try {
            value = dc.read("$..title");
            String shortName = (value instanceof JSONArray) ? ((JSONArray)value).get(0).toString() : value.toString();
            if (StringUtils.isEmpty(shortName)) {
                incorrectAnswers.put(entry.getKey(),
                        String.format("Справочник СОП %s не содержит элемент %s .", PFR_DIVISIONS.name(), entry.getValue().getValue()));
            }
        } catch (PathNotFoundException e) {
            incorrectAnswers.put(entry.getKey(), String.format("В текущем значении отсутствует %s.", "title"));
        }
    }
}
