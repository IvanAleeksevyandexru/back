package ru.gosuslugi.pgu.fs.service.impl;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import ru.gosuslugi.pgu.common.esia.search.dto.UserPersonalData;
import ru.gosuslugi.pgu.draft.DraftClient;
import ru.gosuslugi.pgu.draft.model.DraftHolderDto;
import ru.gosuslugi.pgu.dto.DisplayRequest;
import ru.gosuslugi.pgu.dto.InitServiceDto;
import ru.gosuslugi.pgu.dto.ScenarioDto;
import ru.gosuslugi.pgu.dto.ScenarioRequest;
import ru.gosuslugi.pgu.dto.ScenarioResponse;
import ru.gosuslugi.pgu.dto.ServiceInfoDto;
import ru.gosuslugi.pgu.dto.descriptor.FieldComponent;
import ru.gosuslugi.pgu.dto.descriptor.ServiceDescriptor;
import ru.gosuslugi.pgu.dto.descriptor.types.ScreenType;
import ru.gosuslugi.pgu.fs.common.descriptor.DescriptorService;
import ru.gosuslugi.pgu.fs.common.descriptor.MainDescriptorService;
import ru.gosuslugi.pgu.fs.common.helper.ScreenHelper;
import ru.gosuslugi.pgu.fs.common.service.AbstractScreenService;
import ru.gosuslugi.pgu.fs.descriptor.SubDescriptorService;
import ru.gosuslugi.pgu.fs.esia.EsiaCacheService;
import ru.gosuslugi.pgu.fs.service.SubScreenService;

import java.util.*;
import java.util.stream.Collectors;

@Component
@AllArgsConstructor
@Slf4j
public class SubScreenServiceImpl extends AbstractScreenService implements SubScreenService {

    private final SubDescriptorService subDescriptorService;
    private final DraftClient draftClient;
    private final UserPersonalData personalData;
    private final EsiaCacheService esiaRestCacheService;
    private final MainScreenServiceImpl mainScreenService;
    private final MainDescriptorService mainDescriptorService;

    @Override
    public ScenarioResponse getInitScreen(String serviceId, InitServiceDto initServiceDto, String parentServiceId) {
        ScenarioDto scenarioDto = initDefaultScenarioByScreen(serviceId, initServiceDto.getServiceInfo(), parentServiceId);
        ScenarioResponse scenarioResponse = new ScenarioResponse();
        scenarioResponse.setScenarioDto(scenarioDto);
        return scenarioResponse;
    }

    protected ScenarioDto initDefaultScenarioByScreen(String serviceId, ServiceInfoDto serviceInfoDto, String parentServiceId)  {
        ScenarioDto scenarioDto = new ScenarioDto();
        scenarioDto.setParentServiceId(parentServiceId);
        scenarioDto.setServiceInfo(serviceInfoDto);
        ServiceDescriptor currentDescriptor = getServiceDescriptor(serviceId, scenarioDto);
        if (currentDescriptor != null) {
            scenarioDto.setCurrentScenarioId(1L);
            scenarioDto.getCachedAnswers().clear();
            scenarioDto.setCurrentValue(new HashMap<>());
            scenarioDto.setApplicantAnswers(new HashMap<>());

            String initScreenId = currentDescriptor.getInit();
            currentDescriptor.getScreenDescriptorById(initScreenId).ifPresent(screen -> {
                ScreenHelper screenHelper = screenRegistry.getHelper(screen.getType());
                if (Objects.nonNull(screenHelper)) {
                    screen = screenHelper.processScreen(screen, scenarioDto);
                }
                DisplayRequest displayRequest = new DisplayRequest(screen, componentService.getScreenFields(screen, scenarioDto, currentDescriptor));
                scenarioDto.getFinishedAndCurrentScreens().add(screen.getId());
                scenarioDto.setDisplay(displayRequest);
            });
        }
        return scenarioDto;
    }

    @Override
    protected DescriptorService getDescriptorService() {
        return subDescriptorService;
    }

    @Override
    public ScenarioResponse getNextScreen(ScenarioRequest request, String serviceId) {
        if (request.getIsInternalScenario()) {
            ScenarioResponse response = super.getNextScreen(request, serviceId);
            response.setCallBackOrderId(request.getCallBackOrderId());
            response.setCallBackServiceId(request.getCallBackServiceId());
            response.setIsInternalScenario(true);
            return response;
        }
        return originalMainScenarioResponse(request, serviceId);
    }

    @Override
    public ScenarioResponse getPrevScreen(ScenarioRequest request, String serviceId) {
        ScenarioDto scenarioDto = request.getScenarioDto();
        if (CollectionUtils.isEmpty(scenarioDto.getFinishedAndCurrentScreens()) || isInitSubScreenDisplayed(request, serviceId)) {
            return originalMainScenarioResponse(request, serviceId);
        }
        ScenarioResponse response = super.getPrevScreen(request, serviceId);
        response.setCallBackOrderId(request.getCallBackOrderId());
        response.setCallBackServiceId(request.getCallBackServiceId());
        ScenarioDto responseScenarioDto = response.getScenarioDto();
        responseScenarioDto.setServiceId(serviceId);
        response.setIsInternalScenario(true);
        return response;
    }

