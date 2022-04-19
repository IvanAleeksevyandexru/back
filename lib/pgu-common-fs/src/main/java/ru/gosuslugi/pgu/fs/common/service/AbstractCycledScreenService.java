package ru.gosuslugi.pgu.fs.common.service;

import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.JsonPath;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.logging.log4j.util.Strings;
import org.springframework.util.StringUtils;
import ru.gosuslugi.pgu.common.core.exception.PguException;
import ru.gosuslugi.pgu.components.BasicComponentUtil;
import ru.gosuslugi.pgu.components.FieldComponentUtil;
import ru.gosuslugi.pgu.dto.ApplicantAnswer;
import ru.gosuslugi.pgu.dto.DisplayRequest;
import ru.gosuslugi.pgu.dto.InitServiceDto;
import ru.gosuslugi.pgu.dto.ScenarioDto;
import ru.gosuslugi.pgu.dto.ScenarioRequest;
import ru.gosuslugi.pgu.dto.ScenarioResponse;
import ru.gosuslugi.pgu.dto.cycled.CycledApplicantAnswer;
import ru.gosuslugi.pgu.dto.cycled.CycledApplicantAnswerItem;
import ru.gosuslugi.pgu.dto.cycled.CycledApplicantAnswers;
import ru.gosuslugi.pgu.dto.descriptor.FieldComponent;
import ru.gosuslugi.pgu.dto.descriptor.ScreenDescriptor;
import ru.gosuslugi.pgu.dto.descriptor.ScreenRule;
import ru.gosuslugi.pgu.dto.descriptor.ServiceDescriptor;
import ru.gosuslugi.pgu.dto.descriptor.types.ComponentType;
import ru.gosuslugi.pgu.dto.descriptor.types.ScreenType;
import ru.gosuslugi.pgu.fs.common.component.AbstractCycledComponent;
import ru.gosuslugi.pgu.fs.common.component.BaseComponent;
import ru.gosuslugi.pgu.fs.common.component.ComponentRegistry;
import ru.gosuslugi.pgu.fs.common.component.ComponentResponse;
import ru.gosuslugi.pgu.fs.common.descriptor.DescriptorService;
import ru.gosuslugi.pgu.fs.common.exception.ErrorModalException;
import ru.gosuslugi.pgu.fs.common.exception.NoScreensFoundException;
import ru.gosuslugi.pgu.fs.common.helper.HelperScreenRegistry;
import ru.gosuslugi.pgu.fs.common.helper.ScreenHelper;
import ru.gosuslugi.pgu.fs.common.utils.AnswerUtil;

import java.util.AbstractMap;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import static ru.gosuslugi.pgu.dto.ScenarioResponse.EMPTY_SCENARIO;

@RequiredArgsConstructor
@Slf4j
public abstract class AbstractCycledScreenService extends AbstractScreenService implements CycledScreenService {

    private final ComponentService componentService;
    private final JsonProcessingService jsonProcessingService;
    private final ComponentRegistry componentRegistry;
    private final HelperScreenRegistry screenRegistry;
    private final RuleConditionService ruleConditionService;
    private final ListComponentItemUniquenessService listComponentItemUniquenessService;


