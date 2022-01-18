package ru.gosuslugi.pgu.fs.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.gosuslugi.pgu.common.esia.search.dto.PersonWithAge;
import ru.gosuslugi.pgu.dto.ApplicantDto;
import ru.gosuslugi.pgu.dto.ApplicantMode;
import ru.gosuslugi.pgu.dto.ApplicantRole;
import ru.gosuslugi.pgu.dto.ScenarioDto;
import ru.gosuslugi.pgu.dto.descriptor.FieldComponent;
import ru.gosuslugi.pgu.dto.descriptor.ServiceDescriptor;
import ru.gosuslugi.pgu.fs.common.service.CycledScreenService;
import ru.gosuslugi.pgu.fs.service.ParticipantService;

import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class ParticipantServiceImpl implements ParticipantService {

    public static final String APPLICANT_ROLE = "role";
    public static final String APPLICANT_MODE = "mode";
    private static final String PARTICIPANT_NODE_IN_ATTRS = "participant";

    private final CycledScreenService cycledScreenService;

    public void setParticipant(ScenarioDto scenarioDto, FieldComponent fieldComponent, PersonWithAge ps, Integer itemIndex) {
        Optional<Map<String, String>> participant = Optional.ofNullable(fieldComponent.getAttrs()).map(v -> (Map<String, String>) v.get("participant"));
        participant.ifPresent(map -> {
            ApplicantDto applicantDto = new ApplicantDto();
            applicantDto.setRole(ApplicantRole.valueOf(map.get(APPLICANT_ROLE)));
            applicantDto.setMode(ApplicantMode.valueOf(map.get(APPLICANT_MODE)));
            applicantDto.setComponent(fieldComponent.getId());
            applicantDto.setIndex(itemIndex);
            Map<String, ApplicantDto> participantsToRemove = Collections.emptyMap();
            if (Objects.isNull(itemIndex) || itemIndex == 0) {
                participantsToRemove = scenarioDto.getParticipants().entrySet().stream()
                        .filter(entry -> fieldComponent.getId().equals(entry.getValue().getComponent()))
                        .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
            }
            Map<String, ApplicantDto> participants = scenarioDto.getParticipants();
            participantsToRemove.keySet().forEach(participants::remove);
            participants.put(ps.getOid(), applicantDto);
        });
    }

    public void deleteRedundantParticipants(ScenarioDto scenarioDto, ServiceDescriptor serviceDescriptor) {
        Set<String> applicantAnswerKeysWithCycled = cycledScreenService.getApplicantAnswerKeysWithCycled(
                scenarioDto.getApplicantAnswers(),
                serviceDescriptor,
                scenarioDto.getServiceCode()
        );
        Map<String, ApplicantDto> filteredParticipants = scenarioDto.getParticipants().entrySet().stream()
                .filter(entry -> applicantAnswerKeysWithCycled.contains(entry.getValue().getComponent()))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
        scenarioDto.setParticipants(filteredParticipants);
    }

    public void deleteRedundantParticipantsByComponentId(ScenarioDto scenarioDto, ServiceDescriptor serviceDescriptor, String componentId) {
        var participants = scenarioDto.getParticipants();
        participants.entrySet().stream()
                .filter(entry -> componentId.equals(entry.getValue().getComponent()))
                .map(Map.Entry::getKey)
                .collect(Collectors.toList())
                .forEach(participants::remove);
    }

    public void addParticipant(ScenarioDto scenarioDto, FieldComponent fieldComponent, PersonWithAge ps, Integer itemIndex) {
        String mainCycledComponentId = fieldComponent.getAttrs().getOrDefault("mainCycledComponentId", "").toString();
        var participantDescBox = Optional.of(fieldComponent.getAttrs().get(PARTICIPANT_NODE_IN_ATTRS));
        var participantParams = (Map<String, String>) participantDescBox.orElseThrow();
        ApplicantDto applicantDto = ApplicantDto.builder()
                .role(ApplicantRole.valueOf(participantParams.get(APPLICANT_ROLE)))
                .mode(ApplicantMode.valueOf(participantParams.get(APPLICANT_MODE)))
                .component(fieldComponent.getId())
                .cycledComponent(mainCycledComponentId)
                .index(itemIndex).build();
        scenarioDto.getParticipants().put(ps.getOid(), applicantDto);
    }
}
