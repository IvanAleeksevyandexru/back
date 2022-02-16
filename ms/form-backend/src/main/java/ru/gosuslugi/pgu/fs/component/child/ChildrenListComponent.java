package ru.gosuslugi.pgu.fs.component.child;

import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.JsonPath;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import ru.atc.carcass.security.rest.model.person.Kids;
import ru.atc.carcass.security.rest.model.person.PersonDoc;
import ru.gosuslugi.pgu.common.core.date.util.DateUtil;
import ru.gosuslugi.pgu.common.core.json.JsonProcessingUtil;
import ru.gosuslugi.pgu.common.esia.search.dto.UserPersonalData;
import ru.gosuslugi.pgu.components.ChildrenUtilResponse;
import ru.gosuslugi.pgu.components.FieldComponentUtil;
import ru.gosuslugi.pgu.components.descriptor.attr_factory.FieldComponentAttrsFactory;
import ru.gosuslugi.pgu.components.descriptor.placeholder.Reference;
import ru.gosuslugi.pgu.dto.ApplicantAnswer;
import ru.gosuslugi.pgu.dto.ApplicantDto;
import ru.gosuslugi.pgu.dto.ApplicantMode;
import ru.gosuslugi.pgu.dto.ApplicantRole;
import ru.gosuslugi.pgu.dto.cycled.CycledApplicantAnswer;
import ru.gosuslugi.pgu.dto.cycled.CycledApplicantAnswerItem;
import ru.gosuslugi.pgu.dto.cycled.CycledApplicantAnswers;
import ru.gosuslugi.pgu.dto.ScenarioDto;
import ru.gosuslugi.pgu.dto.descriptor.FieldComponent;
import ru.gosuslugi.pgu.dto.descriptor.ServiceDescriptor;
import ru.gosuslugi.pgu.dto.descriptor.types.ComponentType;
import ru.gosuslugi.pgu.fs.common.component.AbstractComponent;
import ru.gosuslugi.pgu.fs.common.component.ComponentResponse;
import ru.gosuslugi.pgu.fs.common.service.ListComponentItemUniquenessService;
import ru.gosuslugi.pgu.fs.common.utils.AnswerUtil;
import ru.gosuslugi.pgu.fs.component.input.RepeatableFieldsComponent;
import ru.gosuslugi.pgu.fs.service.DictionaryListPreprocessorService;
import ru.gosuslugi.pgu.fs.service.ParticipantService;

import java.time.OffsetDateTime;
import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static java.time.temporal.ChronoUnit.YEARS;
import static ru.gosuslugi.pgu.components.ComponentAttributes.*;
import static ru.gosuslugi.pgu.components.descriptor.placeholder.Reference.FIELD_ID_INDEX_IN_PATH;


@Slf4j
@Component
@RequiredArgsConstructor
public class ChildrenListComponent extends AbstractComponent<String> {

    private static final int OID_GENERATION_BASE = 1000;
    public final static int MIN_AGE = 0;
    public final static int MAX_AGE = 17;

    // Признак валидации филдов
    public final static String VALIDATE_OFF_ATTR_NAME = "validateOff";

    public static final String EXCLUDE_CHILD_COMPONENTS = "excludeChildComponents";

    private final UserPersonalData userPersonalData;
    private final RepeatableFieldsComponent repeatableFieldsComponent;
    private final ParticipantService participantService;
    private final ListComponentItemUniquenessService listComponentItemUniquenessService;
    private final DictionaryListPreprocessorService dictionaryListPreprocessorService;

    @Override
    public ComponentResponse<String> getInitialValue(FieldComponent component, ScenarioDto scenarioDto, ServiceDescriptor serviceDescriptor) {
        ChildrenUtilResponse response = getInitialValue(component, serviceDescriptor, scenarioDto);
        fillCyclicChildData(scenarioDto, response.getChildFullDataMap(), component.getId());
        Optional.ofNullable(component.getAttrs().get("cleanParticipantsById")).ifPresent(componentId -> {
            if (StringUtils.hasText(componentId.toString()))
                participantService.deleteRedundantParticipantsByComponentId(scenarioDto, serviceDescriptor, componentId.toString());
        });
        listComponentItemUniquenessService.updateDisclaimerForUniquenessErrors(component, scenarioDto.getUniquenessErrors());
        return ComponentResponse.of(response.getPresetValue());
    }

