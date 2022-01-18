package ru.gosuslugi.pgu.fs.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.gosuslugi.pgu.dto.ApplicantDto;
import ru.gosuslugi.pgu.dto.ApplicantRole;
import ru.gosuslugi.pgu.dto.ScenarioDto;
import ru.gosuslugi.pgu.common.esia.search.dto.UserPersonalData;
import ru.gosuslugi.pgu.common.logging.annotation.Log;
import ru.gosuslugi.pgu.draft.model.DraftHolderDto;
import ru.gosuslugi.pgu.fs.delirium.DeliriumClient;
import ru.gosuslugi.pgu.fs.delirium.mapper.DeliriumMapper;
import ru.gosuslugi.pgu.fs.delirium.model.DeliriumRequestDto;
import ru.gosuslugi.pgu.fs.delirium.model.DeliriumStageDto;
import ru.gosuslugi.pgu.fs.service.DeliriumService;

import java.util.HashMap;
import java.util.Objects;
import java.util.Optional;

@Log
@Service
@RequiredArgsConstructor
public class DeliriumServiceImpl implements DeliriumService {

    private final DeliriumClient deliriumClient;
    private final DeliriumMapper deliriumMapper;
    private final UserPersonalData personalData;

    @Override
    public void notifyScenarioEnd(ScenarioDto scenarioDto) {
        DeliriumRequestDto deliriumRequestDto = deliriumMapper.mapToDeliriumDto(scenarioDto, personalData.getUserId());
        deliriumClient.postOrder(deliriumRequestDto);
    }

    @Override
    public void requestAction(ScenarioDto scenarioDto, String deliriumAction) {
        DeliriumRequestDto deliriumRequestDto = deliriumMapper.mapToDeliriumDto(scenarioDto, deliriumAction, personalData.getUserId());
        deliriumClient.postOrder(deliriumRequestDto);
    }

    @Override
    public DeliriumStageDto getStage(long orderId) {
        return Optional.ofNullable(deliriumClient.getStage(orderId)).orElse(getDefaultStage(orderId));
    }

    @Override
    public DeliriumStageDto calcStage(long orderId) {
        return Optional.ofNullable(deliriumClient.calcStage(orderId)).orElse(getDefaultStage(orderId));
    }

    @Override
    public DeliriumStageDto calcStageWithDraft(long orderId) {
        return Optional.ofNullable(deliriumClient.calcStageWithDraft(orderId)).orElse(getDefaultStage(orderId));
    }

    private DeliriumStageDto getDefaultStage(long orderId) {
        return new DeliriumStageDto(orderId, DEFAULT_STAGE, null, false, null, null);
    }

    @Override
    public ApplicantRole getUserRole(DraftHolderDto draftHolderDto) {
        if(Objects.isNull(draftHolderDto) || Objects.isNull(draftHolderDto.getBody())){
            return DEFAULT_USER_ROLE;
        }
        String oid = personalData.getUserId().toString();
        ApplicantDto participant = draftHolderDto.getBody().getParticipants().get(oid);
        return participant != null ? participant.getRole() : DEFAULT_USER_ROLE;
    }

    @Override
    public DeliriumStageDto getStage(ApplicantRole role, Long orderId, boolean useDefaultStage) {
        if(useDefaultStage) {
            return getDefaultStage(orderId);
        }
        return DEFAULT_USER_ROLE.equals(role) ? getStage(orderId) : calcStage(orderId);
    }
}