    @Override
    public ScenarioResponse getNextScreen(ScenarioRequest request, String serviceId) {
        ScenarioDto scenarioDto = request.getScenarioDto();
        ServiceDescriptor serviceDescriptor = getDescriptorService(request).getServiceDescriptor(serviceId);
        Optional<FieldComponent> fieldBox = scenarioDto.getCurrentValue().entrySet().stream()
                .map(answerEntry -> componentService.getFieldComponent(serviceId, answerEntry, serviceDescriptor))
                .filter(Optional::isPresent)
                .filter(component -> component.get().getAttrs() != null)
                .filter(component -> (Boolean) component.get().getAttrs().getOrDefault(FieldComponentUtil.IS_CYCLED_KEY, Boolean.FALSE))
                .findAny()
                .map(Optional::get);

        if (fieldBox.isPresent()) {
            return getInitScenarioResponse(request, serviceDescriptor, fieldBox.get());
        }

        if (serviceDescriptor.getScreenRules() == null || serviceDescriptor.getScreenRules().containsKey(scenarioDto.getDisplay().getId())) {
            return EMPTY_SCENARIO;
        }
        if (serviceDescriptor.getCycledScreenRules() == null || !serviceDescriptor.getCycledScreenRules().containsKey(scenarioDto.getDisplay().getId()) ) {
            throw new NoScreensFoundException(String.format("В описании услуги %s не найден экран с идентификатором %s", serviceDescriptor.getService(), scenarioDto.getDisplay().getId()));
        }

        fillCycledApplicantAnswer(scenarioDto, serviceDescriptor);
        if (request.getScenarioDto().getUniquenessErrors().stream().anyMatch(list -> !list.isEmpty())) {
            return getValidationErrorScenarioResponse(request);
        }
        if (serviceDescriptor.getCycledScreenRules().containsKey(scenarioDto.getDisplay().getId())) {
            ScenarioResponse nextScenarioResponse = getNextScenarioResponse(serviceDescriptor, request);
            if (nextScenarioResponse != null) {
                Optional<ScreenDescriptor> screenDescriptor = serviceDescriptor.getScreenDescriptorById(scenarioDto.getDisplay().getId());
                screenDescriptor.ifPresent(screen -> {
                    if (Objects.nonNull(screen.getLogicComponentIds()) && !screen.getLogicComponentIds().isEmpty())
                        nextScenarioResponse.getScenarioDto().setLogicComponents(
                                componentService.getLogicFields(screen, scenarioDto, serviceDescriptor));
                });
                return nextScenarioResponse;
            }
        }

        return getTerminalScenarioResponse(request, serviceDescriptor);
    }

    @Override
    public ScenarioResponse getPrevScreen(ScenarioRequest request, String serviceId) {
        ScenarioDto scenarioDto = request.getScenarioDto();
        Map.Entry<String, ApplicantAnswer> answerEntry = createCycledApplicantAnswerEntry(scenarioDto);
        scenarioDto.getApplicantAnswers().put(answerEntry.getKey(), answerEntry.getValue());
        CycledApplicantAnswers cycledApplicantAnswers = scenarioDto.getCycledApplicantAnswers();
        CycledApplicantAnswer currentAnswer = cycledApplicantAnswers.getCurrentAnswer();

        ServiceDescriptor serviceDescriptor = getDescriptorService(request).getServiceDescriptor(serviceId);
        ScreenDescriptor prevScreenDescriptor = null;

        do {
            final CycledApplicantAnswerItem currentAnswerItem = currentAnswer.getCurrentAnswerItem();
            final LinkedList<String> finishedScreens = currentAnswer.getCurrentAnswerItem().getFinishedScreens();

            final var prevScreenId = finishedScreens.pollLast();
            if (prevScreenId != null) {
                prevScreenDescriptor = serviceDescriptor.getScreenDescriptorById(prevScreenId).orElse(null);
                if (prevScreenDescriptor != null && prevScreenDescriptor.getType() == ScreenType.EMPTY) {
                    prevScreenDescriptor = null;
                    continue;
                }
                break;
            }

            //если экран первый в цикле - не очищать cachedAnswers при шаге назад на выходе из цикла
            final var keys = new LinkedList<>(currentAnswer.getItemsIds());
            final var currentItemIdIndex = keys.lastIndexOf(currentAnswer.getCurrentItemId());
            if (currentItemIdIndex == 0) {
                break;
            }

            scenarioDto.getCachedAnswers().keySet().removeAll(currentAnswerItem.getCachedAnswers().keySet());
            scenarioDto.getApplicantAnswers().keySet().removeAll(currentAnswerItem.getItemAnswers().keySet());

            currentAnswer.setCurrentItemId(keys.get(currentItemIdIndex - 1));
            scenarioDto.getApplicantAnswers().putAll(currentAnswer.getCurrentAnswerItem().getItemAnswers());

        } while (true);

        if (prevScreenDescriptor != null) {
            return getPrevScreenScenarioResponse(request, serviceDescriptor, prevScreenDescriptor);
        }

        return EMPTY_SCENARIO;
    }