    @Override
    protected void validateAfterSubmit(Map<String, String> incorrectAnswers, Map.Entry<String, ApplicantAnswer> entry, ScenarioDto scenarioDto, FieldComponent fieldComponent) {
        if (!fieldComponent.getBooleanAttr(VALIDATE_OFF_ATTR_NAME)) {
            incorrectAnswers.putAll(repeatableFieldsComponent.validate(entry, scenarioDto, fieldComponent));
        }
    }

    @Override
    protected void postProcess(Map.Entry<String, ApplicantAnswer> entry, ScenarioDto scenarioDto, FieldComponent fieldComponent) {
        List<String> fieldComponentIdsForCurrentValueEntry = scenarioDto.getDisplay().getComponents().stream()
                .map(FieldComponent::getId)
                .filter(id -> id.equals(entry.getKey()))
                .collect(Collectors.toList());
        //this method
        if (!fieldComponentIdsForCurrentValueEntry.isEmpty() && fieldComponent.getAttrs().containsKey(SEND_AS_ANOTHER_REQUEST_ATTR)) {
            int oidBase = (Integer) fieldComponent.getAttrs().get(SEND_AS_ANOTHER_REQUEST_ATTR);
            if (oidBase > 0) {
                oidBase *= -1;
            }

            List<Map<Object, Object>> value = AnswerUtil.toMapList(entry, true);
            int itemsSize = value.size();
            //remove all previous possible values in case of editing
            int finalOidBase = oidBase;
            Map<String, ApplicantDto> participants = scenarioDto.getParticipants().entrySet().stream()
                    .filter(x -> Integer.parseInt(x.getKey()) / OID_GENERATION_BASE != finalOidBase)
                    .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
            //adding users as  all previous possible values in case of editing
            for (int i=0; i < itemsSize; i++) {
                ApplicantDto applicantDto = new ApplicantDto();
                applicantDto.setRole(ApplicantRole.Coapplicant);
                applicantDto.setMode(ApplicantMode.SlaveApplicant);
                applicantDto.setComponent(fieldComponent.getId());
                applicantDto.setIndex(i);
                participants.put(Integer.toString((oidBase * OID_GENERATION_BASE) - i), applicantDto);
            }
            scenarioDto.setParticipants(participants);
        }
    }

    @Override
    public ComponentType getType() {
        return ComponentType.ChildrenList;
    }

    @Override
    public List<List<Map<String, String>>> validateItemsUniqueness(Map.Entry<String, ApplicantAnswer> entry, ScenarioDto scenarioDto, FieldComponent fieldComponent) {
        return repeatableFieldsComponent.validateItemsUniqueness(entry, scenarioDto, fieldComponent);
    }

    private void fillCyclicChildData(ScenarioDto scenarioDto, Map<String, Map<String, Object>> childData, String componentId) {
        CycledApplicantAnswers cycledApplicantAnswers = scenarioDto.getCycledApplicantAnswers();
        cycledApplicantAnswers.addAnswerIfAbsent(componentId, new CycledApplicantAnswer(componentId));
        cycledApplicantAnswers.getAnswer(componentId).ifPresent(answer -> {
            childData.forEach((k, v) -> {
                answer.addItemIfAbsent(k, new CycledApplicantAnswerItem(k));
                answer.getItem(k).ifPresent(item -> item.setEsiaData(v));
            });
        });
    }

