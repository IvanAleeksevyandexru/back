package ru.gosuslugi.pgu.fs.common.service;

import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import ru.gosuslugi.pgu.fs.common.exception.FormBaseException;
import ru.gosuslugi.pgu.dto.ApplicantAnswer;
import ru.gosuslugi.pgu.dto.DisplayRequest;
import ru.gosuslugi.pgu.dto.InitServiceDto;
import ru.gosuslugi.pgu.dto.ScenarioDto;
import ru.gosuslugi.pgu.dto.ScenarioRequest;
import ru.gosuslugi.pgu.dto.ScenarioResponse;
import ru.gosuslugi.pgu.dto.ServiceInfoDto;
import ru.gosuslugi.pgu.dto.descriptor.FieldComponent;
import ru.gosuslugi.pgu.dto.descriptor.ScreenDescriptor;
import ru.gosuslugi.pgu.dto.descriptor.ServiceDescriptor;
import ru.gosuslugi.pgu.fs.common.descriptor.DescriptorService;
import ru.gosuslugi.pgu.fs.common.exception.NoScreensFoundException;
import ru.gosuslugi.pgu.fs.common.helper.HelperScreenRegistry;
import ru.gosuslugi.pgu.fs.common.helper.ScreenHelper;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Component
@Setter
@Slf4j
public abstract class AbstractScreenService implements ScreenService {

    @Autowired
    protected ComponentService componentService;

    @Autowired
    protected JsonProcessingService jsonProcessingService;

    @Autowired
    private CycledScreenService cycledScreenService;

    @Autowired
    private AnswerValidationService answerValidationService;

    @Autowired
    protected HelperScreenRegistry screenRegistry;

    @Autowired
    private ScreenFinderService screenFinderService;

    @Autowired
    protected DisplayReferenceService displayReferenceService;

    protected abstract DescriptorService getDescriptorService();

    /**
     * Метод для получения дескриптора услуги
     * @param serviceId     Идентификатор услуги
     * @param scenarioDto   Сценарий
     * @return              Дескриптор услуги
     */
    protected ServiceDescriptor getServiceDescriptor(String serviceId, ScenarioDto scenarioDto) {
        return getDescriptorService().getServiceDescriptor(serviceId);
    }

    protected ScenarioDto initDefaultScenarioByScreen(String serviceId, ServiceInfoDto serviceInfoDto)  {
        ScenarioDto scenarioDto = new ScenarioDto();
        scenarioDto.setServiceInfo(serviceInfoDto);
        ServiceDescriptor currentDescriptor = getServiceDescriptor(serviceId, scenarioDto);
        if (currentDescriptor != null) {
            scenarioDto.setCurrentScenarioId(1L);
            scenarioDto.getCachedAnswers().clear();
            scenarioDto.setCurrentValue(new HashMap<>());
            scenarioDto.setApplicantAnswers(new HashMap<>());
        }
        return scenarioDto;
    }

    @Override
    public ScenarioResponse getInitScreen(String serviceId, InitServiceDto initServiceDto) {
        ScenarioDto scenarioDto = initDefaultScenarioByScreen(serviceId, initServiceDto.getServiceInfo());
        ScenarioResponse scenarioResponse = new ScenarioResponse();
        scenarioResponse.setScenarioDto(scenarioDto);
        return scenarioResponse;
    }

    @Override
    public ScenarioResponse skipStep(ScenarioRequest request, String serviceId) {
        ScenarioResponse scenarioResponse = beforeNextScreen(request);
        ServiceDescriptor serviceDescriptor = getServiceDescriptor(serviceId, request.getScenarioDto());
        return prepareNextScreen(request, serviceId, scenarioResponse, serviceDescriptor);
    }

    @Override
    public ScenarioResponse getNextScreen(ScenarioRequest request, String serviceId) {
        ScenarioResponse scenarioResponse = beforeNextScreen(request);
        ScenarioDto scenarioDto = request.getScenarioDto();
        ServiceDescriptor serviceDescriptor = getServiceDescriptor(serviceId, scenarioDto);
        scenarioResponse = validate(scenarioResponse.getScenarioDto(), scenarioResponse, serviceDescriptor);
        if(scenarioResponse.getScenarioDto().getErrors().isEmpty()){
            componentService.preloadComponents(scenarioDto.getDisplay().getId(), scenarioDto, serviceDescriptor);
            scenarioResponse = prepareNextScreen(request, serviceId, scenarioResponse, serviceDescriptor);
        }
        return scenarioResponse;
    }