    @Override
    protected ScenarioResponse afterPrevScreen(ScenarioResponse scenarioResponse, String serviceId) {
        return scenarioResponse;
    }

    @Override
    protected DescriptorService getDescriptorService() {
        return null;
    }

    @Override
    public ScenarioResponse getInitScreen(String serviceId, InitServiceDto initServiceDto) {
        return null;
    }

    private ScenarioResponse getTerminalScenarioResponse(ScenarioRequest request, ServiceDescriptor serviceDescriptor) {
        ScenarioDto scenarioDto = request.getScenarioDto();
        CycledApplicantAnswer cycledApplicantAnswer = scenarioDto.getCycledApplicantAnswers().getCurrentAnswer();
        CycledApplicantAnswerItem currentAnswerItem = cycledApplicantAnswer.getCurrentAnswerItem();
        List<String> keys = cycledApplicantAnswer.getItemsIds();

        if (keys.lastIndexOf(cycledApplicantAnswer.getCurrentItemId()) == keys.size() - 1) {
            Map.Entry<String, ApplicantAnswer> answerEntry = createCycledApplicantAnswerEntry(scenarioDto);
            scenarioDto.getApplicantAnswers().put(answerEntry.getKey(), answerEntry.getValue());
            currentAnswerItem.getCachedAnswers().keySet().forEach(scenarioDto.getCachedAnswers()::remove);
            currentAnswerItem.getItemAnswers().keySet().forEach(scenarioDto.getApplicantAnswers()::remove);
            scenarioDto.getDisplay().setId(cycledApplicantAnswer.getInitScreen());
        } else {
            cycledApplicantAnswer.getCurrentAnswerItem().getCachedAnswers().keySet().forEach(scenarioDto.getCachedAnswers()::remove);
            cycledApplicantAnswer.getCurrentAnswerItem().getItemAnswers().keySet().forEach(scenarioDto.getApplicantAnswers()::remove);
            int nextIndex = keys.lastIndexOf(cycledApplicantAnswer.getCurrentItemId()) + 1;
            String nextKey = keys.get(nextIndex);
            cycledApplicantAnswer.setCurrentItemId(nextKey);
            cycledApplicantAnswer.getCurrentAnswerItem().getCachedAnswers().forEach(scenarioDto.getCachedAnswers()::put);
            cycledApplicantAnswer.getCurrentAnswerItem().getItemAnswers().forEach(scenarioDto.getApplicantAnswers()::put);
            scenarioDto.getDisplay().setId(cycledApplicantAnswer.getInitScreen());
            return getNextScenarioResponse(serviceDescriptor, request);
        }
        return EMPTY_SCENARIO;
    }

    private Map.Entry<String, ApplicantAnswer> createCycledApplicantAnswerEntry(ScenarioDto scenarioDto) {
        CycledApplicantAnswer cycledApplicantAnswer = scenarioDto.getCycledApplicantAnswers().getCurrentAnswer();
        List<Map<String, Object>> itemStringList = cycledApplicantAnswer.getItems().stream()
                .map(item -> item.getItemAnswers().entrySet().stream()
                        .map(entry -> {
                            Object value = AnswerUtil.tryParseToMap(entry.getValue().getValue());
                            if (value instanceof String) {
                                value = AnswerUtil.tryParseToList((String) value);
                            }
                            return new AbstractMap.SimpleEntry<>(entry.getKey(), value);
                        })
                        .collect(Collectors.toMap(AbstractMap.SimpleEntry::getKey,
                                v-> Objects.nonNull(v.getValue()) ? v.getValue() : Strings.EMPTY)))
                .collect(Collectors.toList());
        return AnswerUtil.createAnswerEntry(scenarioDto.getCycledApplicantAnswers().getCurrentAnswerId(), jsonProcessingService.toJson(itemStringList));
    }

    private ScenarioResponse getInitScenarioResponse(ScenarioRequest request, ServiceDescriptor serviceDescriptor, FieldComponent fieldComponent) {
        initFillCycledApplicantAnswer(request, fieldComponent, serviceDescriptor);
        if (request.getScenarioDto().getUniquenessErrors().stream().anyMatch(list -> !list.isEmpty())) {
            return getValidationErrorScenarioResponse(request);
        }
        return getNextScenarioResponse(serviceDescriptor, request);
    }