    private ChildrenUtilResponse getInitialValue(FieldComponent fieldComponent, ServiceDescriptor serviceDescriptor, ScenarioDto scenarioDto) {
        if(fieldComponent.getAttrs().containsKey(CHILDREN_LIST_REF_ATTR)) {
            return getInitialValueFromRef(fieldComponent, serviceDescriptor, scenarioDto);
        }

        List<FieldComponent> componentsToShow = FieldComponentUtil.getFieldComponentsFromAttrs(fieldComponent, serviceDescriptor);
        if (!componentsToShow.isEmpty()) {
            fieldComponent.getAttrs().put(FieldComponentUtil.COMPONENTS_KEY, componentsToShow);
        }

        int minAge = (int) fieldComponent.getAttrs().getOrDefault(MIN_AGE_ATTR, MIN_AGE);
        int maxAge = (int) fieldComponent.getAttrs().getOrDefault(MAX_AGE_ATTR, MAX_AGE);
        Optional<OffsetDateTime> minBirthDate = Optional.ofNullable((String) fieldComponent.getAttrs().get(BORN_AFTER_DATE_ATTR))
                .filter(StringUtils::hasText)
                .map(date -> DateUtil.toOffsetDateTime(date, DateUtil.ESIA_DATE_FORMAT));
        Predicate<OffsetDateTime> birthDateCheck = birthDate -> {
            int age = (int) YEARS.between(birthDate, OffsetDateTime.now());
            return minBirthDate.map(date -> birthDate.isAfter(date) || birthDate.isEqual(date)).orElse(true)
                    && age >= minAge
                    && age <= maxAge;
        };
        ChildrenUtilResponse response = new ChildrenUtilResponse();
        ArrayList<HashMap<String, Object>> result= new ArrayList<>();
        userPersonalData.fillKidsOms();
        // exclude previous selected children
        List<Kids> filteredKids = excludeFromKids(fieldComponent, scenarioDto, userPersonalData.getKids());
        for (Kids kid:  filteredKids) {
            OffsetDateTime birthDate = DateUtil.toOffsetDateTime(kid.getBirthDate(), DateUtil.ESIA_DATE_FORMAT);
            if (!birthDateCheck.test(birthDate)) {
                continue;
            }

            HashMap<String, Object> kidInfo = new HashMap<>();
            kidInfo.put(CHILDREN_ID_ATTR, kid.getId());
            kidInfo.put(CHILDREN_IS_NEW_ATTR, false);
            kidInfo.put(CHILDREN_FIRST_NAME_ATTR, kid.getFirstName());
            kidInfo.put(CHILDREN_MIDDLE_NAME_ATTR, Optional.ofNullable(kid.getMiddleName()).orElse(""));
            kidInfo.put(CHILDREN_LAST_NAME_ATTR, kid.getLastName());
            kidInfo.put(CHILDREN_BIRTH_DATE_ATTR, DateUtil.toOffsetDateTimeString(kid.getBirthDate(), DateUtil.ESIA_DATE_FORMAT));
            kidInfo.put(CHILDREN_GENDER_ATTR, kid.getGender());
            kidInfo.put(SNILS, kid.getSnils());
            Optional<PersonDoc> brthCertOptional = kid.getDocuments().getDocs().stream().filter(x -> x.getType().contains("BRTH_CERT")).findFirst();
            if (birthDateCheck.test(birthDate) && brthCertOptional.isPresent()) {
                PersonDoc birthCert = brthCertOptional.get();
                kidInfo.put(CHILDREN_RF_BIRTH_CERTIFICATE_SERIES_ATTR, birthCert.getSeries());
                kidInfo.put(CHILDREN_RF_BIRTH_CERTIFICATE_NUMBER_ATTR, birthCert.getNumber());
                kidInfo.put(CHILDREN_RF_BIRTH_CERTIFICATE_ACT_NUMBER_ATTR, birthCert.getActNo() == null ? "-" : birthCert.getActNo());
                kidInfo.put(CHILDREN_RF_BIRTH_CERTIFICATE_ISSUE_DATE_ATTR, DateUtil.toOffsetDateTime(birthCert.getIssueDate(), DateUtil.ESIA_DATE_FORMAT));
                kidInfo.put(CHILDREN_RF_BIRTH_CERTIFICATE_ISSUED_BY_ATTR, birthCert.getIssuedBy());
                kidInfo.put(CHILDREN_ACT_DATE_ATTR, birthCert.getActDate());
                kidInfo.put(CHILDREN_BIRTH_CERTIFICATE_TYPE, birthCert.getType());
            }
            Optional<PersonDoc> rfPassportOptional = kid.getDocuments().getDocs().stream().filter(x -> x.getType().equals(RF_PASSPORT_ATTR)).findFirst();
            if (birthDateCheck.test(birthDate) && rfPassportOptional.isPresent()) {
                PersonDoc rfPassport = rfPassportOptional.get();
                kidInfo.put(RF_PASSPORT_SERIES_ATTR, rfPassport.getSeries());
                kidInfo.put(RF_PASSPORT_NUMBER_ATTR, rfPassport.getNumber());
                kidInfo.put(CHILDREN_RF_BIRTH_CERTIFICATE_ACT_NUMBER_ATTR, rfPassport.getActNo() == null ? "-" : rfPassport.getActNo());
                kidInfo.put(RF_PASSPORT_ISSUE_DATE_ATTR, rfPassport.getIssueDate());
                kidInfo.put(RF_PASSPORT_ISSUED_BY_ID_ATTR, rfPassport.getIssueId());
                kidInfo.put(CHILDREN_ACT_DATE_ATTR, rfPassport.getActDate());
            }

            Optional<PersonDoc> omsOptional = kid.getDocuments().getDocs().stream().filter(x -> x.getType().equals(MDCL_PLCY_ATTR)).findFirst();
            if (birthDateCheck.test(birthDate) && omsOptional.isPresent()) {
                PersonDoc oms = omsOptional.get();
                kidInfo.put(CHILDREN_OMS_SERIES_ATTR, oms.getSeries());
                kidInfo.put(CHILDREN_OMS_NUMBER_ATTR, oms.getNumber());
            }

            kidInfo.put(CHILDREN_RELATION_ATTR, "");
            kidInfo.put(REGISTRATION_ATTR, "");
            kidInfo.put(REGISTRATION_DATE_ATTR, "");
            result.add(kidInfo);
            response.addChildData(kid.getId(), kidInfo);
        }

        List<FieldComponent> fieldComponents = (List<FieldComponent>) fieldComponent.getAttrs().get(FieldComponentUtil.COMPONENTS_KEY);
        for (FieldComponent component : fieldComponents) {
            linkedValuesService.fillLinkedValues(component, scenarioDto);
            if (ComponentType.DropDown == component.getType()) {
                dictionaryListPreprocessorService.prepareDictionaryListFromComponent(component, scenarioDto);
            }
        }
        List<Map<String, Object>> childrenList = new ArrayList<>();
        for (Map<String, Object> r : result) {
            Map<String, Object> map = new HashMap<>();
            for (FieldComponent component : fieldComponents) {
                List<String> fieldNames = FieldComponentUtil.getFieldNames(component);
                if (fieldNames.size() == 1) {
                    map.put(component.getId(), r.get(fieldNames.get(0)));
                }
                if (fieldNames.size() > 1) {
                    Map<String, Object> fields = new HashMap<>();
                    for(String fieldName : fieldNames) {
                        fields.put(fieldName, r.get(fieldName));
                    }
                    if (!fields.values().stream().allMatch(Objects::isNull)) {
                        map.put(component.getId(), fields);
                    }

                }
            }

            childrenList.add(map);
        }
        response.setPresetValue(JsonProcessingUtil.toJson(childrenList));
        return response;
    }

