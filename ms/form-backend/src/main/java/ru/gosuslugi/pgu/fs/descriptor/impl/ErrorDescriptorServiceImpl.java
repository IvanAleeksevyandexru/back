package ru.gosuslugi.pgu.fs.descriptor.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.gosuslugi.pgu.dto.descriptor.ServiceDescriptor;
import ru.gosuslugi.pgu.fs.common.service.ComponentService;
import ru.gosuslugi.pgu.fs.descriptor.ErrorDescriptorService;

import javax.annotation.PostConstruct;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class ErrorDescriptorServiceImpl implements ErrorDescriptorService {

    private final ComponentService componentService;
    private final List<ServiceDescriptor> errorDescriptors;
    Map<String, ServiceDescriptor> serviceDescriptorMap = new HashMap<>();

    @PostConstruct
    public void init() {
        for (ServiceDescriptor serviceDescriptor: errorDescriptors) {
            applyNewServiceDescriptor(serviceDescriptor.getServiceName(), serviceDescriptor);
        }
    }

    @Override
    public void applyNewServiceDescriptor(String serviceId, ServiceDescriptor serviceDescriptor) {
        serviceDescriptorMap.put(serviceId, serviceDescriptor);
    }

    @Override
    public ServiceDescriptor getServiceDescriptor(String serviceId) {
        return serviceDescriptorMap.get(serviceId);
    }
}
