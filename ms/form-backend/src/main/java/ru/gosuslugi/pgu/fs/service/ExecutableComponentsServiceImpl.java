package ru.gosuslugi.pgu.fs.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import ru.gosuslugi.pgu.draft.DraftClient;
import ru.gosuslugi.pgu.draft.model.DraftHolderDto;
import ru.gosuslugi.pgu.dto.ScenarioDto;
import ru.gosuslugi.pgu.dto.descriptor.ServiceDescriptor;
import ru.gosuslugi.pgu.fs.common.descriptor.MainDescriptorService;
import ru.gosuslugi.pgu.fs.common.service.ComponentService;

import java.util.Objects;

@Slf4j
@Service
@RequiredArgsConstructor
public class ExecutableComponentsServiceImpl implements ExecutableComponentsService {

    private final MainDescriptorService mainDescriptorService;
    private final ComponentService componentService;
    private final DraftClient draftClient;

    @Override
    public void process(String serviceId, Long orderId, Long userId, Long orgId) {
        ServiceDescriptor serviceDescriptor = mainDescriptorService.getServiceDescriptor(serviceId);
        DraftHolderDto draft = draftClient.getDraftById(orderId, userId, orgId);
        if(Objects.isNull(draft) || Objects.isNull(draft.getBody())){
            log.info("У заявления {} нет черновика, отменяю постобработку", orderId);
            return;
        }
        ScenarioDto scenarioDto = draft.getBody();
        serviceDescriptor.getScreens().stream()
                .filter(sd -> !CollectionUtils.isEmpty(sd.getAfterOrderCreatedComponentIds()))
                .forEach(sd -> componentService.executeComponentAfterOrderCreated(sd, scenarioDto, serviceDescriptor));
    }
}
