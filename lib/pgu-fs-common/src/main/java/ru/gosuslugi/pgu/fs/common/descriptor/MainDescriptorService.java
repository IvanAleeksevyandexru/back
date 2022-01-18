package ru.gosuslugi.pgu.fs.common.descriptor;

public interface MainDescriptorService extends DescriptorService {
    void clearCachedDescriptor(String serviceId);
}
