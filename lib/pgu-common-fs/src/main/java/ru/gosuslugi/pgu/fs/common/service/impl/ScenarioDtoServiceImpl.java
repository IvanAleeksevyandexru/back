package ru.gosuslugi.pgu.fs.common.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.logging.log4j.util.Strings;
import org.springframework.util.StringUtils;
import ru.gosuslugi.pgu.common.core.exception.EntityNotFoundException;
import ru.gosuslugi.pgu.fs.common.exception.NoScreensFoundException;
import ru.gosuslugi.pgu.components.descriptor.types.Stage;
import ru.gosuslugi.pgu.dto.ApplicantAnswer;
import ru.gosuslugi.pgu.dto.ApplicantRole;
import ru.gosuslugi.pgu.dto.DisplayRequest;
import ru.gosuslugi.pgu.dto.InitServiceDto;
import ru.gosuslugi.pgu.dto.ScenarioDto;
import ru.gosuslugi.pgu.dto.descriptor.ScreenDescriptor;
import ru.gosuslugi.pgu.dto.descriptor.ScreenRule;
import ru.gosuslugi.pgu.dto.descriptor.ServiceDescriptor;
import ru.gosuslugi.pgu.fs.common.helper.HelperScreenRegistry;
import ru.gosuslugi.pgu.fs.common.helper.ScreenHelper;
import ru.gosuslugi.pgu.fs.common.service.ComponentService;
import ru.gosuslugi.pgu.fs.common.service.ComputeAnswerService;
import ru.gosuslugi.pgu.fs.common.service.DisplayReferenceService;
import ru.gosuslugi.pgu.fs.common.service.JsonProcessingService;
import ru.gosuslugi.pgu.fs.common.service.ScenarioDtoService;
import ru.gosuslugi.pgu.fs.common.service.ScreenFinderService;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import static org.springframework.util.StringUtils.hasText;

/**
 * Выполняет различные действия над DTO
 * @see ScenarioDto
 */
@RequiredArgsConstructor
@Slf4j
public class ScenarioDtoServiceImpl implements ScenarioDtoService {

    private final static String DEFAULT_STAGE = Stage.Applicant.name();
    private final static ApplicantRole DEFAULT_ROLE = ApplicantRole.Applicant;

    private final ComponentService componentService;
    private final HelperScreenRegistry screenRegistry;
    private final ScreenFinderService screenFinderService;
    private final JsonProcessingService jsonProcessingService;
    private final DisplayReferenceService displayReferenceService;
    private final ComputeAnswerService computeAnswerService;

    /**
     * Восстанавливает DTO из черновика и устанавливает начальный экран в зависимости от роли, стейджа и стадии
     * заполнения пользователем
     * @param scenarioDto сценарий сервиса форм
     * @param serviceId идентификатор услуги
     * @return true если сченарий был изменен и требует сохранения в сервисе черновиков
     * @see #prepareScenarioDto(ScenarioDto, ServiceDescriptor, String, String, ApplicantRole)
     */
    @Override
    public boolean prepareScenarioDto(ScenarioDto scenarioDto, ServiceDescriptor currentDescriptor, String serviceId) {
        return prepareScenarioDto(scenarioDto, currentDescriptor, serviceId, DEFAULT_STAGE, DEFAULT_ROLE);
    }