    private ScenarioResponse getPrevScreenScenarioResponse(ScenarioRequest request, ServiceDescriptor serviceDescriptor, ScreenDescriptor prevScreenDescriptor) {
        ScenarioDto scenarioDto = request.getScenarioDto();
        CycledApplicantAnswer currentAnswer = scenarioDto.getCycledApplicantAnswers().getCurrentAnswer();
        CycledApplicantAnswerItem currentAnswerItem = currentAnswer.getCurrentAnswerItem();
        Map<String, ApplicantAnswer> answersToRemove = currentAnswerItem.getItemAnswers()
                .entrySet()
                .stream()
                .filter(e -> prevScreenDescriptor.getComponentIds().contains(e.getKey()))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
        scenarioDto.getCachedAnswers().putAll(answersToRemove);
        currentAnswerItem.getCachedAnswers().putAll(answersToRemove);
        answersToRemove.keySet().forEach(key -> currentAnswerItem.getItemAnswers().remove(key));
        removeAnswersFromEsiaData(serviceDescriptor, currentAnswerItem, answersToRemove);
        FieldComponent cycledComponent = serviceDescriptor.getFieldComponentById(currentAnswer.getId())
                .orElseThrow(() -> new PguException(String.format("Компонент %s не найден", currentAnswer.getId())));
        listComponentItemUniquenessService.removeUniquenessKeys(cycledComponent, currentAnswerItem);
        answersToRemove.keySet().forEach(key -> scenarioDto.getApplicantAnswers().remove(key));
        Map.Entry<String, ApplicantAnswer> answerEntry = createCycledApplicantAnswerEntry(scenarioDto);
        scenarioDto.getApplicantAnswers().put(answerEntry.getKey(), answerEntry.getValue());
        return getCommonScenarioResponse(prevScreenDescriptor, serviceDescriptor, request);
    }

    private void removeAnswersFromEsiaData(ServiceDescriptor serviceDescriptor,
                                           CycledApplicantAnswerItem currentAnswerItem,
                                           Map<String, ApplicantAnswer> answersToRemove) {
        List<FieldComponent> answersToRemoveComponents = answersToRemove.keySet().stream()
                .map(id -> serviceDescriptor.getFieldComponentById(id)
                        .orElseThrow(() -> new PguException(String.format("Компонент %s не найден", id))))
                .collect(Collectors.toList());
        for (FieldComponent fieldComponent : answersToRemoveComponents) {
            BaseComponent<?> component = componentRegistry.getComponent(fieldComponent.getType());
            if (component != null && component.isCycled()) {
                ((AbstractCycledComponent<?>) component).removeFromCycledItemEsiaData(fieldComponent, currentAnswerItem);
            }
        }

    }

    private ScenarioResponse getValidationErrorScenarioResponse(ScenarioRequest request) {
        ScenarioDto scenarioDto = request.getScenarioDto();
        scenarioDto.addToCachedAnswers(scenarioDto.getCurrentValue());
        ScenarioResponse scenarioResponse = new ScenarioResponse();
        scenarioResponse.setScenarioDto(request.getScenarioDto());
        return scenarioResponse;
    }

