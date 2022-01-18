package ru.gosuslugi.pgu.fs.common.service;

import ru.gosuslugi.pgu.dto.ApplicantAnswer;
import ru.gosuslugi.pgu.dto.descriptor.ServiceDescriptor;

import java.util.Map;
import java.util.Set;

public interface CycledScreenService extends ScreenService {
    Set<String> getApplicantAnswerKeysWithCycled(Map<String, ApplicantAnswer> answerMap, ServiceDescriptor serviceDescriptor, String serviceId);
}
