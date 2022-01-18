package ru.gosuslugi.pgu.fs.service;

public interface ExecutableComponentsService {

    void process(String serviceId, Long orderId, Long userId, Long orgId);
}
