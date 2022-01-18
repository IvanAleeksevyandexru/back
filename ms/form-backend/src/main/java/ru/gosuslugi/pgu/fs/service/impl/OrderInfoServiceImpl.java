package ru.gosuslugi.pgu.fs.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import ru.gosuslugi.pgu.dto.ApplicantDto;
import ru.gosuslugi.pgu.dto.ApplicantRole;
import ru.gosuslugi.pgu.dto.order.OrderInfoDto;
import ru.gosuslugi.pgu.common.esia.search.dto.UserPersonalData;
import ru.gosuslugi.pgu.components.descriptor.types.Stage;
import ru.gosuslugi.pgu.core.lk.model.order.Order;
import ru.gosuslugi.pgu.core.lk.model.order.OrderPayment;
import ru.gosuslugi.pgu.draft.model.DraftHolderDto;
import ru.gosuslugi.pgu.fs.delirium.model.DeliriumStageDto;
import ru.gosuslugi.pgu.fs.pgu.service.PguOrderService;
import ru.gosuslugi.pgu.fs.service.DeliriumService;
import ru.gosuslugi.pgu.fs.service.OrderInfoService;
import ru.gosuslugi.pgu.pgu_common.payment.dto.bill.BillInfoResponseWrapper;
import ru.gosuslugi.pgu.pgu_common.payment.service.BillingService;

import java.util.HashMap;
import java.util.Objects;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class OrderInfoServiceImpl implements OrderInfoService {
    private final PguOrderService pguOrderService;
    private final BillingService billingService;
    private final UserPersonalData userPersonalData;

    public final String CAN_START_NEW_ATTR_NAME = "canStartNew";

    @Override
    public OrderInfoDto getOrderInfo(Order order, ApplicantRole role, DeliriumStageDto stageDto, DraftHolderDto draftHolderDto, Boolean alwaysContinueScenario) {
        OrderInfoDto orderInfoDto = new OrderInfoDto();
        boolean canStartNew = false;
        String draftCanStartNew = null;
        if(Objects.nonNull(draftHolderDto)){
            draftCanStartNew = draftHolderDto.getBody().getAdditionalParameters().get(CAN_START_NEW_ATTR_NAME);
        }
        if ((Objects.isNull(alwaysContinueScenario)
                || !alwaysContinueScenario)
                && (Objects.isNull(draftCanStartNew)
                || Boolean.parseBoolean(draftCanStartNew))) {
            canStartNew = canStartNewApplication(order, role, stageDto);
        }
        orderInfoDto.setCanStartNew(canStartNew);
        orderInfoDto.setIsInviteScenario(isInviteScenario(draftHolderDto));
        return orderInfoDto;
    }

    /**
     * начать заполнение заявки заново может основной заявитель в случаях:
     * 1. Если не были приглашены созаявители или заявление вернулось в статус Draft
     * 2. Если было осуществлено предварительное бронирование
     * 3. Не была совершена оплата
     * @param order
     * @param role
     * @param stageDto
     * @return
     */
    private boolean canStartNewApplication(Order order, ApplicantRole role, DeliriumStageDto stageDto){
        boolean applicantOrDraftStage = DeliriumService.DEFAULT_USER_ROLE.equals(role) && (DeliriumService.DEFAULT_STAGE.equals(stageDto.getStage()) || Stage.Draft.name().equals(stageDto.getStage()));
        boolean slotBooked = order.getInvitation() != null;
        boolean noPaymentsExists = order.getPaymentCount() < 1;
        if (applicantOrDraftStage && !slotBooked) {
            return noPaymentsExists || !isAnyPaymentDone(order.getId());
        }
        return false;
    }

    private boolean isAnyPaymentDone(Long orderId) {
        Order orderWithPayment = pguOrderService.getOrderWithPaymentInfo(orderId);
        for (OrderPayment payment: orderWithPayment.getOrderPayments()) {
            if (payment.isIsPaid()) {
                return true;
            }
            BillInfoResponseWrapper billInfo = billingService.getBillInfoByBillNumber(userPersonalData.getToken(), payment.getUin());
            if (billingService.isBillPaid(billInfo)) {
                return true;
            }
        }
        return false;
    }

    private boolean isInviteScenario(DraftHolderDto draftHolderDto){
        if(Objects.isNull(draftHolderDto) || Objects.isNull(draftHolderDto.getBody())){
            return false;
        }
        // Получаем роль пользователя и стадию заполнения заявки
        String oid = userPersonalData.getUserId().toString();
        ApplicantDto participant = Optional.ofNullable(draftHolderDto.getBody().getParticipants()).orElse(new HashMap<>()).get(oid);

        // Если созаявитель перешел не по ссылке из приглашения, а из черновика
        return participant != null;
    }
}
