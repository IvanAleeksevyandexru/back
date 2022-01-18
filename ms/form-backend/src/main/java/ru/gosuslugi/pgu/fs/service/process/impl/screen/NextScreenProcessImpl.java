package ru.gosuslugi.pgu.fs.service.process.impl.screen;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.web.context.WebApplicationContext;
import ru.gosuslugi.pgu.common.esia.search.dto.UserOrgData;
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
import ru.gosuslugi.pgu.fs.exception.DuplicateOrderException;
import ru.gosuslugi.pgu.fs.exception.NoEmpowermentException;
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
    private final UserOrgData userOrgData;
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
        ScenarioDto scenarioDto = request.getScenarioDto();
        response = new ScenarioResponse();
        response.setScenarioDto(scenarioDto);
    }

    @Override
    public void tryToCreateOrderId() {
        ScenarioDto scenarioDto = response.getScenarioDto();
        response.getScenarioDto().setOrderId(createOrderService.tryToCreateOrderId(serviceId, scenarioDto, serviceDescriptor));
    }

    @Override
    public void removeOldAnswers() {
        response.getScenarioDto().getCurrentValue().keySet().forEach(currentKey-> {
            response.getScenarioDto().getApplicantAnswers().remove(currentKey);
            response.getScenarioDto().getCachedAnswers().remove(currentKey);
        });
    }

    @Override
    public void removeDisclaimers() {
        response.getScenarioDto().getDisclaimers().clear();
    }

    @Override
    public void clearValidateErrors() {
        response.getScenarioDto().getErrors().clear();
        response.getScenarioDto().getUniquenessErrors().clear();
    }

    @Override
    public void validate() {
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
    }

    @Override
    public void prepareDisplayAfterValidationErrors() {
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
    }

    @Override
    public void fillUserData() {
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
        if (!createOrderService.saveValuesForOrder(serviceDescriptor, response.getScenarioDto())) {
            throw new DuplicateOrderException("Ошибка при сохранении выбранных значений для заявления");
        }
    }

    @Override
    public void updateAdditionalAttributes() {
        scenarioDtoService.updateAdditionalAttributes(response.getScenarioDto(), serviceDescriptor.getSmevEnv());
    }

    @Override
    public void performIntegrationSteps() {
        integrationService.performIntegrationSteps(response, serviceId, serviceDescriptor);
    }

    @Override
    public NextScreenProcessImpl getProcess() {
        return this;
    }

    @Override
    public void mergePdfDocuments() {
        scenarioDtoService.mergePdfDocuments(response.getScenarioDto(), serviceDescriptor);
    }

    @Override
    public void clearCacheForComponents() {
        request.getScenarioDto().getCurrentValue().entrySet()
                .stream()
                .filter(entry -> {
                    Map<String, ApplicantAnswer> cached = request.getScenarioDto().getCachedAnswers();
                    ApplicantAnswer cachedValue = cached.get(entry.getKey());
                    if (Objects.nonNull(cachedValue)) {
                        return !String.valueOf(cachedValue).equals(String.valueOf(entry.getValue()));
                    }
                    return false;
                })
                .map(Map.Entry::getKey).forEach(componentId -> {
                    var displayComponentOptional = request.getScenarioDto().getDisplay().getComponents().stream().filter(v -> v.getId().equals(componentId)).findAny();
                    displayComponentOptional
                            .ifPresent(component -> serviceDescriptor.getFieldComponentById(component.getId()).get().getClearCacheForComponentIds()
                                    .forEach(componentIdToClearCache -> {
                                        request.getScenarioDto().getCachedAnswers().remove(componentIdToClearCache);
                    }));
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
    public void checkPermissions() {
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