    private ScenarioResponse getNextScenarioResponse(ServiceDescriptor serviceDescriptor, ScenarioRequest request) {
        ScenarioDto scenarioDto = request.getScenarioDto();
        CycledApplicantAnswerItem currentAnswerItem = scenarioDto.getCycledApplicantAnswers().getCurrentAnswer().getCurrentAnswerItem();
        currentAnswerItem.getItemAnswers().keySet().forEach(currentKey-> scenarioDto.getCachedAnswers().remove(currentKey));
        scenarioDto.getApplicantAnswers().putAll(currentAnswerItem.getItemAnswers());
        Map.Entry<String, ApplicantAnswer> answerEntry = createCycledApplicantAnswerEntry(scenarioDto);
        scenarioDto.getCachedAnswers().remove(answerEntry.getKey());
        scenarioDto.getApplicantAnswers().put(answerEntry.getKey(), answerEntry.getValue());
        Optional<ScreenDescriptor> screenDescriptorOptional = getNextScreenDescriptor(request, serviceDescriptor);
        if (screenDescriptorOptional.isEmpty()) {

            // проверить, все ли cycled answers обошли
            CycledApplicantAnswer cycledApplicantAnswer = scenarioDto.getCycledApplicantAnswers().getCurrentAnswer();
            List<String> keys = cycledApplicantAnswer.getItemsIds();
            if (keys.lastIndexOf(cycledApplicantAnswer.getCurrentItemId()) < keys.size() - 1) {
                cycledApplicantAnswer.getCurrentAnswerItem().getCachedAnswers().keySet().forEach(scenarioDto.getCachedAnswers()::remove);
                cycledApplicantAnswer.getCurrentAnswerItem().getItemAnswers().keySet().forEach(scenarioDto.getApplicantAnswers()::remove);
                int nextIndex = keys.lastIndexOf(cycledApplicantAnswer.getCurrentItemId()) + 1;
                String nextKey = keys.get(nextIndex);
                cycledApplicantAnswer.setCurrentItemId(nextKey);
                cycledApplicantAnswer.getCurrentAnswerItem().getCachedAnswers().forEach(scenarioDto.getCachedAnswers()::put);
                cycledApplicantAnswer.getCurrentAnswerItem().getItemAnswers().forEach(scenarioDto.getApplicantAnswers()::put);
                scenarioDto.getDisplay().setId(cycledApplicantAnswer.getInitScreen());
                return getNextScenarioResponse(serviceDescriptor, request);
            }

            return EMPTY_SCENARIO;
        }
        return getCommonScenarioResponse(screenDescriptorOptional.get(), serviceDescriptor, request);
    }

    private ScenarioResponse getCommonScenarioResponse(ScreenDescriptor screenDescriptor, ServiceDescriptor serviceDescriptor, ScenarioRequest request) {
        ScenarioDto scenarioDto = request.getScenarioDto();
        ScenarioResponse scenarioResponse = new ScenarioResponse();
        ScreenHelper screenHelper = screenRegistry.getHelper(screenDescriptor.getType());
        if (Objects.nonNull(screenHelper)) {
            screenDescriptor = screenHelper.processScreen(screenDescriptor, scenarioDto);
        }
        DisplayRequest displayRequest = new DisplayRequest(screenDescriptor, componentService.getScreenFields(screenDescriptor, scenarioDto, serviceDescriptor));
        fillCycledDisplayComponents(displayRequest, scenarioDto, serviceDescriptor);
        displayReferenceService.processDisplayRefs(displayRequest, scenarioDto);
        scenarioDto.setDisplay(displayRequest);
        scenarioDto.setCurrentValue(new HashMap<>());
        scenarioResponse.setScenarioDto(scenarioDto);
        return scenarioResponse;
    }

