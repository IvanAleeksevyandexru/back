package ru.gosuslugi.pgu.fs.service.process.impl.screen;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.stereotype.Component;
import org.springframework.web.context.WebApplicationContext;
import ru.gosuslugi.pgu.fs.common.exception.FormBaseException;
import ru.gosuslugi.pgu.common.esia.search.dto.UserPersonalData;
import ru.gosuslugi.pgu.draft.DraftClient;
import ru.gosuslugi.pgu.dto.*;
import ru.gosuslugi.pgu.dto.descriptor.types.OrderBehaviourType;
import ru.gosuslugi.pgu.fs.common.descriptor.MainDescriptorService;
import ru.gosuslugi.pgu.fs.common.service.ScenarioDtoService;
import ru.gosuslugi.pgu.fs.pgu.service.PguOrderService;
import ru.gosuslugi.pgu.fs.service.CreateOrderService;
import ru.gosuslugi.pgu.fs.service.process.ExternalScreenProcess;
import ru.gosuslugi.pgu.fs.service.process.impl.AbstractProcess;

import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Component
@Scope(value = WebApplicationContext.SCOPE_REQUEST, proxyMode = ScopedProxyMode.TARGET_CLASS)
@RequiredArgsConstructor
public class ExternalScreenProcessImpl extends AbstractProcess<ExternalScreenProcessImpl, ScenarioResponse>
        implements ExternalScreenProcess {

    private ScenarioFromExternal request;
    private final MainDescriptorService mainDescriptorService;
    private final UserPersonalData userPersonalData;
    private final CreateOrderService createOrderService;
    private final DraftClient draftClient;
    private final ScenarioDtoService scenarioDtoService;
    private final PguOrderService pguOrderService;

    @Override
    public ExternalScreenProcessImpl getProcess() {
        return this;
    }
    @Override
    public ExternalScreenProcess of(ScenarioFromExternal scenarioFromExternal) {
        return of(scenarioFromExternal, false);
    }

    @Override
    public ExternalScreenProcess of(ScenarioFromExternal scenarioFromExternal, boolean deleteOrderOnPguException) {
        this.request = scenarioFromExternal;
        this.response = new ScenarioResponse();
        this.response.setScenarioDto(new ScenarioDto());
        this.response.getScenarioDto().getFinishedAndCurrentScreens().add(request.getScreenId());
        if (deleteOrderOnPguException) {
            this.onPguErrorCallback = (response) -> {
                if (response.getScenarioDto() != null && response.getScenarioDto().getOrderId() != null) {
                    pguOrderService.deleteOrderById(response.getScenarioDto().getOrderId());
                }
            };
        }
        return this;
    }

    @Override
    public void prepareApplicantAnswer() {
        var answers = this.request.getExternalApplicantAnswers().entrySet().stream().map(entry -> {
            var applicantAnswer = new ApplicantAnswer();
            applicantAnswer.setValue(entry.getValue());
            applicantAnswer.setVisited(true);
            return Map.entry(entry.getKey(),applicantAnswer);
        }).collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
        this.response.getScenarioDto().setApplicantAnswers(answers);
    }

    @Override
    public void prepareDisplay() {
        var serviceDescriptor = mainDescriptorService.getServiceDescriptor(this.request.getServiceId());
        serviceDescriptor
                .getScreens()
                .stream()
                .filter(e-> e.getId().equals(this.request.getScreenId()))
                .findFirst()
                .orElseThrow(()-> new FormBaseException("Экран "+ this.request.getScreenId() + " не найден"));
        var scenario = scenarioDtoService.prepareScreenById(serviceDescriptor,this.request.getScreenId(), response.getScenarioDto());
        this.response.setScenarioDto(scenario);
    }

    @Override
    public void createOrder() {
        var scenarioDto = this.response.getScenarioDto();
        var serviceDescriptor = mainDescriptorService.getServiceDescriptor(this.request.getServiceId());
        this.response.getScenarioDto().setTargetCode(this.request.getTargetId());
        this.response.getScenarioDto().setServiceCode(this.request.getServiceId());
        if(Arrays.asList(OrderBehaviourType.ONLY_ORDER,OrderBehaviourType.ORDER_AND_DRAFT).contains(serviceDescriptor.getOrderBehaviourType()) ){
            Long orderId = createOrderService.tryToCreateOrderId(this.request.getServiceId(),this.response.getScenarioDto(),serviceDescriptor);
            scenarioDto.setOrderId(orderId);
        }
    }

    @Override
    public void saveDraft() {
        var serviceDescriptor = mainDescriptorService.getServiceDescriptor(this.request.getServiceId());
        if(Arrays.asList(OrderBehaviourType.ONLY_ORDER,OrderBehaviourType.ORDER_AND_DRAFT).contains(serviceDescriptor.getOrderBehaviourType())) {
            draftClient.saveDraft(this.response.getScenarioDto(), this.request.getServiceId(), userPersonalData.getUserId(), userPersonalData.getOrgId(), serviceDescriptor.getDraftTtl(), serviceDescriptor.getOrderTtl());
        }
    }
}