    private boolean isInitSubScreenDisplayed(ScenarioRequest request, String serviceId) {
        String currentDisplay = request.getScenarioDto().getDisplay().getId();
        String descriptorInitDisplay = subDescriptorService.getServiceDescriptor(serviceId).getInit();
        return currentDisplay.equals(descriptorInitDisplay);
    }

    @Override
    protected Boolean hasPrevStep(ScenarioResponse scenarioResponse) {
        // для вложенных экранов последним будет внешний экран из которого был вызван вложенный сценарий!
        return scenarioResponse.getIsInternalScenario();
    }

    private ScenarioResponse originalMainScenarioResponse(ScenarioRequest scenarioRequest, String serviceId){
        Long userId = personalData.getUserId();
        String token = personalData.getToken();
        DraftHolderDto draftHolderDto = draftClient.getDraftById(scenarioRequest.getCallBackOrderId(), userId, personalData.getOrgId());
        ScenarioDto scenarioDto = draftHolderDto.getBody();
        scenarioDto.setServiceId(scenarioRequest.getCallBackServiceId());
        esiaRestCacheService.clearCache(userId, token);
        subDescriptorService.presetFieldsForCurrentStep(serviceId, scenarioDto);
        scenarioDto = checkAndMoveToNextStep(scenarioRequest.getScenarioDto(), scenarioDto, serviceId);

        ScenarioResponse mainScreenResponse = new ScenarioResponse();
        mainScreenResponse.setIsInternalScenario(false);
        mainScreenResponse.setScenarioDto(scenarioDto);
        return validate(scenarioDto, mainScreenResponse, mainDescriptorService.getServiceDescriptor(scenarioDto.getServiceDescriptorId()));
    }

    private ScenarioDto checkAndMoveToNextStep(ScenarioDto internalDto, ScenarioDto scenarioDto, String serviceId) {
        boolean isMoveNextStep = Optional.of(getServiceDescriptor(serviceId, scenarioDto))
            .map(ServiceDescriptor::getParameters)
            .filter(parameters -> parameters.containsKey("moveMainScenarioToNextStep"))
            .isPresent();

        ScenarioRequest scenarioRequest = new ScenarioRequest();
        if (isMoveNextStep && internalDto.getApplicantAnswers().containsKey("internalProcessSuccess")) {
            scenarioRequest.setIsInternalScenario(false);
            scenarioRequest.setScenarioDto(scenarioDto);
            return mainScreenService.getNextScreen(scenarioRequest, scenarioDto.getServiceCode()).getScenarioDto();
        }
        if ((Objects.nonNull(scenarioDto.getDisplay())) && ScreenType.EMPTY.equals(scenarioDto.getDisplay().getType())) {
            scenarioRequest.setScenarioDto(scenarioDto);
            return mainScreenService.getPrevScreen(scenarioRequest, scenarioDto.getServiceCode()).getScenarioDto();
        }
        return scenarioDto;
    }

    /**
     * Метод получает дескриптор сервиса и если в сценарии указана родительская услуга - заменяет если нужно в дескрипторе компоненты
     * @param serviceId     Идентификатор услуги
     * @param scenarioDto   Сценарий
     * @return              Дескриптор услуги
     */
    @Override
    protected ServiceDescriptor getServiceDescriptor(String serviceId, ScenarioDto scenarioDto) {
        ServiceDescriptor descriptor = getDescriptorService().getServiceDescriptor(serviceId);
        if (descriptor != null && scenarioDto.getParentServiceId() != null) {
            ServiceDescriptor parentServiceDescriptor = mainDescriptorService.getServiceDescriptor(scenarioDto.getParentServiceId());
            Map<String, List<FieldComponent>> overrideInternalComponents = parentServiceDescriptor.getOverrideSubServiceComponents();
            if (!CollectionUtils.isEmpty(overrideInternalComponents) && overrideInternalComponents.containsKey(serviceId)) {
                return copyAndOverrideApplicationFields(descriptor, overrideInternalComponents.get(serviceId));
            }
        }
        return descriptor;
    }

    /** Копируем дескриптор и заменяем в нем компоненты */
    private ServiceDescriptor copyAndOverrideApplicationFields(ServiceDescriptor descriptor, List<FieldComponent> overrideComponents) {
        ServiceDescriptor newDescriptor = ServiceDescriptor.getCopy(descriptor);
        List<String> replaceComponentIds = overrideComponents.stream()
            .map(FieldComponent::getId)
            .collect(Collectors.toList());
        List<FieldComponent> fields = newDescriptor.getApplicationFields().stream()
            .filter(it -> !replaceComponentIds.contains(it.getId()))
            .collect(Collectors.toList());
        newDescriptor.setApplicationFields(fields);
        newDescriptor.getApplicationFields().addAll(overrideComponents);
        return newDescriptor;
    }
}
