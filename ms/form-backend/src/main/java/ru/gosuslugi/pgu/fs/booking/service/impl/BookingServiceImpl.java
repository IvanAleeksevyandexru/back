package ru.gosuslugi.pgu.fs.booking.service.impl;

import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.JsonPath;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.gosuslugi.pgu.dto.ApplicantAnswer;
import ru.gosuslugi.pgu.dto.ScenarioDto;
import ru.gosuslugi.pgu.dto.descriptor.BookingInfo;
import ru.gosuslugi.pgu.dto.descriptor.RuleCondition;
import ru.gosuslugi.pgu.fs.booking.client.PguBookingInfoClient;
import ru.gosuslugi.pgu.fs.booking.dto.PguBookingDto;
import ru.gosuslugi.pgu.fs.booking.service.BookingService;
import ru.gosuslugi.pgu.fs.common.descriptor.MainDescriptorService;
import ru.gosuslugi.pgu.fs.common.service.JsonProcessingService;
import ru.gosuslugi.pgu.fs.common.service.RuleConditionService;

import java.util.*;

import static java.util.Objects.nonNull;

@Slf4j
@Service
@RequiredArgsConstructor
public class BookingServiceImpl implements BookingService {

    private final MainDescriptorService mainDescriptorService;
    private final RuleConditionService ruleConditionService;
    private final JsonProcessingService jsonProcessingService;
    private final PguBookingInfoClient pguBookingInfoClient;

    @Override
    public void sendBookingInfo(String serviceId, ScenarioDto scenarioDto) {
        var bookingInfo = mainDescriptorService.getServiceDescriptor(serviceId).getBookingInfo();
        log.info("Checking sending booking info for order" + scenarioDto.getOrderId());
        if (checkIfSendNeeded(bookingInfo) && isAllRulesValid(bookingInfo.getAvailableIf(), scenarioDto)) {
            var dto = PguBookingDto.createFrom(bookingInfo);
            sendPguBookingInfo(scenarioDto.getOrderId(), dto);
        }

    }

    private boolean checkIfSendNeeded(BookingInfo bookingInfo) {
        return nonNull(bookingInfo) && bookingInfo.isEnabled();
    }

    private boolean isAllRulesValid(Set<RuleCondition> ruleConditions, ScenarioDto scenarioDto) {
        Map<String, ApplicantAnswer> applicantAnswers = scenarioDto.getApplicantAnswers();
        Map<String, ApplicantAnswer> currentValue = scenarioDto.getCurrentValue();

        DocumentContext applicantAnswersContext = JsonPath.parse(jsonProcessingService.convertAnswersToJsonString(applicantAnswers));
        DocumentContext currentValueContext = JsonPath.parse(jsonProcessingService.convertAnswersToJsonString(currentValue));
        DocumentContext serviceInfoContext = JsonPath.parse(jsonProcessingService.toJson(scenarioDto.getServiceInfo()));
        DocumentContext cycledCurrentItemContext = JsonPath.parse(jsonProcessingService.toJson(scenarioDto.getCycledApplicantAnswerContext()));

        return ruleConditionService.isRuleApplyToAnswers(
                ruleConditions,
                Arrays.asList(currentValue, applicantAnswers),
                Arrays.asList(currentValueContext, applicantAnswersContext, serviceInfoContext, cycledCurrentItemContext),
                scenarioDto
        );
    }

    private void sendPguBookingInfo(Long orderId, PguBookingDto pguBookingDto) {
        pguBookingInfoClient.sendPguBookingInfo(orderId, pguBookingDto);
    }
}
