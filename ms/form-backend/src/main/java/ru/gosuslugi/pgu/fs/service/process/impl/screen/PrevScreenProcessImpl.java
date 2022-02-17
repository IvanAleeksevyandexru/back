package ru.gosuslugi.pgu.fs.service.process.impl.screen;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.web.context.WebApplicationContext;
import ru.gosuslugi.pgu.dto.ApplicantAnswer;
import ru.gosuslugi.pgu.dto.DisplayRequest;
import ru.gosuslugi.pgu.dto.InitServiceDto;
import ru.gosuslugi.pgu.dto.ScenarioDto;
import ru.gosuslugi.pgu.dto.ScenarioResponse;
import ru.gosuslugi.pgu.dto.descriptor.FieldComponent;
import ru.gosuslugi.pgu.dto.descriptor.ScreenDescriptor;
import ru.gosuslugi.pgu.dto.descriptor.ServiceDescriptor;
import ru.gosuslugi.pgu.dto.descriptor.types.ScreenType;
import ru.gosuslugi.pgu.fs.common.component.BaseComponent;
import ru.gosuslugi.pgu.fs.common.component.ComponentRegistry;
import ru.gosuslugi.pgu.fs.common.descriptor.MainDescriptorService;
import ru.gosuslugi.pgu.fs.common.exception.FormBaseException;
import ru.gosuslugi.pgu.fs.common.helper.HelperScreenRegistry;
import ru.gosuslugi.pgu.fs.common.helper.ScreenHelper;
import ru.gosuslugi.pgu.fs.common.service.ComponentService;
import ru.gosuslugi.pgu.fs.common.service.CycledScreenService;
import ru.gosuslugi.pgu.fs.common.service.DisplayReferenceService;
import ru.gosuslugi.pgu.fs.exception.DraftNotEditableException;
import ru.gosuslugi.pgu.fs.pgu.client.impl.OrderStatuses;
import ru.gosuslugi.pgu.fs.service.impl.FormScenarioDtoServiceImpl;
import ru.gosuslugi.pgu.fs.service.process.PrevScreenProcess;

import java.util.AbstractMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * Описание этапов процесса перехода на предыдущий экран
 */
@Slf4j
@Component
@Scope(value = WebApplicationContext.SCOPE_REQUEST, proxyMode = ScopedProxyMode.TARGET_CLASS)
@RequiredArgsConstructor
public class PrevScreenProcessImpl extends AbstractScreenProcess<PrevScreenProcess> implements PrevScreenProcess {

    @Getter
    private final MainDescriptorService mainDescriptorService;
    private final CycledScreenService cycledScreenService;
    private final HelperScreenRegistry screenRegistry;
    private final ComponentService componentService;
    private final DisplayReferenceService displayReferenceService;
    private final FormScenarioDtoServiceImpl scenarioDtoService;
    private final ComponentRegistry componentRegistry;

    @Override
    public Boolean onlyInitScreenWasShow() {
        return request.getScenarioDto().getFinishedAndCurrentScreens().size() < 2;
    }

    @Override
    public void getInitScreen() {
        InitServiceDto initServiceDto = new InitServiceDto();
        ScenarioDto scenarioDto = scenarioDtoService.createInitScenario(mainDescriptorService.getServiceDescriptor(serviceId), initServiceDto);
        response = new ScenarioResponse();
        response.setScenarioDto(scenarioDto);
    }

    @Override
    public boolean isCycledScreen() {
        ScenarioDto dto = request.getScenarioDto();
        return !dto.getFinishedAndCurrentScreens().contains(dto.getDisplay().getId());
    }

    @Override
    public void setResponseIfPrevScreenCycled() {
        ScenarioResponse scenarioResponseForLoopStep = cycledScreenService.getPrevScreen(request, serviceId);
        if (scenarioResponseForLoopStep != null) {
            response = scenarioResponseForLoopStep;
        }
    }

    @Override
    public boolean checkResponseIsNull() {
        return response == null;
    }

    @Override
    public void putAnswersToCache(){
        ScenarioDto dto = request.getScenarioDto();
        String lastScreenId = dto.getFinishedAndCurrentScreens().getLast();
        // если скрин цикличный - пропускаем
        if (dto.getDisplay().getId().equals(lastScreenId)) {
            log.info("Put answers to cache" + formatScenarioLogFields());
            serviceDescriptor.getScreenDescriptorById(lastScreenId).ifPresent(screen -> {
                Map<String, ApplicantAnswer> answersToCache = dto.getApplicantAnswers()
                        .entrySet()
                        .stream()
                        .filter(e -> screen.getComponentIds().contains(e.getKey()))
                        .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

                dto.getCachedAnswers().putAll(answersToCache);
                answersToCache.keySet().forEach(dto.getApplicantAnswers()::remove);
            });
        }
    }

    @Override
    public void removeScreenFromFinished(){
        ScenarioDto dto = request.getScenarioDto();
        String lastScreenId = dto.getFinishedAndCurrentScreens().getLast();
        // если скрин цикличный его не нужно удалять
        if (dto.getDisplay().getId().equals(lastScreenId)) {
            log.info("Removing screen from finished" + formatScenarioLogFields());
            dto.getFinishedAndCurrentScreens().removeLastOccurrence(lastScreenId);
        }
    }