    private Optional<ScreenDescriptor> getNextScreenDescriptor(ScenarioRequest request, ServiceDescriptor serviceDescriptor) {
        ScenarioDto scenarioDto = request.getScenarioDto();
        DocumentContext currentValueContext = JsonPath.parse(jsonProcessingService.convertAnswersToJsonString(scenarioDto.getCurrentValue()));

        List<ScreenRule> screenRuleList = serviceDescriptor.getCycledScreenRules().get(scenarioDto.getDisplay().getId());

        CycledApplicantAnswer cycledApplicantAnswer = scenarioDto.getCycledApplicantAnswers().getCurrentAnswer();
        Map<String, ApplicantAnswer> applicantAnswersForCycledItem = cycledApplicantAnswer.getCurrentAnswerItem().getItemAnswers();
        Map<String, ApplicantAnswer> applicantAnswersForContext = new HashMap<>();
        applicantAnswersForContext.putAll(applicantAnswersForCycledItem);
        applicantAnswersForContext.putAll(scenarioDto.getApplicantAnswers());
        DocumentContext applicantAnswersContext = JsonPath.parse(jsonProcessingService.convertAnswersToJsonString(applicantAnswersForContext));
        DocumentContext serviceInfoContext = JsonPath.parse(jsonProcessingService.toJson(scenarioDto.getServiceInfo()));
        DocumentContext cycledCurrentItemContext = JsonPath.parse(jsonProcessingService.toJson(scenarioDto.getCycledApplicantAnswerContext()));
        List<ScreenRule> validScreenRules = screenRuleList
                .stream()
                .filter(screenRule -> ruleConditionService.isRuleApplyToAnswers(
                        screenRule.getConditions(),
                        Arrays.asList(scenarioDto.getCurrentValue(), applicantAnswersForContext),
                        Arrays.asList(currentValueContext, applicantAnswersContext, serviceInfoContext, cycledCurrentItemContext),
                        scenarioDto))
                .collect(Collectors.toList());

        if (validScreenRules.size() > 1) {
            log.warn("На экране " + scenarioDto.getDisplay().getId() + " найдено более 1 экрана для перехода " + validScreenRules.stream().map(ScreenRule::getNextDisplay).collect(Collectors.joining(",")));
            throw new RuntimeException("Найдено больше чем один экран для перехода");
        }
        if (validScreenRules.size() == 0) {
            log.warn("На экране " + scenarioDto.getDisplay().getId() + " не найдено экранов для перехода ");
            throw new RuntimeException("Не найдено экрана для перехода");
        }
        String nextScreenId = validScreenRules.get(0).getNextDisplay();
        if (nextScreenId == null) {
            return Optional.empty();
        }
        Optional<ScreenDescriptor> foundScreen = serviceDescriptor.getScreenDescriptorById(nextScreenId);

        foundScreen.ifPresent(screenDescriptor -> log.warn("Отправляю пользователю экран =" + screenDescriptor.getId()));
        return foundScreen;
    }

    private void initFillCycledApplicantAnswer(ScenarioRequest request, FieldComponent fieldComponent, ServiceDescriptor serviceDescriptor) {
        ScenarioDto scenarioDto = request.getScenarioDto();
        CycledApplicantAnswers cycledApplicantAnswers = scenarioDto.getCycledApplicantAnswers();
        cycledApplicantAnswers.setCurrentAnswerId(fieldComponent.getId());
        cycledApplicantAnswers.addAnswerIfAbsent(fieldComponent.getId(), new CycledApplicantAnswer(fieldComponent.getId()));
        CycledApplicantAnswer cycledApplicantAnswer = cycledApplicantAnswers.getCurrentAnswer();
        cycledApplicantAnswer.setInitScreen(scenarioDto.getDisplay().getId());
        Optional<FieldComponent> idComponentOptional = FieldComponentUtil.getFieldComponentsFromAttrs(fieldComponent, serviceDescriptor).stream()
                .filter(f -> FieldComponentUtil.getFieldNames(f).size() == 1)
                .filter(f -> FieldComponentUtil.getFieldNames(f).contains("id"))
                .findAny();
        if (idComponentOptional.isPresent()) {
            ApplicantAnswer applicantAnswer = scenarioDto.getCurrentValue().get(fieldComponent.getId());
            Map.Entry<String, ApplicantAnswer> entry = new AbstractMap.SimpleEntry<>(fieldComponent.getId(), applicantAnswer);
            List<Map<String, String>> currentValueItems = AnswerUtil.toStringMapList(entry, true);
            FieldComponent idComponent = idComponentOptional.get();
            List<CycledApplicantAnswerItem> sortedItems = new LinkedList<>();
            for (Map<String, String> item : currentValueItems) {
                String itemId;
                if (StringUtils.hasText(item.get(idComponent.getId()))) {
                    itemId = item.get(idComponent.getId());
                } else {
                    itemId = UUID.randomUUID().toString();
                }
                CycledApplicantAnswerItem clonedCycledApplicantAnswerItem = jsonProcessingService.clone(
                        cycledApplicantAnswer.getItemOrDefault(itemId, new CycledApplicantAnswerItem(itemId)));
                Map<String, ApplicantAnswer> applicantAnswersForItem = clonedCycledApplicantAnswerItem.getItemAnswers();
                for (String key : item.keySet()) {
                    Map.Entry<String, ApplicantAnswer> answerEntry = AnswerUtil.createAnswerEntry(key, item.get(key));
                    applicantAnswersForItem.put(key, answerEntry.getValue());
                    serviceDescriptor.getFieldComponentById(key).ifPresent(field -> {
                        BaseComponent<?> component = componentRegistry.getComponent(field.getType());
                        if (component != null && component.isCycled()) {
                            ((AbstractCycledComponent<?>) component).addToCycledItemEsiaData(field, answerEntry.getValue(), clonedCycledApplicantAnswerItem);
                        }
                    });
                }
                listComponentItemUniquenessService.initUniqueKeys(fieldComponent, clonedCycledApplicantAnswerItem);
                sortedItems.add(clonedCycledApplicantAnswerItem);
            }
            List<List<Map<String, String>>> compositeErrors = listComponentItemUniquenessService.validateCycledItemsUniqueness(fieldComponent, sortedItems);
            scenarioDto.setUniquenessErrors(compositeErrors);
            if (scenarioDto.getUniquenessErrors().stream().anyMatch(list -> !list.isEmpty())) {
                return;
            }
            if (sortedItems.isEmpty()){
                throw ErrorModalException.getWindowWithErrorIcon("Компонент содержит невалидные данные", null);
            }
            cycledApplicantAnswer.setItems(sortedItems);
            cycledApplicantAnswer.setCurrentItemId(cycledApplicantAnswer.getItemsIds().get(0));
        }
        scenarioDto.setCurrentValue(Collections.emptyMap());
    }