    /**
     * Восстанавливает DTO из черновика и устанавливает начальный экран в зависимости от роли, стейджа и стадии
     * заполнения пользователем
     * @param scenarioDto сценарий сервиса форм
     * @param serviceId идентификатор услуги
     * @param stage стейдж
     * @param role роль пользователя
     * @return true если сченарий был изменен и требует сохранения в сервисе черновиков
     * @throws NoScreensFoundException если не удалось найти начальный экран для услуги
     */
    @Override
    public boolean prepareScenarioDto(ScenarioDto scenarioDto, ServiceDescriptor currentDescriptor, String serviceId, String stage, ApplicantRole role) {
        boolean needSaveDraft = false;
        if (currentDescriptor != null) {
            // Ищем начальный экран в соответствии с описанием услуги
            String foundInitScreenId = findInitScreenId(currentDescriptor, stage, role, scenarioDto);
            Optional<ScreenDescriptor> initScreen = currentDescriptor.getScreenDescriptorById(foundInitScreenId);

            // Экран для отображения пользователю
            ScreenDescriptor screenToProcess;

            if(initScreen.isPresent() && !scenarioDto.getFinishedAndCurrentScreens().contains(initScreen.get().getId())) {
                // Первый экран еще не посещался
                screenToProcess = initScreen.get();

                // Очищаем посещенные экраны, т.к. начинаем прохождение ветки сценария заново
                scenarioDto.getFinishedAndCurrentScreens().clear();

                // Для json2 надо добавлять первый экран
                scenarioDto.getFinishedAndCurrentScreens().add(screenToProcess.getId());

                needSaveDraft = true;
            } else {
                // Первый экран посещался, обрабатываем экран из черновика
                // Не менять на scenarioDto.getFinishedAndCurrentScreens().getLast()
                // Для циклов туда не сохраняются пройденные экраны
                String lastScreenId = scenarioDto.getDisplay().getId();
                screenToProcess = currentDescriptor.getScreenDescriptorById(lastScreenId).orElse(null);

                if(screenToProcess == null) {
                    throw new NoScreensFoundException(String.format("В описании услуги %s не найден экран с идентификатором %s", currentDescriptor.getService(), lastScreenId));
                }
            }

            ScreenHelper screenHelper = screenRegistry.getHelper(screenToProcess.getType());
            if (Objects.nonNull(screenHelper)) {
                screenToProcess = screenHelper.processScreen(screenToProcess, scenarioDto);
            }

            DisplayRequest displayRequest = new DisplayRequest(screenToProcess, componentService.getScreenFields(screenToProcess, scenarioDto, currentDescriptor));
            displayRequest.getComponents().forEach(el -> componentService.fillCycledField(el, scenarioDto));
            displayRequest.getInfoComponents().forEach(el -> componentService.fillCycledField(el, scenarioDto));
            displayRequest.getLogicAfterValidationComponents().forEach(el -> componentService.fillCycledField(el, scenarioDto));
            displayReferenceService.processDisplayRefs(displayRequest, scenarioDto);
            scenarioDto.setDisplay(displayRequest);
            computeAnswerService.computeValues(screenToProcess, scenarioDto);
            scenarioDto.setLogicComponents(componentService.getLogicFields(screenToProcess, scenarioDto, currentDescriptor));
        }
        return needSaveDraft;
    }

    public ScenarioDto createInitScenario(ServiceDescriptor currentDescriptor, InitServiceDto initServiceDto) {
        ScenarioDto scenarioDto = new ScenarioDto();
        scenarioDto.setServiceInfo(initServiceDto.getServiceInfo());
        if (currentDescriptor != null) {
            scenarioDto.setCurrentScenarioId(1L);
            scenarioDto.getCachedAnswers().clear();
            scenarioDto.setCurrentValue(new HashMap<>());
            scenarioDto.setApplicantAnswers(new HashMap<>());
            scenarioDto.setGepsId(initServiceDto.getGepsId());
            if (StringUtils.hasText(initServiceDto.getOrderId())) {
                scenarioDto.setOrderId(Long.valueOf(initServiceDto.getOrderId()));
            }

            String foundInitScreenId = findInitScreenId(currentDescriptor, DEFAULT_STAGE, DEFAULT_ROLE, scenarioDto);
            currentDescriptor.getScreens().stream().filter(screen -> screen.getId().equals(foundInitScreenId)).forEach(screen -> {
                ScreenHelper screenHelper = screenRegistry.getHelper(screen.getType());
                if (Objects.nonNull(screenHelper)) {
                    screen = screenHelper.processScreen(screen, scenarioDto);
                }
                DisplayRequest displayRequest = new DisplayRequest(screen, componentService.getScreenFields(screen, scenarioDto, currentDescriptor));
                displayReferenceService.processDisplayRefs(displayRequest, scenarioDto);
                scenarioDto.setDisplay(displayRequest);
                scenarioDto.setLogicComponents(componentService.getLogicFields(screen, scenarioDto, currentDescriptor));
                if (Objects.nonNull(currentDescriptor.getParameters())) {
                    scenarioDto.getServiceParameters().putAll(currentDescriptor.getParameters());
                }
                // Для json2 надо добавлять первый экран
                scenarioDto.getFinishedAndCurrentScreens().add(displayRequest.getId());
                computeAnswerService.computeValues(screen, scenarioDto);
            });
        }
        return scenarioDto;
    }

