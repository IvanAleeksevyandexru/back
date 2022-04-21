package ru.gosuslugi.pgu.fs.common.descriptor.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;
import ru.gosuslugi.pgu.dto.descriptor.ServiceDescriptor;
import ru.gosuslugi.pgu.fs.common.descriptor.MainDescriptorService;
import ru.gosuslugi.pgu.sd.storage.ServiceDescriptorClient;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;


@Primary
@Service
@RequiredArgsConstructor
public class MainDescriptorServiceImpl implements MainDescriptorService {

    private static final ConcurrentMap<String, ServiceDescriptor> serviceDescriptorMap = new ConcurrentHashMap<>();
    private final ServiceDescriptorClient serviceDescriptorClient;

    @Override
    public void applyNewServiceDescriptor(String serviceId, ServiceDescriptor serviceDescriptor) {
        serviceDescriptorMap.put(serviceId, serviceDescriptor);
    }

    @Override
    public ServiceDescriptor getServiceDescriptor(String serviceId) {
        return serviceDescriptorMap.getOrDefault(serviceId, serviceDescriptorClient.getServiceDescriptor(serviceId));
    }

    @Override
    public void clearCachedDescriptor(String serviceId) {
        serviceDescriptorMap.remove(serviceId);
        serviceDescriptorClient.clearCachedDescriptor(serviceId);
    }
}