    protected ScenarioResponse prepareNextScreen(ScenarioRequest request, String serviceId, ScenarioResponse scenarioResponse,
                                                 ServiceDescriptor serviceDescriptor) {
        ScenarioDto scenarioDto = request.getScenarioDto();

        ScenarioResponse cycledScreenServiceNextScreen = cycledScreenService.getNextScreen(request, serviceId);
        if (cycledScreenServiceNextScreen != null) {
            return afterNextScreen(cycledScreenServiceNextScreen, serviceId);
        }
        ScreenDescriptor screenDescriptor = screenFinderService.findScreenDescriptorByRules(scenarioDto, serviceDescriptor, null);

        if (log.isDebugEnabled()) log.debug("Screen descriptor {}", screenDescriptor);
        ScreenHelper screenHelper = screenDescriptor != null ? screenRegistry.getHelper(screenDescriptor.getType()) : null;
        if (Objects.nonNull(screenHelper)) {
            screenDescriptor = screenHelper.processScreen(screenDescriptor, scenarioDto);
        }

        if (screenDescriptor != null) {
            scenarioDto.getFinishedAndCurrentScreens().add(screenDescriptor.getId());
            scenarioDto.addToCachedAnswers(scenarioDto.getCurrentValue());
            scenarioDto.getApplicantAnswers().putAll(scenarioDto.getCurrentValue());
            scenarioDto.setCurrentValue(new HashMap<>());
            DisplayRequest displayRequest = new DisplayRequest(screenDescriptor, componentService.getScreenFields(screenDescriptor,
                    scenarioDto, serviceDescriptor));
            displayReferenceService.processDisplayRefs(displayRequest, scenarioDto);
            scenarioDto.setDisplay(displayRequest);

            scenarioResponse.setScenarioDto(scenarioDto);
            return this.afterNextScreen(scenarioResponse, serviceId);
        }
        return null;
    }

    @Override
    public ScenarioResponse getPrevScreen(ScenarioRequest request, String serviceId, Integer stepsBack, String screenId) {
        if (stepsBack != null && screenId != null) {
            throw new FormBaseException("Ошибка конфигурации параметров возврата");
        }

        if (screenId != null) {
            return getPrevScreen(request, serviceId, screenId);
        }
        return getPrevScreen(request, serviceId, stepsBack);
    }

    @Override
    public ScenarioResponse getPrevScreen(ScenarioRequest request, String serviceId, String screenId) {
        if (screenId != null && !request.getScenarioDto().getFinishedAndCurrentScreens().contains(screenId)) {
            throw new NoScreensFoundException(String.format("Не найден экран перехода с идентификатором %s", screenId));
        }
        ScenarioResponse scenarioResponse = null;

        while(scenarioResponse == null || !scenarioResponse.getScenarioDto().getDisplay().getId().equals(screenId)) {
            scenarioResponse = getPrevScreen(request, serviceId);
        }
        return scenarioResponse;
    }

    @Override
    public ScenarioResponse getPrevScreen(ScenarioRequest request, String serviceId, Integer stepsBack) {
        ScenarioResponse scenarioResponse = null;

        for (int i = 0; i < stepsBack; i++) {
            if (scenarioResponse != null && !hasPrevStep(scenarioResponse)) {
                return scenarioResponse;
            }
            scenarioResponse = getPrevScreen(request, serviceId);
        }
        return scenarioResponse;
    }

