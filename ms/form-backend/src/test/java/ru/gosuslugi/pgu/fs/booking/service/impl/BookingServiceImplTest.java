package ru.gosuslugi.pgu.fs.booking.service.impl;

import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import ru.gosuslugi.pgu.dto.ApplicantAnswer;
import ru.gosuslugi.pgu.dto.ScenarioDto;
import ru.gosuslugi.pgu.dto.descriptor.BookingInfo;
import ru.gosuslugi.pgu.dto.descriptor.RuleCondition;
import ru.gosuslugi.pgu.dto.descriptor.ServiceDescriptor;
import ru.gosuslugi.pgu.fs.booking.client.PguBookingInfoClient;
import ru.gosuslugi.pgu.fs.booking.dto.PguBookingDto;
import ru.gosuslugi.pgu.fs.common.descriptor.MainDescriptorService;
import ru.gosuslugi.pgu.fs.common.service.JsonProcessingService;
import ru.gosuslugi.pgu.fs.common.service.RuleConditionService;

import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@SpringBootTest
class BookingServiceImplTest {

    public static final String SERVICE_ID = "10000000103";
    public static final long ORDER_ID = 100L;
    public static final String BOOKING_LINK = "/600103/1/booking";
    public static final String INIT_SCREEN_ID = "s66";
    public static final List<String> AVAILABLE_STATUS_LIST = List.of("1");
    public static final String CHECKED_COMPONENT = "q1";
    public static final String VALID_ANSWER = "ПРАВИЛЬНЫЙ ОТВЕТ";

    @Mock
    private MainDescriptorService mainDescriptorService;

    @Mock
    private PguBookingInfoClient pguBookingInfoClient;

    @Autowired
    private RuleConditionService ruleConditionService;

    @Autowired
    private JsonProcessingService jsonProcessingService;

    @Test
    void successfullySendBookingInfo() {
        RuleCondition ruleCondition = createRuleCondition(VALID_ANSWER);
        BookingInfo bookingInfo = createBookingInfo(ruleCondition, true);
        ServiceDescriptor serviceDescriptor = createServiceDescriptor(bookingInfo);

        given(mainDescriptorService.getServiceDescriptor(SERVICE_ID)).willReturn(serviceDescriptor);

        var scenarioDto = new ScenarioDto();
        scenarioDto.setOrderId(ORDER_ID);
        scenarioDto.setApplicantAnswers(Map.of(CHECKED_COMPONENT, new ApplicantAnswer(true, VALID_ANSWER)));

        var bookingService = new BookingServiceImpl(
                mainDescriptorService, ruleConditionService, jsonProcessingService, pguBookingInfoClient
        );

        var expectedDto = PguBookingDto.createFrom(bookingInfo);

        bookingService.sendBookingInfo(SERVICE_ID, scenarioDto);

        verify(pguBookingInfoClient).sendPguBookingInfo(ORDER_ID, expectedDto);
        verifyNoMoreInteractions(pguBookingInfoClient);
    }

    private RuleCondition createRuleCondition(String value) {
        var ruleCondition = new RuleCondition();
        ruleCondition.setField(CHECKED_COMPONENT);
        ruleCondition.setVisited(true);
        ruleCondition.setValue(value);
        return ruleCondition;
    }

    private BookingInfo createBookingInfo(RuleCondition ruleCondition, boolean enabled) {
        var bookingInfo = new BookingInfo();
        bookingInfo.setEnabled(enabled);
        bookingInfo.setAvailableIf(Set.of(ruleCondition));
        bookingInfo.setAvailableStatusList(AVAILABLE_STATUS_LIST);
        bookingInfo.setBookingLink(BOOKING_LINK);
        bookingInfo.setInitScreenId(INIT_SCREEN_ID);
        return bookingInfo;
    }

    private ServiceDescriptor createServiceDescriptor(BookingInfo bookingInfo) {
        var serviceDescriptor = new ServiceDescriptor();
        serviceDescriptor.setBookingInfo(bookingInfo);
        return serviceDescriptor;
    }

    @Test
    void notSendBookingInfoWhenRulesAreInvalid() {
        RuleCondition ruleCondition = createRuleCondition(VALID_ANSWER);
        BookingInfo bookingInfo = createBookingInfo(ruleCondition, true);
        ServiceDescriptor serviceDescriptor = createServiceDescriptor(bookingInfo);

        given(mainDescriptorService.getServiceDescriptor(SERVICE_ID)).willReturn(serviceDescriptor);

        var scenarioDto = new ScenarioDto();
        scenarioDto.setOrderId(ORDER_ID);
        scenarioDto.setApplicantAnswers(Map.of(CHECKED_COMPONENT, new ApplicantAnswer(true, "НЕ ПРАВИЛЬНЫЙ ОТВЕТ")));

        var bookingService = new BookingServiceImpl(
                mainDescriptorService, ruleConditionService, jsonProcessingService, pguBookingInfoClient
        );

        bookingService.sendBookingInfo(SERVICE_ID, scenarioDto);

        verifyNoInteractions(pguBookingInfoClient);
    }

    @Test
    void notSendBookingInfoWhenBookingInfoDisabled() {
        RuleCondition ruleCondition = createRuleCondition(VALID_ANSWER);
        BookingInfo disabledBookingInfo = createBookingInfo(ruleCondition, false);
        ServiceDescriptor serviceDescriptor = createServiceDescriptor(disabledBookingInfo);

        given(mainDescriptorService.getServiceDescriptor(SERVICE_ID)).willReturn(serviceDescriptor);

        var scenarioDto = new ScenarioDto();
        scenarioDto.setOrderId(ORDER_ID);
        scenarioDto.setApplicantAnswers(Map.of(CHECKED_COMPONENT, new ApplicantAnswer(true, VALID_ANSWER)));

        var bookingService = new BookingServiceImpl(
                mainDescriptorService, ruleConditionService, jsonProcessingService, pguBookingInfoClient
        );

        bookingService.sendBookingInfo(SERVICE_ID, scenarioDto);

        verifyNoInteractions(pguBookingInfoClient);
    }
}