    private void fillCycledApplicantAnswer(ScenarioDto scenarioDto, ServiceDescriptor serviceDescriptor) {
        CycledApplicantAnswer cycledApplicantAnswer = scenarioDto.getCycledApplicantAnswers().getCurrentAnswer();
        List<CycledApplicantAnswerItem> clonedCycledAnswerItems = cycledApplicantAnswer.getItems().stream()
                .map(jsonProcessingService::clone)
                .collect(Collectors.toList());
        int indexOfCurrentAnswerItem = cycledApplicantAnswer.getItemsIds().indexOf(cycledApplicantAnswer.getCurrentAnswerItem().getId());
        CycledApplicantAnswerItem clonedCurrentAnswerItem = clonedCycledAnswerItems.get(indexOfCurrentAnswerItem);
        Map<String, ApplicantAnswer> currentValue = scenarioDto.getCurrentValue();
        List<FieldComponent> currentValueComponents = scenarioDto.getDisplay().getComponents();
        for (FieldComponent fieldComponent : currentValueComponents) {
            BaseComponent<?> component = componentRegistry.getComponent(fieldComponent.getType());
            if (component != null && component.isCycled()) {
                ((AbstractCycledComponent<?>) component).addToCycledItemEsiaData(fieldComponent, currentValue.get(fieldComponent.getId()), clonedCurrentAnswerItem);
            }
        }
        FieldComponent component = serviceDescriptor.getFieldComponentById(cycledApplicantAnswer.getId())
                .orElseThrow(() -> new PguException(String.format("Компонент %s не найден", cycledApplicantAnswer.getId())));
        List<List<Map<String, String>>> compositeErrors = listComponentItemUniquenessService.validateCycledItemUniqueness(
                component, clonedCycledAnswerItems, indexOfCurrentAnswerItem);
        scenarioDto.setUniquenessErrors(compositeErrors);
        if (scenarioDto.getUniquenessErrors().stream().anyMatch(list -> !list.isEmpty())) {
            return;
        }
        clonedCurrentAnswerItem.getItemAnswers().putAll(currentValue);
        clonedCurrentAnswerItem.getFinishedScreens().add(scenarioDto.getDisplay().getId());
        cycledApplicantAnswer.setItems(clonedCycledAnswerItems);
        scenarioDto.setCurrentValue(Collections.emptyMap());
    }