    @Override
    public ScenarioResponse getPrevScreen(ScenarioRequest request, String serviceId) {
        ScenarioDto dto = request.getScenarioDto();
        ServiceDescriptor serviceDescriptor = getServiceDescriptor(serviceId, dto);

        if(dto.getFinishedAndCurrentScreens().size() < 2) {
            return this.getInitScreen(serviceId, new InitServiceDto());
        }
        if (!dto.getFinishedAndCurrentScreens().contains(dto.getDisplay().getId())) {
            ScenarioResponse scenarioResponseForLoopStep = cycledScreenService.getPrevScreen(request, serviceId);
            if (scenarioResponseForLoopStep != null) {
                return afterPrevScreen(scenarioResponseForLoopStep, serviceId);
            }
        }

        String lastScreenId = dto.getFinishedAndCurrentScreens().getLast();
        if (dto.getDisplay().getId().equals(lastScreenId)) {
            serviceDescriptor.getScreenDescriptorById(lastScreenId).ifPresent(screen -> {
                Map<String, ApplicantAnswer> answersToCache = dto.getApplicantAnswers()
                        .entrySet()
                        .stream()
                        .filter(e -> screen.getComponentIds().contains(e.getKey()))
                        .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
                dto.getCachedAnswers().putAll(answersToCache);

                answersToCache.keySet().forEach(dto.getApplicantAnswers()::remove);
            });
            dto.getFinishedAndCurrentScreens().removeLastOccurrence(lastScreenId);
        }

        String nextScreenId = dto.getFinishedAndCurrentScreens().getLast();
        Optional<ScreenDescriptor> foundScreen = serviceDescriptor.getScreenDescriptorById(nextScreenId);

        if(foundScreen.isEmpty()){
            throw new FormBaseException("Descriptor not found");
        }
        ScreenDescriptor screenDescriptor = foundScreen.get();
        List<FieldComponent> fieldComponentsForScreen = serviceDescriptor.getFieldComponentsForScreen(screenDescriptor);
        dto.setApplicantAnswers(dto.getApplicantAnswers()
                .entrySet()
                .stream()
                .filter(entry-> fieldComponentsForScreen.stream().noneMatch(fc -> fc.getId().equals(entry.getKey())))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue)));

        Optional<FieldComponent> cycledFieldComponent = fieldComponentsForScreen.stream()
                .filter(f -> !CollectionUtils.isEmpty(f.getAttrs()))
                .filter(f -> (Boolean) f.getAttrs().getOrDefault("isCycled", Boolean.FALSE))
                .findAny();

        if (cycledFieldComponent.isPresent()) {
            request.getScenarioDto().getCycledApplicantAnswers().setCurrentAnswerId(cycledFieldComponent.get().getId());
            ScenarioResponse scenarioResponseForLoopStep = cycledScreenService.getPrevScreen(request, serviceId);
            if (scenarioResponseForLoopStep != null) {
                return afterPrevScreen(scenarioResponseForLoopStep, serviceId);
            }
        }

        ScreenHelper screenHelper = screenRegistry.getHelper(screenDescriptor.getType());
        if (Objects.nonNull(screenHelper)) {
            screenDescriptor = screenHelper.processScreen(screenDescriptor, dto);
        }
        DisplayRequest displayRequest = new DisplayRequest(screenDescriptor, componentService.getScreenFields(screenDescriptor,
                dto, serviceDescriptor));
        displayReferenceService.processDisplayRefs(displayRequest, dto);
        dto.setDisplay(displayRequest);
        dto.setCurrentValue(new HashMap<>());

        ScenarioResponse scenarioResponse = new ScenarioResponse();
        scenarioResponse.setScenarioDto(dto);
        return afterPrevScreen(scenarioResponse, serviceId);
    }

    @Override
    public void saveCacheToDraft(String serviceId, ScenarioRequest request) {}

    protected ScenarioResponse afterNextScreen(ScenarioResponse scenarioResponse, String serviceId) {
        return scenarioResponse;
    }

    protected ScenarioResponse beforeNextScreen(ScenarioRequest request) {
        ScenarioResponse scenarioResponse = new ScenarioResponse();
        ScenarioDto scenarioDto = request.getScenarioDto();
        scenarioResponse.setScenarioDto(scenarioDto);
        return scenarioResponse;
    }

    protected ScenarioResponse afterPrevScreen(ScenarioResponse scenarioResponse,String serviceId) {
        return scenarioResponse;
    }

    protected Boolean hasPrevStep(ScenarioResponse scenarioResponse) {
        return scenarioResponse.getScenarioDto().getFinishedAndCurrentScreens() != null &&
                scenarioResponse.getScenarioDto().getFinishedAndCurrentScreens().size() > 1;
    }

    protected ScenarioResponse validate(ScenarioDto scenarioDto, ScenarioResponse scenarioResponse, ServiceDescriptor serviceDescriptor) {
        scenarioDto.getErrors().clear();
        ConcurrentHashMap<String, String> validationErrors = answerValidationService.validateAnswers(scenarioDto, serviceDescriptor);
        if (!validationErrors.isEmpty()) {
            scenarioDto.setErrors(validationErrors);
            scenarioDto.addToCachedAnswers(scenarioDto.getCurrentValue());
            scenarioResponse.setScenarioDto(scenarioDto);
        }
        return scenarioResponse;
    }
}
