package ru.gosuslugi.pgu.fs.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.gosuslugi.pgu.client.draftconverter.DraftConverterClient;
import ru.gosuslugi.pgu.common.esia.search.dto.UserPersonalData;
import ru.gosuslugi.pgu.draft.DraftClient;
import ru.gosuslugi.pgu.dto.ApplicantRole;
import ru.gosuslugi.pgu.dto.ExternalOrderRequest;
import ru.gosuslugi.pgu.dto.ScenarioDto;
import ru.gosuslugi.pgu.dto.SpAdapterDto;
import ru.gosuslugi.pgu.dto.descriptor.ServiceDescriptor;
import ru.gosuslugi.pgu.fs.common.descriptor.MainDescriptorService;
import ru.gosuslugi.pgu.fs.service.AutofillOrderService;
import ru.gosuslugi.pgu.fs.service.CreateOrderService;
import ru.gosuslugi.pgu.fs.sp.ServiceProcessingClient;

@Service
@RequiredArgsConstructor
public class AutofillOrderServiceImpl implements AutofillOrderService {

    private final UserPersonalData userPersonalData;
    private final ServiceProcessingClient serviceProcessingClient;
    private final DraftConverterClient draftConverterClient;
    private final CreateOrderService createOrderService;
    private final MainDescriptorService mainDescriptorService;
    private final DraftClient draftClient;
    private final FormScenarioDtoServiceImpl formScenarioDtoService;

    @Override
    public ScenarioDto processExternalOrderRequest(ExternalOrderRequest request) {
        ScenarioDto scenarioDto = draftConverterClient.convertExternalOrderRequest(request);
        ServiceDescriptor serviceDescriptor = mainDescriptorService.getServiceDescriptor(request.getServiceId());
        Long orderId = request.getOrderId();
        if (scenarioDto.getOrderId() == null && orderId == null) {
            orderId = createOrderService.tryToCreateOrderId(request.getServiceId(), scenarioDto, serviceDescriptor);
        }

        if (orderId != null) {
            scenarioDto.setOrderId(orderId);
            formScenarioDtoService.updateAdditionalAttributes(scenarioDto, serviceDescriptor.getSmevEnv());
            draftClient.saveDraft(scenarioDto, request.getServiceId(), userPersonalData.getUserId(), userPersonalData.getOrgId(), serviceDescriptor.getDraftTtl(), serviceDescriptor.getOrderTtl());
            SpAdapterDto spAdapterDto = new SpAdapterDto(scenarioDto.getServiceCode(),
                    scenarioDto.getTargetCode(),
                    scenarioDto.getOrderId(),
                    userPersonalData.getUserId(),
                    ApplicantRole.Applicant.name(),
                    null,
                    userPersonalData.getOrgId(),
                    false);
            serviceProcessingClient.send(spAdapterDto, null);
        }
        return scenarioDto;
    }
}
