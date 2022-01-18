package ru.gosuslugi.pgu.fs.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.gosuslugi.pgu.common.core.exception.dto.ExternalErrorInfo;
import ru.gosuslugi.pgu.dto.DisplayRequest;
import ru.gosuslugi.pgu.dto.ScenarioDto;
import ru.gosuslugi.pgu.dto.ScenarioResponse;
import ru.gosuslugi.pgu.dto.ServiceInfoDto;
import ru.gosuslugi.pgu.dto.descriptor.ScreenDescriptor;
import ru.gosuslugi.pgu.dto.descriptor.ServiceDescriptor;
import ru.gosuslugi.pgu.fs.common.descriptor.DescriptorService;
import ru.gosuslugi.pgu.fs.common.helper.ScreenHelper;
import ru.gosuslugi.pgu.fs.common.service.AbstractScreenService;
import ru.gosuslugi.pgu.fs.descriptor.ErrorDescriptorService;
import ru.gosuslugi.pgu.fs.service.ErrorScreenService;

import java.util.NoSuchElementException;
import java.util.Objects;

@Slf4j
@Service
@RequiredArgsConstructor
public class ErrorScreenServiceImpl extends AbstractScreenService implements ErrorScreenService {

    private static final String DEFAULT_ERROR_SCREEN_ID = "default";
    private final ErrorDescriptorService errorDescriptorService;

    @Override
    public ScenarioResponse getInitScreen(int errorCode, ExternalErrorInfo externalErrorInfo) {
        ScenarioResponse scenarioResponse = new ScenarioResponse();
        ScenarioDto scenarioDto = new ScenarioDto();
        ServiceDescriptor currentDescriptor = getDescriptorService().getServiceDescriptor(externalErrorInfo.getId());
        if (currentDescriptor != null) {
            currentDescriptor.setInit(String.valueOf(errorCode));
            scenarioDto.setCurrentScenarioId(1L);
            String initScreenId = currentDescriptor.getInit();
            ScreenDescriptor screen = currentDescriptor.getScreenDescriptorById(initScreenId)
                    .orElseGet(() -> currentDescriptor.getScreenDescriptorById(DEFAULT_ERROR_SCREEN_ID)
                            .orElseThrow(() -> new NoSuchElementException("Не задан экран ошибки по умолчанию")));
            ScreenHelper screenHelper = screenRegistry.getHelper(screen.getType());
            if (Objects.nonNull(screenHelper)) {
                screen = screenHelper.processScreen(screen, scenarioDto);
            }
            DisplayRequest displayRequest = new DisplayRequest(screen, componentService.getScreenFields(screen, scenarioDto, currentDescriptor));
            scenarioDto.getFinishedAndCurrentScreens().add(screen.getId());
            scenarioDto.setDisplay(displayRequest);
        }
        scenarioDto.setServiceInfo(new ServiceInfoDto());
        scenarioResponse.setScenarioDto(scenarioDto);
        return scenarioResponse;
    }

    @Override
    protected DescriptorService getDescriptorService() {
        return errorDescriptorService;
    }
}
