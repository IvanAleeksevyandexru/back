package ru.gosuslugi.pgu.fs.action.impl;

import lombok.RequiredArgsConstructor;
import net.fortuna.ical4j.model.Calendar;
import net.fortuna.ical4j.model.component.VEvent;
import net.fortuna.ical4j.model.property.*;
import org.springframework.stereotype.Component;
import ru.gosuslugi.pgu.dto.ScenarioDto;
import ru.gosuslugi.pgu.dto.action.ActionRequestDto;
import ru.gosuslugi.pgu.dto.action.ActionResponseDto;
import ru.gosuslugi.pgu.dto.descriptor.FieldComponent;
import ru.gosuslugi.pgu.dto.descriptor.ServiceDescriptor;
import ru.gosuslugi.pgu.dto.descriptor.types.ComponentType;
import ru.gosuslugi.pgu.fs.action.ActionService;
import ru.gosuslugi.pgu.fs.action.ActionType;
import ru.gosuslugi.pgu.fs.common.descriptor.MainDescriptorService;
import ru.gosuslugi.pgu.fs.common.service.JsonProcessingService;
import ru.gosuslugi.pgu.fs.utils.CalculatedAttributesHelper;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

import static ru.gosuslugi.pgu.components.ComponentAttributes.CALCULATIONS_ATTR;

@Component
@RequiredArgsConstructor
public class CalendarAction implements ActionService {

    private static final DateTimeFormatter DATE_TIME_UTC_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMdd'T'HHmmss'Z'");
    private static final DateTimeFormatter VISIT_TIME_ISO_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ssXXX");
    private static final DateTimeFormatter VISIT_TIME_ISO_WITHOUT_TZ_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss'Z'");
    private static final ZoneId UTC_ZONE_ID = ZoneId.of("UTC");
    public static final ZoneId TIMESLOT_DEFAULT_TIMEZONE = ZoneId.of("Europe/Moscow");

    private final MainDescriptorService mainDescriptorService;
    private final JsonProcessingService jsonProcessingService;
    private final CalculatedAttributesHelper calculatedAttributesHelper;

    @Override
    public ActionType getActionType() {
        return ActionType.addToCalendar;
    }

    @Override
    public ActionResponseDto invoke(ActionRequestDto actionRequestDto) {
        ActionResponseDto responseDto = new ActionResponseDto();
        String icsString = createCalendarEvent(actionRequestDto.getScenarioDto());
        responseDto.getResponseData().put("value", icsString);
        responseDto.getResponseData().put("type", "text/calendar");
        return responseDto;
    }

    private String createCalendarEvent(ScenarioDto scenarioDto) {
        Map<String, Object> calculatedValues = new HashMap<>();
        FieldComponent timeSlotComponent = getTimeSlotComponent(mainDescriptorService.getServiceDescriptor(scenarioDto.getServiceCode()), scenarioDto.getApplicantAnswers().keySet());
        if (timeSlotComponent != null && timeSlotComponent.getAttrs() != null) {
            calculatedValues.putAll(Optional.ofNullable(calculatedAttributesHelper.getAllCalculatedValues(CALCULATIONS_ATTR, timeSlotComponent, scenarioDto)).orElse(Collections.emptyMap()));
        }

        var calendarActionBuilder = new CalendarActionBuilder(mainDescriptorService.getServiceDescriptor(scenarioDto.getServiceCode()), jsonProcessingService);
        VEvent reminder = calendarActionBuilder.buildCalendarEvent(scenarioDto.getCurrentUrl(), scenarioDto.getApplicantAnswers(), calculatedValues);
        return getCalendar(reminder).toString();
    }

    private Calendar getCalendar(VEvent reminder) {
        Calendar icsCalendar = new Calendar();
        icsCalendar.getProperties().add(new ProdId("-//EPGU 2.0//iCal4j 2.0//EN"));
        icsCalendar.getProperties().add(Version.VERSION_2_0);
        icsCalendar.getProperties().add(CalScale.GREGORIAN);
        icsCalendar.getProperties().add(Method.REQUEST);
        icsCalendar.getComponents().add(reminder);
        return icsCalendar;
    }

    private FieldComponent getTimeSlotComponent(ServiceDescriptor serviceDescriptor, Set<String> fieldsId) {
        Optional<FieldComponent> originalComponentBox = fieldsId.stream()
                .map(serviceDescriptor::getFieldComponentById)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .filter(fieldComponent -> ComponentType.TimeSlot.equals(fieldComponent.getType()))
                .findFirst();
        if (originalComponentBox.isPresent()) return FieldComponent.getCopy(originalComponentBox.get());
        return null;
    }

    public static String getVDateTimeUtc(String visitTimeISO) {
        ZonedDateTime zonedDateTime = ZonedDateTime.parse(visitTimeISO, VISIT_TIME_ISO_FORMATTER);
        if (zonedDateTime.getOffset().getId().equals("Z")) {
            LocalDateTime localDateTime = LocalDateTime.parse(visitTimeISO, VISIT_TIME_ISO_WITHOUT_TZ_FORMATTER);
            zonedDateTime = localDateTime.atZone(TIMESLOT_DEFAULT_TIMEZONE);
        }
        ZonedDateTime zonedDateTimeUtc = zonedDateTime.withZoneSameInstant(UTC_ZONE_ID);
        return zonedDateTimeUtc.format(DATE_TIME_UTC_FORMATTER);
    }
}