    private ChildrenUtilResponse getInitialValueFromRef(FieldComponent fieldComponent, ServiceDescriptor serviceDescriptor, ScenarioDto scenarioDto) {
        ChildrenUtilResponse response = new ChildrenUtilResponse();
        FieldComponentAttrsFactory componentAttrsFactory = new FieldComponentAttrsFactory(fieldComponent);
        Object listRef = fieldComponent.getAttrs().remove(CHILDREN_LIST_REF_ATTR);
        Reference reference = componentAttrsFactory.getReference("", listRef, Collections.emptyMap());
        reference.setKey(reference.getPath().split("\\.")[FIELD_ID_INDEX_IN_PATH]);
        Optional<FieldComponent> refComponentOptional = serviceDescriptor.getFieldComponentById(reference.getKey());
        if (refComponentOptional.isPresent()) {
            FieldComponent refComponent = refComponentOptional.get();
            List<FieldComponent> componentsToShow = FieldComponentUtil.getFieldComponentsFromAttrs(refComponent, serviceDescriptor);
            if (!componentsToShow.isEmpty()) {
                fieldComponent.getAttrs().put(FieldComponentUtil.COMPONENTS_KEY, componentsToShow);
            }
            DocumentContext currentValueContext = JsonPath.parse(jsonProcessingService.convertAnswersToJsonString(scenarioDto.getCurrentValue()));
            DocumentContext applicantAnswersContext = JsonPath.parse(jsonProcessingService.convertAnswersToJsonString(scenarioDto.getApplicantAnswers()));
            String childrenString = reference.getNext(currentValueContext, applicantAnswersContext);
            Optional<FieldComponent> idComponentOptional = componentsToShow.stream()
                    .filter(f -> FieldComponentUtil.getFieldNames(f).size() == 1)
                    .filter(f -> FieldComponentUtil.getFieldNames(f).contains(CHILDREN_IS_NEW_ATTR))
                    .findAny();
            if (idComponentOptional.isPresent()) {
                List<Map<Object, Object>> childrenList = AnswerUtil.toMapList(AnswerUtil.createAnswerEntry("", childrenString), true).stream()
                        .peek(map -> map.remove(idComponentOptional.get().getId()))
                        .collect(Collectors.toList());
                childrenString = jsonProcessingService.toJson(childrenList);
            }
            response.setPresetValue(childrenString);
        }
        return response;
    }

