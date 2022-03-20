package ru.gosuslugi.pgu.fs.service.process.impl.screen;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.web.context.WebApplicationContext;
import ru.gosuslugi.pgu.common.esia.search.dto.UserPersonalData;
import ru.gosuslugi.pgu.core.lk.model.order.Order;
import ru.gosuslugi.pgu.dto.ApplicantAnswer;
import ru.gosuslugi.pgu.dto.DisplayRequest;
import ru.gosuslugi.pgu.dto.ScenarioDto;
import ru.gosuslugi.pgu.dto.ScenarioResponse;
import ru.gosuslugi.pgu.dto.descriptor.FieldComponent;
import ru.gosuslugi.pgu.dto.descriptor.ScreenDescriptor;
import ru.gosuslugi.pgu.dto.descriptor.ServiceDescriptor;
import ru.gosuslugi.pgu.dto.descriptor.types.OrderBehaviourType;
import ru.gosuslugi.pgu.fs.common.descriptor.MainDescriptorService;
import ru.gosuslugi.pgu.fs.common.helper.HelperScreenRegistry;
import ru.gosuslugi.pgu.fs.common.helper.ScreenHelper;
import ru.gosuslugi.pgu.fs.common.service.AnswerValidationService;
import ru.gosuslugi.pgu.fs.common.service.ComponentService;
import ru.gosuslugi.pgu.fs.common.service.ComputeAnswerService;
import ru.gosuslugi.pgu.fs.common.service.CycledScreenService;
import ru.gosuslugi.pgu.fs.common.service.DisplayReferenceService;
import ru.gosuslugi.pgu.fs.common.service.ScreenFinderService;
import ru.gosuslugi.pgu.fs.descriptor.ErrorModalDescriptorService;
import ru.gosuslugi.pgu.fs.descriptor.ErrorModalView;
import ru.gosuslugi.pgu.fs.exception.DraftNotEditableException;
import ru.gosuslugi.pgu.fs.exception.DuplicateOrderException;
import ru.gosuslugi.pgu.fs.exception.NoEmpowermentException;
import ru.gosuslugi.pgu.fs.pgu.client.impl.OrderStatuses;
import ru.gosuslugi.pgu.fs.pgu.service.PguOrderService;
import ru.gosuslugi.pgu.fs.service.CreateOrderService;
import ru.gosuslugi.pgu.fs.service.EmpowermentService;
import ru.gosuslugi.pgu.fs.service.IntegrationService;
import ru.gosuslugi.pgu.fs.service.impl.AdditionalAttributesHelper;
import ru.gosuslugi.pgu.fs.service.impl.FormScenarioDtoServiceImpl;
import ru.gosuslugi.pgu.fs.service.process.NextScreenProcess;
import ru.gosuslugi.pgu.fs.suggests.service.SuggestsService;
import ru.gosuslugi.pgu.fs.utils.OrderBehaviourTypeUtil;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Predicate;

/**
 * Описание этапов процесса перехода на следующий экран
 */
@Slf4j
@Component
@Scope(value = WebApplicationContext.SCOPE_REQUEST, proxyMode = ScopedProxyMode.TARGET_CLASS)
@RequiredArgsConstructor
public class NextScreenProcessImpl extends AbstractScreenProcess<NextScreenProcess> implements NextScreenProcess {

    @Getter
    private final MainDescriptorService mainDescriptorService;
    private final AnswerValidationService answerValidationService;
    private final ComponentService componentService;
    private final CreateOrderService createOrderService;
    private final CycledScreenService cycledScreenService;
    private final ScreenFinderService screenFinderService;
    private final DisplayReferenceService displayReferenceService;
    private final HelperScreenRegistry screenRegistry;
    private final UserPersonalData userPersonalData;
    private final PguOrderService pguOrderService;
    private final FormScenarioDtoServiceImpl scenarioDtoService;
    private final IntegrationService integrationService;
    private final ComputeAnswerService computeAnswerService;
    private final AdditionalAttributesHelper additionalAttributesHelper;
    private final SuggestsService suggestsService;
    private final EmpowermentService empowermentService;
    private final ErrorModalDescriptorService errorModalDescriptorService;

    @Override
    public void buildResponse() {
        log.info("Starting to build scenario response" + formatScenarioLogFields());

        ScenarioDto scenarioDto = request.getScenarioDto();
        response = new ScenarioResponse();
        response.setScenarioDto(scenarioDto);
    }

