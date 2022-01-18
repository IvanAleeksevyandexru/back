package ru.gosuslugi.pgu.fs.booking.process.impl;

import lombok.RequiredArgsConstructor;
import lombok.val;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.stereotype.Component;
import org.springframework.web.context.WebApplicationContext;
import ru.gosuslugi.pgu.common.esia.search.dto.UserPersonalData;
import ru.gosuslugi.pgu.draft.DraftClient;
import ru.gosuslugi.pgu.dto.DisplayRequest;
import ru.gosuslugi.pgu.fs.booking.dto.InitBookingRequestDto;
import ru.gosuslugi.pgu.fs.booking.dto.InitBookingResponseDto;
import ru.gosuslugi.pgu.fs.booking.process.InitBookingProcess;
import ru.gosuslugi.pgu.fs.common.descriptor.MainDescriptorService;
import ru.gosuslugi.pgu.fs.common.helper.HelperScreenRegistry;
import ru.gosuslugi.pgu.fs.common.helper.ScreenHelper;
import ru.gosuslugi.pgu.fs.common.service.ComponentService;
import ru.gosuslugi.pgu.fs.exception.InitBookingProcessException;
import ru.gosuslugi.pgu.fs.pgu.service.PguOrderService;

import java.util.Objects;

@Component
@Scope(value = WebApplicationContext.SCOPE_REQUEST, proxyMode = ScopedProxyMode.TARGET_CLASS)
@RequiredArgsConstructor
public class InitBookingProcessImpl implements InitBookingProcess {

    private InitBookingRequestDto initBookingRequestDto;

    private InitBookingResponseDto initBookingResponseDto;

    private final UserPersonalData userPersonalData;
    private final DraftClient draftClient;
    private final MainDescriptorService mainDescriptorService;
    private final HelperScreenRegistry screenRegistry;
    private final ComponentService componentService;

    @Override
    public InitBookingProcess of(InitBookingRequestDto initBookingRequestDto) {
        this.initBookingRequestDto = initBookingRequestDto;
        this.initBookingResponseDto = new InitBookingResponseDto();
        return this;
    }

    @Override
    public InitBookingProcess clearFinishedAndCurrentScreens() {
        this.initBookingResponseDto.getScenarioDto().getFinishedAndCurrentScreens().clear();
        return this;
    }

    @Override
    public InitBookingProcess receiveParentOrder() {
        val parentDraft = draftClient.getDraftById(initBookingRequestDto.getParentOrderId(), userPersonalData.getUserId(), userPersonalData.getOrgId());
        if(Objects.isNull(parentDraft)){
            throw new InitBookingProcessException("Отсутствует order для создания букинга");
        }
        initBookingResponseDto.setScenarioDto(parentDraft.getBody());
        initBookingResponseDto.getScenarioDto().setOrderId(null);
        initBookingResponseDto.getScenarioDto().setMasterOrderId(initBookingRequestDto.getParentOrderId());
        return this;
    }

    @Override
    public InitBookingProcess calculateInitScreen() {
        val sd = mainDescriptorService.getServiceDescriptor(initBookingRequestDto.getServiceId());
        if(Objects.isNull(sd.getBookingInfo())){
            throw new InitBookingProcessException("Описание процесса букинга отсутствует для услуги" + initBookingRequestDto.getServiceId());
        }
        val initBookingScreen = sd.getBookingInfo().getInitScreenId();
        sd.getScreens().stream().filter(screen -> screen.getId().equals(initBookingScreen)).forEach(screen -> {
            ScreenHelper screenHelper = screenRegistry.getHelper(screen.getType());
            if (Objects.nonNull(screenHelper)) {
                screen = screenHelper.processScreen(screen, initBookingResponseDto.getScenarioDto());
            }
            DisplayRequest displayRequest = new DisplayRequest(screen, componentService.getScreenFields(screen, initBookingResponseDto.getScenarioDto(), sd));
            initBookingResponseDto.getScenarioDto().setDisplay(displayRequest);
            initBookingResponseDto.getScenarioDto().getFinishedAndCurrentScreens().add(displayRequest.getId());
        });

        return this;
    }

    @Override
    public InitBookingResponseDto finish() {
        return initBookingResponseDto;
    }
}