    /**
     * Исключает из списка Kids детей, ранее выбранных в предыдущих компонентах ChildrenList
     *
     * @param fieldComponent экземпляр компонента
     * @param scenarioDto данные сценария
     * @param kids список детей, полученный из UserPersonalData
     */
    private List<Kids> excludeFromKids(FieldComponent fieldComponent, ScenarioDto scenarioDto, List<Kids> kids) {
        List<Map<String, Object>> esiaDataList = getExcludedDataEsiaList(fieldComponent, scenarioDto);
        log.debug("Excluded esiaDataList: "+esiaDataList);
        if (!esiaDataList.isEmpty()) {
            return kids.stream().filter(item -> !ChildComparator.contains(esiaDataList, item))
                    .collect(Collectors.toList());
        }
        else {
            return kids;
        }
    }

    /**
     * Определяет компоненты, включенные в атрибут excludeChildComponents,
     * для каждой компоненты выбирается ответ, зафиксированный на предыдущем проходе ChildrenList.
     * Возвращается список из полученных esiaData или пустой список, если таких компонент нет.
     *
     * @param fieldComponent экземпляр компонента
     * @param scenarioDto данные сценария
     */
    private List<Map<String, Object>> getExcludedDataEsiaList(FieldComponent fieldComponent, ScenarioDto scenarioDto) {
        Optional<Object> optionalExcludes = Optional.ofNullable(fieldComponent.getAttrs().get(EXCLUDE_CHILD_COMPONENTS));
        if (optionalExcludes.isPresent()) {
            Object excludes = optionalExcludes.get();
            if (excludes instanceof List) {
                List<String> componentIds = (List<String>)excludes;
                CycledApplicantAnswers currentAnswers = scenarioDto.getCycledApplicantAnswers();
                return currentAnswers.getAnswers().stream()
                        .filter(answer -> componentIds.contains(answer.getId()))
                        .flatMap(answer -> answer.getItems().stream())
                        .map(CycledApplicantAnswerItem::getEsiaData)
                        .collect(Collectors.toList());
            }
        }
        return List.of();
    }

}
