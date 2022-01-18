package ru.gosuslugi.pgu.fs.delirium.client;

import lombok.extern.slf4j.Slf4j;
import ru.gosuslugi.pgu.fs.controller.TestDemoController;
import ru.gosuslugi.pgu.fs.delirium.DeliriumClient;
import ru.gosuslugi.pgu.fs.delirium.model.DeliriumResponseDto;
import ru.gosuslugi.pgu.fs.delirium.model.DeliriumStageDto;
import ru.gosuslugi.pgu.fs.delirium.model.DeliriumRequestDto;
import ru.gosuslugi.pgu.fs.service.DeliriumService;

@Slf4j
public class DeliriumClientStub implements DeliriumClient {
    public static DeliriumStageDto TEST_STAGE_DTO = new DeliriumStageDto(0L, DeliriumService.DEFAULT_STAGE, null, false, null, null);

    @Override
    public DeliriumResponseDto postOrder(DeliriumRequestDto deliriumRequestDto) {
        if(log.isInfoEnabled()) log.info("posting order to delirium. Order {}", deliriumRequestDto);
        return null;
    }

    /**
     * @see TestDemoController#updateDeliriumStage(DeliriumStageDto)
     */
    @Override
    public DeliriumStageDto getStage(Long orderId) {
        DeliriumStageDto deliriumStageDto = new DeliriumStageDto(orderId, TEST_STAGE_DTO.getStage(), TEST_STAGE_DTO.getComplete(), false, TEST_STAGE_DTO.getTimerToken(), TEST_STAGE_DTO.getTimerObjectId());
        if(log.isInfoEnabled()) log.info("returning {}", deliriumStageDto);
        return deliriumStageDto;
    }

    /**
     * @see TestDemoController#updateDeliriumStage(DeliriumStageDto)
     */
    @Override
    public DeliriumStageDto calcStage(Long orderId) {
        DeliriumStageDto deliriumStageDto = new DeliriumStageDto(orderId, TEST_STAGE_DTO.getStage(), TEST_STAGE_DTO.getComplete(), TEST_STAGE_DTO.isStatusChanged(), TEST_STAGE_DTO.getTimerToken(), TEST_STAGE_DTO.getTimerObjectId());
        if(log.isInfoEnabled()) log.info("returning {}", deliriumStageDto);
        return deliriumStageDto;
    }

    /**
     * @see TestDemoController#updateDeliriumStage(DeliriumStageDto)
     */
    @Override
    public DeliriumStageDto calcStageWithDraft(Long orderId) {
        DeliriumStageDto deliriumStageDto = new DeliriumStageDto(orderId, TEST_STAGE_DTO.getStage(), TEST_STAGE_DTO.getComplete(), TEST_STAGE_DTO.isStatusChanged(), TEST_STAGE_DTO.getTimerToken(), TEST_STAGE_DTO.getTimerObjectId());
        if(log.isInfoEnabled()) log.info("returning {}", deliriumStageDto);
        return deliriumStageDto;
    }

}