    public ScenarioDto prepareScreenById(ServiceDescriptor serviceDescriptor, String screenId, ScenarioDto scenarioDto){
        if (serviceDescriptor != null && scenarioDto != null) {
            scenarioDto.setCurrentScenarioId(1L);
            scenarioDto.getCachedAnswers().clear();
            scenarioDto.setCurrentValue(new HashMap<>());
            serviceDescriptor.getScreens().stream().filter(screen -> screen.getId().equals(screenId)).forEach(screen -> {
                ScreenHelper screenHelper = screenRegistry.getHelper(screen.getType());
                if (Objects.nonNull(screenHelper)) {
                    screen = screenHelper.processScreen(screen, scenarioDto);
                }
                DisplayRequest displayRequest = new DisplayRequest(screen, componentService.getScreenFields(screen, scenarioDto, serviceDescriptor));
                if (serviceDescriptor.getExternalScreenIds().contains(screenId)) {
                    displayRequest.setHideBackButton(true);
                }

                scenarioDto.setDisplay(displayRequest);
                scenarioDto.setLogicComponents(componentService.getLogicFields(screen, scenarioDto, serviceDescriptor));
                if (Objects.nonNull(serviceDescriptor.getParameters())) {
                    scenarioDto.getServiceParameters().putAll(serviceDescriptor.getParameters());
                }
                // Для json2 надо добавлять первый экран
                scenarioDto.getFinishedAndCurrentScreens().add(displayRequest.getId());
                computeAnswerService.computeValues(screen, scenarioDto);
            });
        }
        return scenarioDto;
    }

    public String findInitScreenId(ServiceDescriptor currentDescriptor, String fullStage, ApplicantRole applicantRole, ScenarioDto scenarioDto) {
        String role = applicantRole.name();
        var initScreensMap = Optional.ofNullable(currentDescriptor.getInitScreens()).orElse(new HashMap<>());

        // поиск начального экрана в init, если не найден в секции initScreens
        if (!initScreensMap.containsKey(role)) {
            return findFallbackInitScreenId(currentDescriptor);
        }

        // стейджи для роли
        var stageScreensMap = initScreensMap.get(role);

        // Настройки экрана для полного статуса или для стейджа без статуса заполнения
        Object initScreenSettings = Optional.ofNullable(stageScreensMap.get(fullStage))
                .orElse(stageScreensMap.get(getDefaultStage(fullStage)));

        // Ищем экран, если не найден, то ищем в Default стейдже
        String screenId = findScreenId(initScreenSettings, scenarioDto, currentDescriptor);
        if (StringUtils.isEmpty(screenId)) {
            return findScreenId(stageScreensMap.get("Default"), scenarioDto, currentDescriptor);
        }
        return screenId;
    }

    /**
     * Ищет начальный экран в атрибуте init
     * @param currentDescriptor описание услуги
     * @return id начального экрана в атрибуте init
     */
    private String findFallbackInitScreenId(ServiceDescriptor currentDescriptor) {
        var initScreenId = currentDescriptor.getInit();
        if(Strings.isBlank(initScreenId)){
            throw new EntityNotFoundException("No init screen found");
        }
        return initScreenId;
    }

    /**
     * Находит экран по маппингу или по бизнес-правилам
     * @param initScreenSettings настройки экрано
     * @param scenarioDto DTO
     * @param serviceDescriptor описание услуги
     * @return идентификатор начального экрана
     */
    private String findScreenId(Object initScreenSettings, ScenarioDto scenarioDto, ServiceDescriptor serviceDescriptor) {
        // Поддержка старого формата
        if(initScreenSettings instanceof String) {
            return (String)initScreenSettings;
        }

        // Поиск экрана по бизнес-правилам
        if(initScreenSettings instanceof List) {
            return getScreenByScreenRules(initScreenSettings, scenarioDto, serviceDescriptor);
        }

        return null;
    }

    private String getScreenByScreenRules(Object initScreenSettings, ScenarioDto scenarioDto, ServiceDescriptor serviceDescriptor) {
        List<Map> screenConditions = (List<Map>) initScreenSettings;
        List<ScreenRule> rules = screenConditions.stream().map(map -> {
            String screenRuleJson = jsonProcessingService.toJson(map);
            return jsonProcessingService.fromJson(screenRuleJson, ScreenRule.class);
        }).collect(Collectors.toList());

        return screenFinderService.findScreenDescriptorByRules(scenarioDto, serviceDescriptor, rules).getId();
    }

    private String getDefaultStage(String fullStage) {
        if (hasText(fullStage) && fullStage.contains("_")) {
            return fullStage.substring(0, fullStage.indexOf("_"));
        }
        return DEFAULT_STAGE;
    }

    protected JsonProcessingService getJsonProcessingService() {
        return jsonProcessingService;
    }
}