    @Override
    public void tryToCreateOrderId() {
        log.info("Trying to create order" + formatScenarioLogFields());

        ScenarioDto scenarioDto = response.getScenarioDto();
        response.getScenarioDto().setOrderId(createOrderService.tryToCreateOrderId(serviceId, scenarioDto, serviceDescriptor));
    }

    @Override
    public void removeOldAnswers() {
        log.info("Removing old answers" + formatScenarioLogFields());

        response.getScenarioDto().getCurrentValue().keySet().forEach(currentKey-> {
            response.getScenarioDto().getApplicantAnswers().remove(currentKey);
            response.getScenarioDto().getCachedAnswers().remove(currentKey);
        });
    }

    @Override
    public void removeDisclaimers() {
        log.info("Removing disclaimers" + formatScenarioLogFields());

        response.getScenarioDto().getDisclaimers().clear();
    }

    @Override
    public void clearValidateErrors() {
        log.info("Clearing validation errors" + formatScenarioLogFields());

        response.getScenarioDto().getErrors().clear();
        response.getScenarioDto().getUniquenessErrors().clear();
    }

    @Override
    public void validate() {
        log.info("Validating request" + formatScenarioLogFields());

        ConcurrentHashMap<String, String> validationErrors = answerValidationService.validateAnswers(response.getScenarioDto(), serviceDescriptor);
        if (!validationErrors.isEmpty()) {
            response.getScenarioDto().setErrors(validationErrors);
            response.getScenarioDto().addToCachedAnswers(response.getScenarioDto().getCurrentValue());
        }
        List<List<Map<String, String>>> uniquenessValidationErrors = answerValidationService.validateItemsUniqueness(response.getScenarioDto(), serviceDescriptor);
        if (uniquenessValidationErrors.stream().anyMatch(list -> !list.isEmpty())) {
            response.getScenarioDto().setUniquenessErrors(uniquenessValidationErrors);
            response.getScenarioDto().addToCachedAnswers(response.getScenarioDto().getCurrentValue());
        }
    }

    @Override
    public boolean hasValidateErrors() {
        return !response.getScenarioDto().getErrors().isEmpty() || !response.getScenarioDto().getUniquenessErrors().isEmpty();
    }

    @Override
    public void preloadComponents() {
        log.info("Preloading components" + formatScenarioLogFields());

        ScenarioDto scenarioDto = request.getScenarioDto();
        componentService.preloadComponents(scenarioDto.getDisplay().getId(), scenarioDto, serviceDescriptor);
    }

    @Override
    public boolean replaceResponseForCycledScreen() {
        ScenarioResponse scenarioResponse = cycledScreenService.getNextScreen(request, serviceId);
        if (scenarioResponse != null) {
            response = scenarioResponse;
            return true;
        }

        return false;
    }

    @Override
    public void calculateNextScreen() {
        log.info("Calculating next screen" + formatScenarioLogFields());

        ScenarioDto scenarioDto = response.getScenarioDto();
        ScreenDescriptor screenDescriptor = screenFinderService.findScreenDescriptorByRules(scenarioDto, serviceDescriptor, null);
        if (screenDescriptor == null) {
            response = null;
            isFinished = true;
            return;
        }

        if (log.isDebugEnabled()) log.debug("Screen descriptor {}", screenDescriptor);
        ScreenHelper screenHelper = screenRegistry.getHelper(screenDescriptor.getType());
        if (Objects.nonNull(screenHelper)) {
            screenDescriptor = screenHelper.processScreen(screenDescriptor, scenarioDto);
        }

        computeAnswerService.computeValues(screenDescriptor, scenarioDto);

        scenarioDto.getFinishedAndCurrentScreens().add(screenDescriptor.getId());
        scenarioDto.getApplicantAnswers().putAll(scenarioDto.getCurrentValue());
        scenarioDto.setCurrentValue(new HashMap<>());
        DisplayRequest displayRequest = new DisplayRequest(screenDescriptor, componentService.getScreenFields(screenDescriptor, scenarioDto, serviceDescriptor));
        displayReferenceService.processDisplayRefs(displayRequest, scenarioDto);
        scenarioDto.setDisplay(displayRequest);
        scenarioDto.setLogicComponents(componentService.getLogicFields(screenDescriptor, scenarioDto, serviceDescriptor));

        log.info("Next screen calculated: {}" + formatScenarioLogFields(), displayRequest.getId());
    }

