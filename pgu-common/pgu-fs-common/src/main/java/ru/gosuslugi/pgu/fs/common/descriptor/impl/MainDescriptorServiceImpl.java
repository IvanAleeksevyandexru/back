package ru.gosuslugi.pgu.fs.common.descriptor.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;
import ru.gosuslugi.pgu.dto.descriptor.ServiceDescriptor;
import ru.gosuslugi.pgu.fs.common.descriptor.MainDescriptorService;
import ru.gosuslugi.pgu.fs.common.service.JsonProcessingService;
import ru.gosuslugi.pgu.sd.storage.ServiceDescriptorClient;

import java.util.HashMap;
import java.util.Map;


@Primary
@Service
@RequiredArgsConstructor
public class MainDescriptorServiceImpl implements MainDescriptorService {

    private final Map<String, ServiceDescriptor> serviceDescriptorMap = new HashMap<>();

    private final ServiceDescriptorClient serviceDescriptorClient;
    private final JsonProcessingService jsonProcessingService;

    @Override
    public void applyNewServiceDescriptor(String serviceId, ServiceDescriptor serviceDescriptor) {
        serviceDescriptorMap.put(serviceId, serviceDescriptor);
    }

    @Override
    public ServiceDescriptor getServiceDescriptor(String serviceId) {
        ServiceDescriptor descriptor = serviceDescriptorMap.containsKey(serviceId)
                ? serviceDescriptorMap.get(serviceId)
                : jsonProcessingService.fromJson(
                serviceDescriptorClient.getServiceDescriptor(serviceId),
                ServiceDescriptor.class
        );
        DescriptorClarificationConverter.convert(descriptor);
        return descriptor;
    }

    @Override
    public void clearCachedDescriptor(String serviceId) {
        serviceDescriptorClient.clearCachedDescriptor(serviceId);
    }
}
