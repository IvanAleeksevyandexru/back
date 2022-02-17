package ru.gosuslugi.pgu.fs.service.process.impl.screen;

import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.stereotype.Service;
import org.springframework.web.context.WebApplicationContext;
import ru.gosuslugi.pgu.common.esia.search.dto.UserPersonalData;
import ru.gosuslugi.pgu.draft.DraftClient;
import ru.gosuslugi.pgu.dto.DisplayRequest;
import ru.gosuslugi.pgu.dto.InitServiceDto;
import ru.gosuslugi.pgu.dto.ScenarioDto;
import ru.gosuslugi.pgu.dto.ScenarioRequest;
import ru.gosuslugi.pgu.dto.ScenarioResponse;
import ru.gosuslugi.pgu.dto.descriptor.ScreenDescriptor;
import ru.gosuslugi.pgu.dto.descriptor.ServiceDescriptor;
import ru.gosuslugi.pgu.fs.common.descriptor.MainDescriptorService;
import ru.gosuslugi.pgu.fs.service.DeliriumService;
import ru.gosuslugi.pgu.fs.service.ScenarioInitializerService;
import ru.gosuslugi.pgu.fs.service.process.ScreenProcess;
import ru.gosuslugi.pgu.fs.service.process.impl.AbstractProcess;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Процесс перехода между экранами
 * @param <T> Класс задающий этапы выполнения процесса
 */
@Slf4j
@Service
@Scope(value = WebApplicationContext.SCOPE_REQUEST, proxyMode = ScopedProxyMode.TARGET_CLASS)
@Setter
public abstract class AbstractScreenProcess<T> extends AbstractProcess<T, ScenarioResponse> implements ScreenProcess<T> {

    @Autowired
    protected DeliriumService deliriumService;
    @Autowired
    protected ScenarioInitializerService scenarioInitializerService;
    @Autowired
    protected DraftClient draftClient;
    @Autowired
    private UserPersonalData userPersonalData;

    /** Код услуги */
    protected String serviceId;
    /** Запрос */
    protected ScenarioRequest request;
    /** Дескриптор сервиса */
    protected ServiceDescriptor serviceDescriptor;

    /**
     * Инициализация процесса
     * @param serviceId Код услуги
     * @param request   Запрос
     * @return          Процесс
     */
    @Override
    public T of(String serviceId, ScenarioRequest request) {
        init();
        this.serviceId = serviceId;
        this.request = request;
        this.serviceDescriptor = getMainDescriptorService().getServiceDescriptor(serviceId);
        return getProcess();
    }

    abstract protected MainDescriptorService getMainDescriptorService();

    /**
     * Рассчитывает состояние заявки и определяет нужно ли начинать новый сценарий.
     * В случае изменения статуса заявки выполняется заполнение услуги с начального экрана,
     * соответствующего новому статусу
     * @return нужно ли начинать новый сценарий
     */
    @Override
    public boolean needReInitScreen() {
        ScenarioDto scenarioDto = response.getScenarioDto();
        DisplayRequest displayRequest = scenarioDto.getDisplay();
        ServiceDescriptor descriptor = getMainDescriptorService().getServiceDescriptor(serviceId);
        Optional<ScreenDescriptor> optionalScreenDescriptor = descriptor.getScreenDescriptorById(displayRequest.getId());
        if (optionalScreenDescriptor.isEmpty())
            return false;

        ScreenDescriptor screenDescriptor = optionalScreenDescriptor.get();
        boolean isDeliriumScenario = !descriptor.checkOnlyOneApplicantAndStage();

        // Отслеживаем асинхронные изменения стейджей
        List<String> initStages = screenDescriptor.getInitStages();
        if(isDeliriumScenario && !initStages.isEmpty() && initStages.contains(deliriumService.getStage(scenarioDto.getOrderId()).getStage())) {
            return true;
        }

        // Отслеживаем синхронные изменения стейджей
        if(isDeliriumScenario) {
            boolean result = false;
            if (Boolean.TRUE.equals(screenDescriptor.getChangeStage())) {
                result = deliriumService.calcStage(scenarioDto.getOrderId()).isStatusChanged();
            }
            if (Boolean.TRUE.equals(screenDescriptor.getChangeStageWithDraft())) {
                saveDraft();
                result = result || deliriumService.calcStageWithDraft(scenarioDto.getOrderId()).isStatusChanged();
            }
            return result;
        }


        return false;
    }

    @Override
    public void reInitScenario() {
        log.info("Reinitializing scenario" + formatScenarioLogFields());

        ScenarioDto scenarioDto = response.getScenarioDto();
        InitServiceDto initServiceDto = new InitServiceDto();
        initServiceDto.setOrderId(scenarioDto.getOrderId().toString());
        initServiceDto.setTargetId(scenarioDto.getTargetCode());
        initServiceDto.setServiceInfo(scenarioDto.getServiceInfo());

        response = response.getIsInviteScenario() ?
                scenarioInitializerService.getInvitedScenario(initServiceDto, serviceId) :
                scenarioInitializerService.getExistingScenario(initServiceDto, serviceId);
    }

    @Override
    public void saveDraft() {
        ScenarioDto scenarioDto = response.getScenarioDto();
        if (scenarioDto.getOrderId() != null) {
            draftClient.saveDraft(scenarioDto, serviceId, userPersonalData.getUserId(), userPersonalData.getOrgId(), serviceDescriptor.getDraftTtl(), serviceDescriptor.getOrderTtl());
        }
    }

    public String formatScenarioLogFields() {
        final var scenarioDto = request.getScenarioDto();
        final var joinedParams = Stream.of(
            Pair.of("serviceId", scenarioDto.getServiceCode()),
            Pair.of("targetId", scenarioDto.getTargetCode()),
            Pair.of("orderId", scenarioDto.getOrderId()),
            Pair.of("screenId", scenarioDto.getDisplay() != null ? scenarioDto.getDisplay().getId() : null)
        ).filter(e -> e.getValue() != null).map(e -> e.getKey() + "=" + e.getValue()).collect(Collectors.joining(","));
        return joinedParams.isEmpty() ? joinedParams : " (" + joinedParams + ")";
    }
}