    private void processCycledDisplayComponents(List<FieldComponent> components, ScenarioDto scenarioDto, ServiceDescriptor serviceDescriptor){
        CycledApplicantAnswer answer = scenarioDto.getCycledApplicantAnswers().getCurrentAnswer();
        components = components.stream().filter(
                ch -> componentRegistry.getComponent(ch.getType()) != null).collect(Collectors.toList()
        );
        List<FieldComponent> withAttrFieds = components.stream().filter(field -> field.getAttrs().containsKey(BasicComponentUtil.PRESET_ATTR_NAME)).collect(Collectors.toList());
        final CycledApplicantAnswerItem answerItem = answer.getCurrentAnswerItem();

        List<FieldComponent> subComponents = components.stream()
                .filter(component -> ComponentType.RepeatableFields == component.getType() && component.getAttrs().containsKey(FieldComponentUtil.COMPONENTS_KEY))
                .flatMap(component -> ((List<FieldComponent>) component.getAttrs().get(FieldComponentUtil.COMPONENTS_KEY)).stream())
                .collect(Collectors.toList());

        subComponents.forEach(c -> {
            ComponentResponse<?> componentValue = getComponentResponse(c, scenarioDto, serviceDescriptor, answerItem.getEsiaData());
            c.setValue(jsonProcessingService.componentDtoToString(componentValue));
        });

        for (FieldComponent fieldComponent : withAttrFieds) {
            BaseComponent<?> component = componentRegistry.getComponent(fieldComponent.getType());
            if (component != null && component.isCycled()) {
                ((AbstractCycledComponent<?>) component).processCycledComponent(fieldComponent, answerItem);
            }
        }
    }

    private void fillCycledDisplayComponents(DisplayRequest displayRequest, ScenarioDto scenarioDto, ServiceDescriptor serviceDescriptor) {
        if(Objects.nonNull(displayRequest.getInfoComponents())){
            this.processCycledDisplayComponents(displayRequest.getInfoComponents(),scenarioDto,serviceDescriptor);
        }
        this.processCycledDisplayComponents(displayRequest.getComponents(),scenarioDto,serviceDescriptor);
    }

    private ComponentResponse<?> getComponentResponse(FieldComponent component, ScenarioDto scenarioDto, ServiceDescriptor serviceDescriptor, Map<String, Object> esiaData) {
        BaseComponent<?> baseComponent = componentRegistry.getComponent(component.getType());
        if (Objects.isNull(baseComponent)) {
            return null;
        }
        if (baseComponent instanceof AbstractCycledComponent) {
            return ((AbstractCycledComponent<?>) baseComponent).getCycledInitialValue(component, esiaData);
        }
        return baseComponent.getInitialValue(component, scenarioDto, serviceDescriptor);
    }

    @Override
    public Set<String> getApplicantAnswerKeysWithCycled(Map<String, ApplicantAnswer> answerMap,
                                                        ServiceDescriptor serviceDescriptor,
                                                        String serviceId) {
        Set<String> keys = new HashSet<>();
        for (Map.Entry<String, ApplicantAnswer> entry : answerMap.entrySet()) {
            Optional<FieldComponent> optionalField = componentService.getFieldComponent(serviceId, entry, serviceDescriptor);
            if(optionalField.isEmpty()) continue;

            FieldComponent fieldComponent = optionalField.get();
            if (fieldComponent.isCycled()) {
                List<Map<String, String>> cycledAnswerItems;
                // после клонирования черновика в делириуме список превращается в одиночный элемент
                if (entry.getValue().getValue().startsWith("{")) {
                    cycledAnswerItems = List.of(AnswerUtil.toStringMap(entry, true));
                } else {
                    cycledAnswerItems = AnswerUtil.toStringMapList(entry, true);
                }
                for (int i = 0; i < cycledAnswerItems.size(); i++) {
                    int arrayIndex = i;
                    Map<String, String> itemsStringMap = cycledAnswerItems.get(arrayIndex);
                    Map<String, ApplicantAnswer> itemsAnswerMap = itemsStringMap.entrySet().stream()
                            .map(e -> AnswerUtil.createRepeatableItemAnswerEntry(e.getKey(), e.getValue(), arrayIndex))
                            .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
                    keys.add(fieldComponent.getId());
                    keys.addAll(getApplicantAnswerKeysWithCycled(itemsAnswerMap, serviceDescriptor, serviceId));
                }

            } else {
                keys.add(fieldComponent.getId());
            }
        }
        return keys;
    }

    abstract protected DescriptorService getDescriptorService(ScenarioRequest request);
}
