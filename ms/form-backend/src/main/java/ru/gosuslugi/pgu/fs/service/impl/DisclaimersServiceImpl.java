package ru.gosuslugi.pgu.fs.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import ru.gosuslugi.pgu.fs.common.exception.FormBaseException;
import ru.gosuslugi.pgu.dto.DisclaimerDto;
import ru.gosuslugi.pgu.dto.DisclaimerLevel;
import ru.gosuslugi.pgu.dto.DisplayRequest;
import ru.gosuslugi.pgu.dto.LocalizedDisclaimerMessage;
import ru.gosuslugi.pgu.dto.PortalDisclaimer;
import ru.gosuslugi.pgu.dto.ScenarioDto;
import ru.gosuslugi.pgu.dto.descriptor.FieldComponent;
import ru.gosuslugi.pgu.dto.descriptor.ScreenDescriptor;
import ru.gosuslugi.pgu.dto.descriptor.ServiceDescriptor;
import ru.gosuslugi.pgu.fs.common.service.ComponentReferenceService;
import ru.gosuslugi.pgu.fs.descriptor.SubDescriptorService;
import ru.gosuslugi.pgu.fs.pgu.client.impl.PguUtilsClientImpl;
import ru.gosuslugi.pgu.fs.pgu.dto.PguServiceCodes;
import ru.gosuslugi.pgu.fs.service.DisclaimersClient;
import ru.gosuslugi.pgu.fs.service.DisclaimersService;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DisclaimersServiceImpl implements DisclaimersService {
    private final DisclaimersClient disclaimersClient;
    private final SubDescriptorService subDescriptorService;
    private final ComponentReferenceService componentReferenceService;
    private final PguUtilsClientImpl orderUtilsClient;

    @Override
    public List<DisclaimerDto> getDisclaimers(String serviceCode, String targetCode) {
        PguServiceCodes pguServiceCodes = orderUtilsClient.getPguServiceCodes(serviceCode, targetCode);
        List<PortalDisclaimer> disclaimers = disclaimersClient.getDisclaimers(pguServiceCodes.getPassport(), pguServiceCodes.getTarget());
        return disclaimers.stream().filter(disclaimer -> !disclaimer.getIsHidden() && !disclaimer.getMessages().isEmpty()).map(this::mapToDisclaimerDto).collect(Collectors.toList());
    }

    @Override
    public DisplayRequest getDisplayForCriticalDisclaimer(DisclaimerDto disclaimer, ScenarioDto scenarioDto) {
        ServiceDescriptor criticalErrorServiceDescriptor = ServiceDescriptor.getCopy(subDescriptorService.getServiceDescriptor("criticalError"));
        ScreenDescriptor criticalErrorScreenDescriptor = criticalErrorServiceDescriptor.getScreenDescriptorById(criticalErrorServiceDescriptor.getInit())
                .orElseThrow(() -> new FormBaseException("Не найден экран для критической ошибки"));
        List<FieldComponent> components = criticalErrorServiceDescriptor.getFieldComponentsForScreen(criticalErrorScreenDescriptor);
        components.forEach(component -> {
            component.addArgument("errorMsg", disclaimer.getMessage());
            component.addArgument("errorHeader", disclaimer.getTitle());
            componentReferenceService.processComponentRefs(component, scenarioDto);
        });

        return new DisplayRequest(criticalErrorScreenDescriptor, components);
    }

    private DisclaimerDto mapToDisclaimerDto(PortalDisclaimer disclaimer) {
        String message;
        Optional<LocalizedDisclaimerMessage> ruMessage = disclaimer.getMessages().stream().filter(msg -> "Ru".equalsIgnoreCase(msg.getLanguage())).findFirst();
        String title;
        if (ruMessage.isPresent()) {
            message = ruMessage.get().getMessage();
            title = ruMessage.get().getTitle();
        } else {
            LocalizedDisclaimerMessage msg = disclaimer.getMessages().get(0);
            message = msg.getMessage();
            title = msg.getTitle();
        }
        DisclaimerLevel level = DisclaimerLevel.valueOf(disclaimer.getLevel());
        if (!StringUtils.hasText(title)) {
            title = level.getHeader();
        }
        return new DisclaimerDto(disclaimer.getId(), title, message, level, disclaimer.getMnemonic());
    }
}