    @Override
    public void prepareDisplayAfterValidationErrors() {
        log.info("Preparing to display validation errors" + formatScenarioLogFields());

        ScenarioDto scenarioDto = response.getScenarioDto();
        Optional<ScreenDescriptor> screenDescriptorOptional = serviceDescriptor.getScreenDescriptorById(scenarioDto.getDisplay().getId());
        if (screenDescriptorOptional.isEmpty()) {
            return;
        }
        ScreenDescriptor screenDescriptor = screenDescriptorOptional.get();
        if (log.isDebugEnabled()) log.debug("Screen descriptor {}", screenDescriptor);
        ScreenHelper screenHelper = screenRegistry.getHelper(screenDescriptor.getType());
        if (Objects.nonNull(screenHelper)) {
            screenDescriptor = screenHelper.processScreen(screenDescriptor, scenarioDto);
        }
        DisplayRequest displayRequest = new DisplayRequest(screenDescriptor, componentService.getScreenFields(screenDescriptor, scenarioDto, serviceDescriptor));
        displayReferenceService.processDisplayRefs(displayRequest, scenarioDto);
        scenarioDto.setDisplay(displayRequest);

        log.info("Screen to display validation errors: {}" + formatScenarioLogFields(), displayRequest.getId());
    }

    @Override
    public void fillUserData() {
        log.info("Filling user data" + formatScenarioLogFields());
        additionalAttributesHelper.fillUserData(response.getScenarioDto());
    }

    @Override
    public boolean isTerminalAndImpasse() {
        DisplayRequest displayRequest = response.getScenarioDto().getDisplay();
        return displayRequest.isTerminal() && displayRequest.isImpasse();
    }

    public boolean isTerminalAndForceSendToSuggest(){
        DisplayRequest displayRequest = response.getScenarioDto().getDisplay();
        return displayRequest.isTerminal() && displayRequest.isForceSendToSuggestions();
    }

    @Override
    public boolean isNeedToUpdateAdditionalParameters() {
        DisplayRequest displayRequest = response.getScenarioDto().getDisplay();
        return displayRequest.isNeedToUpdateAdditionalParameters() || (displayRequest.isTerminal() && !displayRequest.isNotSendToSp());
    }

    @Override
    public boolean isTerminalAndNotImpasse() {
        DisplayRequest displayRequest = response.getScenarioDto().getDisplay();
        return displayRequest.isTerminal() && !displayRequest.isImpasse();
    }

    @Override
    public void deleteOrder() {
        log.info("Deleting order and draft" + formatScenarioLogFields());
        pguOrderService.deleteOrderById(response.getScenarioDto().getOrderId());
        draftClient.deleteDraft(response.getScenarioDto().getOrderId(), userPersonalData.getUserId());
    }

    @Override
    public boolean hasOrderCreateCustomParameter() {
        return response.getScenarioDto().getDisplay().isTerminal() && serviceDescriptor.hasOrderCreateCustomParameter();
    }

    @Override
    public boolean orderShouldExists(){
        var descriptor = getMainDescriptorService().getServiceDescriptor(serviceId);
        return !OrderBehaviourType.NO_ORDER.equals(descriptor.getOrderBehaviourType());
    }

    @Override
    public boolean draftShouldExist() {
        var descriptor = getMainDescriptorService().getServiceDescriptor(serviceId);

        /*
         * Если сохранение черновика выставлено в true - всегда сохраняем.
         */
        if(OrderBehaviourType.ORDER_AND_DRAFT.equals(descriptor.getOrderBehaviourType())){
            return true;
        }

        Order order = pguOrderService.findOrderByIdCached(response.getScenarioDto().getOrderId());
        if (OrderBehaviourTypeUtil.getSmevOrderDraftFlag(descriptor, order, false)) {
            return true;
        }

        /*
         * Если экран терминальный и мы должны выполнить отправку в SP
         * И выставлена стратегия - сохранять перед отправкой
         * То надо сохранить черновик
         */
        return this.response.getScenarioDto().getDisplay().isTerminal() &&
                !this.response.getScenarioDto().getDisplay().isNotSendToSp() &&
                OrderBehaviourType.ORDER_AND_DRAFT_BEFORE_SEND.equals(descriptor.getOrderBehaviourType());
    }

    @Override
    public void saveChosenValuesForOrder() {
        if (response.getScenarioDto().getOrderId() == null) {
            throw new DuplicateOrderException("Экран терминальный, но orderId не был создан");
        }
        log.info("Saving chosen values for order" + formatScenarioLogFields());
        if (!createOrderService.saveValuesForOrder(serviceDescriptor, response.getScenarioDto())) {
            throw new DuplicateOrderException("Ошибка при сохранении выбранных значений для заявления");
        }
    }

