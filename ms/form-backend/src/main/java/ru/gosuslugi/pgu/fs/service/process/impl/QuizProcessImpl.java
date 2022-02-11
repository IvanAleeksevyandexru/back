package ru.gosuslugi.pgu.fs.service.process.impl;

import lombok.RequiredArgsConstructor;
import org.apache.logging.log4j.util.Strings;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.stereotype.Component;
import org.springframework.web.context.WebApplicationContext;
import ru.gosuslugi.pgu.components.descriptor.types.Stage;
import ru.gosuslugi.pgu.dto.ApplicantAnswer;
import ru.gosuslugi.pgu.dto.ApplicantRole;
import ru.gosuslugi.pgu.dto.QuizRequest;
import ru.gosuslugi.pgu.dto.QuizResponse;
import ru.gosuslugi.pgu.dto.ScenarioRequest;
import ru.gosuslugi.pgu.dto.ScenarioResponse;
import ru.gosuslugi.pgu.dto.descriptor.ScreenDescriptor;
import ru.gosuslugi.pgu.dto.descriptor.types.OrderType;
import ru.gosuslugi.pgu.fs.common.descriptor.MainDescriptorService;
import ru.gosuslugi.pgu.fs.pgu.service.CatalogService;
import ru.gosuslugi.pgu.fs.pgu.service.PguOrderService;
import ru.gosuslugi.pgu.fs.service.custom.MainScreenServiceRegistry;
import ru.gosuslugi.pgu.fs.service.process.QuizProcess;

import java.util.LinkedList;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import static ru.gosuslugi.pgu.components.regex.RegExpContext.getValueByRegex;


/**
 * Процесс, позволяющий работать с переходом из квиза в основные сценарии
 *
 */
@Component
@Scope(value = WebApplicationContext.SCOPE_REQUEST, proxyMode = ScopedProxyMode.TARGET_CLASS)
@RequiredArgsConstructor
public class QuizProcessImpl implements QuizProcess {

    private QuizRequest qr;

    private QuizResponse qs = new QuizResponse();

    private final PguOrderService pguOrderService;

    private final MainScreenServiceRegistry mainScreenServiceRegistry;

    private final MainDescriptorService mainDescriptorService;

    private final CatalogService catalogService;


    public QuizProcessImpl findAndRemoveOldOrderIfExists() {
        var order = pguOrderService.findLastOrder(qr.getServiceId(),qr.getTargetId());
        if(Objects.nonNull(order)){
            pguOrderService.deleteOrderById(order.getId());
        }
        return this;
    }

    public QuizProcessImpl createNewOrder() {
        var sd = mainDescriptorService.getServiceDescriptor(qr.getServiceId());
        var orderId = pguOrderService.createOrderId(qr.getServiceId(),qr.getTargetId(), OrderType.ORDER, sd.getHighloadParameters());
        qr.getScenarioDto().setOrderId(orderId);
        qr.getScenarioDto().setServiceCode(qr.getServiceId());
        qr.getScenarioDto().setTargetCode("-".concat(qr.getServiceId()));
        qr.getScenarioDto().setServiceDescriptorId(qr.getServiceId());
        qr.getScenarioDto().setServiceId(qr.getServiceId());
        return this;
    }

    @Override
    public QuizProcess convertToNewDisplayId() {
        var oldId = qr.getScenarioDto().getDisplay().getId();
        qr.getScenarioDto().getDisplay().setId(oldId.replace(getAnswerServicePrefix(qr.getServiceId()), Strings.EMPTY));
        return this;
    }

    public QuizProcessImpl convertToNewAnswers() {
        var newApplicantAnswer = qr.getScenarioDto()
                .getApplicantAnswers()
                .entrySet()
                .stream()
                .collect(Collectors.toMap(
                        key-> getValueByRegex(getAnswerServicePrefix(qr.getServiceId()), pattern -> pattern.matcher(key.getKey()).replaceFirst(Strings.EMPTY)),
                        Map.Entry::getValue
                ));
        qr.getScenarioDto().setApplicantAnswers(newApplicantAnswer);
        return this;
    }

    public QuizProcessImpl setInitScreen() {
        var serviceDescriptor = mainDescriptorService.getServiceDescriptor(qr.getServiceId());
        var initScreen = serviceDescriptor.getInit();
        if(serviceDescriptor.getInitScreens()!=null && !serviceDescriptor.getInitScreens().isEmpty()){
            var iniScreens = serviceDescriptor.getInitScreens().get(ApplicantRole.Applicant);
            if(iniScreens!=null && !iniScreens.isEmpty()) {
                if(iniScreens.get(Stage.Applicant)!=null && !iniScreens.get(Stage.Applicant).toString().isEmpty()) {
                    initScreen = iniScreens.get(Stage.Applicant).toString();
                }
            }
        }
        qr.getScenarioDto().getFinishedAndCurrentScreens().removeFirst();
        qr.getScenarioDto().getFinishedAndCurrentScreens().addFirst(initScreen);
        for(ScreenDescriptor screen: serviceDescriptor.getScreens()) {
            if(screen.getId().equals(initScreen)) {
                screen.getComponentIds().stream().forEach(s -> {
                    var answer = new ApplicantAnswer();
                    answer.setVisited(true);
                    answer.setValue("");
                    qr.getScenarioDto().getApplicantAnswers().put(s, answer);
                });
                break;
            }
        }
        return this;
    }

    public QuizProcessImpl convertToNewCachedAnswers() {
        var newApplicantAnswer = qr.getScenarioDto()
                .getCachedAnswers()
                .entrySet()
                .stream()
                .collect(Collectors.toMap(
                        key-> getValueByRegex(getAnswerServicePrefix(qr.getServiceId()), pattern -> pattern.matcher(key.getKey()).replaceFirst(Strings.EMPTY)),
                        Map.Entry::getValue
                ));
        qr.getScenarioDto().setCachedAnswers(newApplicantAnswer);
        return this;
    }

    public QuizProcessImpl convertToNewFinishedScreens(){
        var newFinishedScreens = qr.getScenarioDto()
                .getFinishedAndCurrentScreens()
                .stream()
                .map(v -> getValueByRegex(getAnswerServicePrefix(qr.getServiceId()), pattern -> pattern.matcher(v).replaceFirst(Strings.EMPTY)))
                .collect(Collectors.toCollection(LinkedList::new));
        qr.getScenarioDto().setFinishedAndCurrentScreens(newFinishedScreens);
        return this;
    }

    public QuizProcessImpl calculateNextStep(){
        ScenarioRequest scenarioRequest = new ScenarioRequest();
        scenarioRequest.setScenarioDto(qr.getScenarioDto());
        ScenarioResponse scenarioResponse = mainScreenServiceRegistry.getService(qr.getTargetId())
                .getNextScreen(scenarioRequest,qr.getServiceId());
        qr.setScenarioDto(scenarioResponse.getScenarioDto());
        return this;
    }

    public QuizProcessImpl of(QuizRequest q) {
        this.qr = q;
        return this;
    }

    public QuizResponse finish() {
        qs.setScenarioDto(qr.getScenarioDto());
        return qs;
    }

    @Override
    public QuizProcess enrichWithCatalogInfo() {
        var catalogInfo = catalogService.getCatalogInfoByTargetId(qr.getTargetId());
        qs.setCatalogInfo(catalogInfo);
        return this;
    }

    private String getAnswerServicePrefix(String serviceId){
        return mainDescriptorService.getServiceDescriptor(serviceId).getAnswerServicePrefix();
    }
}