    @Override
    public void removeAnswersFromDto() {
        log.info("Removing answers from DTO" + formatScenarioLogFields());

        var dto = request.getScenarioDto();
        ScreenDescriptor screenDescriptor = getScreenDescriptor();
        List<FieldComponent> fieldComponentsForScreen = serviceDescriptor.getFieldComponentsForScreen(screenDescriptor);
        dto.getCachedAnswers().putAll(dto.getApplicantAnswers()
                .entrySet()
                .stream()
                .filter(entry-> fieldComponentsForScreen.stream().anyMatch(fc -> fc.getId().equals(entry.getKey())))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue)));
        dto.setApplicantAnswers(dto.getApplicantAnswers()
                .entrySet()
                .stream()
                .filter(entry-> fieldComponentsForScreen.stream().noneMatch(fc -> fc.getId().equals(entry.getKey())))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue)));
    }

    @Override
    public void setResponseIfScreenHasCycledComponent() {
        ScreenDescriptor screenDescriptor = getScreenDescriptor();
        List<FieldComponent> fieldComponentsForScreen = serviceDescriptor.getFieldComponentsForScreen(screenDescriptor);
        Optional<FieldComponent> cycledFieldComponent = fieldComponentsForScreen.stream()
                .filter(f -> !CollectionUtils.isEmpty(f.getAttrs()))
                .filter(f -> (Boolean) f.getAttrs().getOrDefault("isCycled", Boolean.FALSE))
                .findAny();

        if (cycledFieldComponent.isPresent()) {
            request.getScenarioDto().getCycledApplicantAnswers().setCurrentAnswerId(cycledFieldComponent.get().getId());
            final var lastCycledAnswerItem =
                    request.getScenarioDto().getCycledApplicantAnswerContext().getCycledApplicantAnswerItem();
            if (lastCycledAnswerItem != null) {
                request.getScenarioDto().getApplicantAnswers().putAll(lastCycledAnswerItem.getItemAnswers());
            }
            ScenarioResponse scenarioResponseForLoopStep = cycledScreenService.getPrevScreen(request, serviceId);
            if (scenarioResponseForLoopStep != null) {
                log.info("Setting response for cycled component" + formatScenarioLogFields());
                response = scenarioResponseForLoopStep;
            }
        }
    }

    @Override
    public void calculateNextScreen() {
        log.info("Calculating next screen" + formatScenarioLogFields());

        var dto = request.getScenarioDto();
        ScreenDescriptor screenDescriptor = getScreenDescriptor();
        while (screenDescriptor.getType().equals(ScreenType.EMPTY) ) {
            dto.getFinishedAndCurrentScreens().removeLastOccurrence(screenDescriptor.getId());
            screenDescriptor = getScreenDescriptor();
        }
        ScreenHelper screenHelper = screenRegistry.getHelper(screenDescriptor.getType());
        if (Objects.nonNull(screenHelper)) {
            screenDescriptor = screenHelper.processScreen(screenDescriptor, dto);
        }
        DisplayRequest displayRequest = new DisplayRequest(screenDescriptor, componentService.getScreenFields(screenDescriptor, dto, serviceDescriptor));
        displayReferenceService.processDisplayRefs(displayRequest, dto);
        dto.setDisplay(displayRequest);
        dto.setCurrentValue(new HashMap<>());
        dto.setLogicComponents(componentService.getLogicFields(screenDescriptor, dto, serviceDescriptor));

        serviceDescriptor.getScreenDescriptorById(displayRequest.getId()).ifPresent(e -> {
            var componentIds = e.getComponentIds();
            componentIds.forEach(v -> {
                var answer = dto.getCachedAnswers().get(v);
                if(Objects.nonNull(answer)){
                    var entry = new AbstractMap.SimpleEntry<>(v, answer);
                    var component = componentService.getFieldComponent(serviceId, entry, serviceDescriptor);
                    BaseComponent<?> componentBase = componentRegistry.getComponent(component.get().getType());
                    if(Objects.nonNull(componentBase) && !componentBase.shouldBeSavedInCachedAnswers(component.get(), dto)){
                        dto.getCachedAnswers().remove(v);
                    }
                }
            });
        });
        response = new ScenarioResponse();
        response.setScenarioDto(dto);
    }

    @Override
    public PrevScreenProcessImpl getProcess() {
        return this;
    }

    private ScreenDescriptor getScreenDescriptor() {
        var dto = request.getScenarioDto();
        String nextScreenId = dto.getFinishedAndCurrentScreens().getLast();
        Optional<ScreenDescriptor> foundScreen = serviceDescriptor.getScreenDescriptorById(nextScreenId);
        if (foundScreen.isEmpty()) {
            throw new FormBaseException("Descriptor not found");
        }

        return foundScreen.get();
    }

    @Override
    public void removeCycledAnswers() {
        log.info("Removing cycled answers" + formatScenarioLogFields());
        var dto = request.getScenarioDto();
        dto.getCurrentValue().keySet().forEach(v ->
                dto.getCycledApplicantAnswers().removeAnswer(v)
        );
    }

    private Optional<FieldComponent> findComponentForPredicate(ScenarioDto scenarioDto,
                                                               ServiceDescriptor serviceDescriptor,
                                                               Predicate<FieldComponent> predicate) {
        return scenarioDto.getDisplay().getComponents().stream()
                .map(fieldComponent -> serviceDescriptor.getFieldComponentById(fieldComponent.getId()))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .filter(predicate)
                .findAny();
    }

    private boolean hasCheckForDuplicate() {
        Optional<FieldComponent> component = findComponentForPredicate(response.getScenarioDto(), serviceDescriptor, FieldComponent::getCheckForDuplicate);
        return component.isPresent();
    }

    @Override
    public void checkEditable() {
        var scenarioDto = this.response.getScenarioDto();
        var serviceInfo = scenarioDto.getServiceInfo();
        if(serviceInfo != null && serviceInfo.getStatusId() != null && !OrderStatuses.isEditableStatus(serviceInfo.getStatusId())){
            throw new DraftNotEditableException(
                    DraftNotEditableException.createWindow(serviceDescriptor.getService(), hasCheckForDuplicate()),
                    "Заявление находится в нередактируемом статусе"
            );
        }
    }

}