    @Override
    public void updateAdditionalAttributes() {
        log.info("Updating additional attributes" + formatScenarioLogFields());
        scenarioDtoService.updateAdditionalAttributes(response.getScenarioDto(), serviceDescriptor.getSmevEnv());
    }

    @Override
    public void performIntegrationSteps() {
        log.info("Performing integration steps" + formatScenarioLogFields());
        integrationService.performIntegrationSteps(response, serviceId, serviceDescriptor);
    }

    @Override
    public NextScreenProcessImpl getProcess() {
        return this;
    }

    @Override
    public void mergePdfDocuments() {
        log.info("Merging PDF documents" + formatScenarioLogFields());
        scenarioDtoService.mergePdfDocuments(response.getScenarioDto(), serviceDescriptor);
    }

    @Override
    public void clearCacheForComponents() {
        log.info("Clearing component cache" + formatScenarioLogFields());
        var scenario = request.getScenarioDto();
        scenario.getCurrentValue().entrySet().stream()
                .filter(entry -> {
                    if (scenario.getCachedAnswers().containsKey(entry.getKey())) {
                        return !String.valueOf(scenario.getCachedAnswers().get(entry.getKey()).getValue()).equals(String.valueOf(entry.getValue()));
                    }
                    if (scenario.getApplicantAnswers().containsKey(entry.getKey())) {
                        return !String.valueOf(scenario.getApplicantAnswers().get(entry.getKey()).getValue()).equals(String.valueOf(entry.getValue()));
                    }
                    return false;
                })
                .map(Map.Entry::getKey).forEach(componentId -> {
                    var displayComponentOptional = scenario.getDisplay().getComponents().stream()
                            .filter(v -> v.getId().equals(componentId))
                            .findAny();
                    displayComponentOptional
                            .ifPresent(component -> serviceDescriptor.getFieldComponentById(component.getId()).get().getClearCacheForComponentIds()
                                    .forEach(componentIdToClearCache ->
                                            scenario.getCachedAnswers().remove(componentIdToClearCache)
                                    ));
                }
        );
    }

    @Override
    public boolean hasCheckForDuplicate() {
        Optional<FieldComponent> component = findComponentForPredicate(response.getScenarioDto(), serviceDescriptor, FieldComponent::getCheckForDuplicate);
        return component.isPresent();
    }

    @Override
    public void checkForDuplicate() {
        log.info("Checking for order duplicate" + formatScenarioLogFields());
        ScenarioDto scenarioDto = response.getScenarioDto();
        Optional<FieldComponent> fieldComponent = findComponentForPredicate(scenarioDto, serviceDescriptor, FieldComponent::getCheckForDuplicate);
        createOrderService.checkForDuplicate(scenarioDto, fieldComponent, serviceDescriptor);
    }

    @Override
    public void forceSendToSuggestion() {
        suggestsService.send(this.userPersonalData.getUserId(),this.response.getScenarioDto());
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

    @Override
    public void checkPermissions() {
        log.info("Checking permissions" + formatScenarioLogFields());

        var scenarioDto = this.response.getScenarioDto();
        var display = scenarioDto.getDisplay();

        if (!display.isTerminal() || display.isNotSendToSp()) {
            return;
        }
        boolean notDeliriumScenario = serviceDescriptor.checkOnlyOneApplicantAndStage() && CollectionUtils.isEmpty(scenarioDto.getParticipants());
        if (!notDeliriumScenario || display.isForceDeliriumCall()) {
            return;
        }
        var empowerments = serviceDescriptor.getEmpowerments();
        if (!empowerments.isEmpty()) {
            if (!empowermentService.hasEmpowerment(empowerments)) {
                throw new NoEmpowermentException(
                        errorModalDescriptorService.getErrorModal(ErrorModalView.NO_RIGHTS_FOR_SENDING_APPLICATION),
                        "Нет доверенности для отправки заявления, обратитесь к руководителю");
            }
            return;
        }
        if (!empowermentService.checkUserUserPermissionForSendOrder(scenarioDto.getTargetCode())) {
            throw new NoEmpowermentException(errorModalDescriptorService.getErrorModal(ErrorModalView.NO_RIGHTS_FOR_SENDING_APPLICATION),
                    "Недостаточно прав для подачи заявления");
        }

    }
}
