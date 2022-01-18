package ru.gosuslugi.pgu.fs.common.descriptor;

import ru.gosuslugi.pgu.dto.descriptor.ServiceDescriptor;

public interface DescriptorService {

    /**
     * Method for updating service  supported scenarios, screens and fields
     * @param serviceDescriptor
     * @return
     */
    void applyNewServiceDescriptor(String serviceId, ServiceDescriptor serviceDescriptor);

    ServiceDescriptor getServiceDescriptor(String serviceId);
}
