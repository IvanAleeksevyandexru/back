package ru.gosuslugi.pgu.fs.service;

import ru.gosuslugi.pgu.core.lk.model.order.Order;
import ru.gosuslugi.pgu.draft.model.DraftHolderDto;
import ru.gosuslugi.pgu.dto.ApplicantRole;
import ru.gosuslugi.pgu.dto.order.OrderInfoDto;
import ru.gosuslugi.pgu.fs.delirium.model.DeliriumStageDto;

public interface OrderInfoService {
    /**
     * Возвращает информацию о черновике
     * @param order заявка из ЛК
     * @param role роль текущего пользователя
     * @param stageDto стадия заполнения черновика
     * @param draftHolderDto черновик из БД
     * @param alwaysContinueScenario настройка услуги, что всегда продолжаем с последнего экрана
     * @return {@link OrderInfoDto}
     */
    OrderInfoDto getOrderInfo(Order order, ApplicantRole role, DeliriumStageDto stageDto, DraftHolderDto draftHolderDto, Boolean alwaysContinueScenario);
}
