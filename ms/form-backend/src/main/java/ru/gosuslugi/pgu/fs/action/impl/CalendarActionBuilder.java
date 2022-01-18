package ru.gosuslugi.pgu.fs.action.impl;

import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.JsonPath;
import lombok.RequiredArgsConstructor;
import net.fortuna.ical4j.model.DateTime;
import net.fortuna.ical4j.model.ParameterList;
import net.fortuna.ical4j.model.Property;
import net.fortuna.ical4j.model.PropertyList;
import net.fortuna.ical4j.model.component.VEvent;
import net.fortuna.ical4j.model.parameter.Cn;
import net.fortuna.ical4j.model.parameter.FmtType;
import net.fortuna.ical4j.model.property.*;
import net.fortuna.ical4j.util.RandomUidGenerator;
import org.springframework.util.StringUtils;
import ru.gosuslugi.pgu.common.core.exception.ValidationException;
import ru.gosuslugi.pgu.dto.ApplicantAnswer;
import ru.gosuslugi.pgu.dto.descriptor.FieldComponent;
import ru.gosuslugi.pgu.dto.descriptor.ServiceDescriptor;
import ru.gosuslugi.pgu.dto.descriptor.types.ComponentType;
import ru.gosuslugi.pgu.fs.common.service.JsonProcessingService;

import java.net.URISyntaxException;
import java.text.ParseException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@RequiredArgsConstructor
public class CalendarActionBuilder {

    private static final String DEFAULT_VALUE = "";

    private static final String VISIT_TIME_ISO_VALUE = "%s.value.timeSlot.visitTimeISO";
    private static final String DATE_TIME_TO_PRINT_VALUE = "%s.value.timeSlot.visitTimeStr";
    private static final String BOOK_ID_VALUE = "%s.value.bookId";

    private static final String ADDRESS_VALUE = "%s.value.attributeValues.address";
    private static final String ADDRESS_UPPERCASE_VALUE = "%s.value.attributeValues.ADDRESS";
    private static final String ADDRESS_OUT_UPPERCASE_VALUE = "%s.value.attributeValues.ADDRESS_OUT";
    private static final String ADDRESS_MO_VALUE = "%s.value.attributeValues.Address_MO";
    private static final String ADDRESS_MO_DOC_LOOKUP_VALUE = "%s.value.docLookup.originalItem.attributes[3].value";

    private static final String DEPARTMENT_VALUE = "%s.value.title";
    private static final String DEPARTMENT_MED_REF_VALUE = "%s.value.convertedAttributes.toMoName";
    private static final String DEPARTMENT_REF_NUMBER_VALUE = "%s.value.referral.toMoName";
    private static final String SUBJECT = "subject";

    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm");
    private static final DateTimeFormatter DATE_TIME_UTC_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMdd'T'HHmmss'Z'");
    private static final DateTimeFormatter VISIT_TIME_ISO_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ssXXX");
    private static final DateTimeFormatter VISIT_TIME_ISO_WITHOUT_TZ_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss'Z'");
    private static final ZoneId UTC_ZONE_ID = ZoneId.of("UTC");
    public static final ZoneId TIMESLOT_DEFAULT_TIMEZONE = ZoneId.of("Europe/Moscow");

    private static final String HTML_DESCRIPTION = "<!DOCTYPE HTML PUBLIC \"-//W3C//DTD HTML 3.2//EN\">" +
            "<HTML><HEAD><TITLE></TITLE></HEAD><BODY><P DIR=LTR>" +
            "<SPAN LANG=\"ru\">Услуга: %s</SPAN></P><P DIR=LTR><SPAN LANG=\"ru\">" +
            "Дата и время события: %s (в часовом поясе подразделения)" +
            "</SPAN></P><P DIR=LTR><SPAN LANG=\"ru\">Ведомство: %s</SPAN></P><P DIR=LTR><SPAN LANG=\"ru\">Адрес: %s</SPAN>" +
            "</P><P DIR=LTR><SPAN LANG=\"ru\"><A HREF=%snotifications/details/EQUEUE/%s><SPAN LANG=\"ru\">Подробности в личном кабинете</SPAN></A></SPAN></P>" +
            "<P DIR=LTR><SPAN LANG=\"ru\">Внимание! Время записи на приём " +
            "отображается в соответствии с часовым поясом устройства. При наличии расхождений " +
            "руководствуйтесь временем записи на приём в личном кабинете.</SPAN></P></BODY></HTML>";
    private static final String DESCRIPTION = "Услуга: %s\n " +
            "Дата и время события: %s (в часовом поясе подразделения)\n " +
            "Ведомство: %s \n " +
            "Адрес: %s\n " +
            "Для просмотра информации перейдите на страницу уведомления %snotifications/details/EQUEUE/%s\n " +
            "Внимание! Время записи на приём отображается в соответствии с часовым поясом устройства. При наличии\n" +
            " расхождений руководствуйтесь временем записи на приём в личном кабинете.";

    private final ServiceDescriptor serviceDescriptor;
    private final JsonProcessingService jsonProcessingService;

    public VEvent buildCalendarEvent(String currentUrl,
                                     Map<String, ApplicantAnswer> applicantAnswers,
                                     Map<String, Object> calculatedValues) {

        DocumentContext applicantAnswersContext = JsonPath.parse(jsonProcessingService.convertAnswersToJsonString(applicantAnswers));

        Map<String, String> componentsIDs = getComponentsIDs(applicantAnswers);
        Map<String, DateProperty> dates = getPropertyDates(componentsIDs.get("timeSlotId"), applicantAnswersContext);
        Map<String, String> propertyMap = calculateProperties(componentsIDs, applicantAnswersContext, calculatedValues);

        PropertyList<Property> properties = new PropertyList<>();

        properties.add(new DtStamp(new DateTime()));
        properties.add(dates.get("startDate"));
        properties.add(dates.get("endDate"));
        properties.add(createOrganizer());
        properties.add(new Summary(propertyMap.get("serviceName")));
        properties.add(new Location(propertyMap.get("location")));
        properties.add(new RandomUidGenerator().generateUid());
        properties.add(new Description(String.format(DESCRIPTION, propertyMap.get("serviceName"), propertyMap.get("dateTimeToPrint"),
                propertyMap.get("department"), propertyMap.get("location"), currentUrl, propertyMap.get("bookId"))));
        properties.add(new Sequence(1));
        properties.add(new XProperty("X-ALT-DESC", getFmtType(), String.format(HTML_DESCRIPTION, propertyMap.get("serviceName"),
                propertyMap.get("dateTimeToPrint"), propertyMap.get("department"), propertyMap.get("location"), currentUrl, propertyMap.get("bookId"))));

        return new VEvent(properties);
    }

    private ParameterList getFmtType() {
        ParameterList parameterList = new ParameterList();
        parameterList.add(new FmtType("text/html"));
        return parameterList;
    }

    private Map<String, String> calculateProperties(Map<String, String> componentsIDs, DocumentContext applicantAnswersContext,
                                                    Map<String, Object> calculatedValues) {

        Map<String, String> propertyMap = new HashMap<>();

        String dateTime = jsonProcessingService.getFieldFromContext(String.format(DATE_TIME_TO_PRINT_VALUE, componentsIDs.get("timeSlotId")), applicantAnswersContext, String.class);
        String dateTimeToPrint = LocalDateTime.parse(dateTime).format(DATE_TIME_FORMATTER);
        String department = calculateDepartment(componentsIDs.get("mapServiceId"), componentsIDs.get("medRefId"), componentsIDs.get("refNumId"), applicantAnswersContext);
        String bookId = jsonProcessingService.getFieldFromContext(String.format(BOOK_ID_VALUE, componentsIDs.get("timeSlotId")), applicantAnswersContext, String.class);
        String location = calculateLocation(componentsIDs.get("mapServiceId"), componentsIDs.get("timeSlotId"), applicantAnswersContext);

        String serviceName = serviceDescriptor.getService();
        if (calculatedValues != null && StringUtils.hasText((String) calculatedValues.get(SUBJECT))) {
            serviceName = (String) calculatedValues.get(SUBJECT);
        }

        propertyMap.put("dateTimeToPrint", dateTimeToPrint);
        propertyMap.put("department", department);
        propertyMap.put("bookId", bookId);
        propertyMap.put("location", location);
        propertyMap.put("serviceName", serviceName);

        return propertyMap;
    }

    private Organizer createOrganizer() {
        ParameterList parameterList = new ParameterList();
        parameterList.add(new Cn("Госуслуги"));
        Organizer organizer;
        try {
            organizer = new Organizer(parameterList, "mailto:no-reply@gosuslugi.ru");
        } catch (URISyntaxException e) {
            throw new ValidationException("Incorrect email gosuslugi");
        }
        return organizer;
    }

    private Map<String, String> getComponentsIDs(Map<String, ApplicantAnswer> applicantAnswers) {

        Map<String, String> componentsIDs = new HashMap<>();

        Map<ComponentType, FieldComponent> fieldsForCalendarMap = getComponentsForFillingCalendar(applicantAnswers);

        String timeSlotId = fieldsForCalendarMap.entrySet().stream()
                .filter(entry -> ComponentType.TimeSlot.equals(entry.getKey())
                        || ComponentType.TimeSlotWithComputableDepartment.equals(entry.getKey())
                        || ComponentType.TimeSlotDoctor.equals(entry.getKey()))
                .findFirst()
                .map(Map.Entry::getValue)
                .map(FieldComponent::getId)
                .orElseThrow(() -> new ValidationException("TimeSlot or TimeSlotWithComputableDepartment or TimeSlotDoctor component is missing"));

        String mapServiceId = Optional.ofNullable(fieldsForCalendarMap.get(ComponentType.MapService)).map(FieldComponent::getId).orElse(null);
        String medRefId = Optional.ofNullable(fieldsForCalendarMap.get(ComponentType.MedicalReferrals)).map(FieldComponent::getId).orElse(null);
        String refNumId = Optional.ofNullable(fieldsForCalendarMap.get(ComponentType.ReferralNumber)).map(FieldComponent::getId).orElse(null);

        componentsIDs.put("timeSlotId", timeSlotId);
        componentsIDs.put("mapServiceId", mapServiceId);
        componentsIDs.put("medRefId", medRefId);
        componentsIDs.put("refNumId", refNumId);

        return componentsIDs;
    }

    private Map<ComponentType, FieldComponent> getComponentsForFillingCalendar(Map<String, ApplicantAnswer> applicantAnswers) {
        return applicantAnswers.entrySet().stream()
                .filter(entry -> entry.getValue().getVisited() != null)
                .map(entry -> serviceDescriptor.getFieldComponentById(entry.getKey())
                        .filter(fieldComponent -> ComponentType.MapService.equals(fieldComponent.getType())
                                || ComponentType.TimeSlot.equals(fieldComponent.getType())
                                || ComponentType.TimeSlotWithComputableDepartment.equals(fieldComponent.getType())
                                || ComponentType.TimeSlotDoctor.equals(fieldComponent.getType())
                                || ComponentType.MedicalReferrals.equals(fieldComponent.getType())
                                || ComponentType.ReferralNumber.equals(fieldComponent.getType()))
                        .orElse(null))
                .filter(Objects::nonNull)
                .map(FieldComponent::getCopy)
                .collect(Collectors.toMap(FieldComponent::getType, Function.identity()));
    }

    private Map<String, DateProperty> getPropertyDates(String timeSlotId, DocumentContext applicantAnswersContext) {
        Map<String, DateProperty> dates = new HashMap<>();
        DtStart startDate;
        DtEnd endDate;
        try {
            String vDateTimeUtc = getVDateTimeUtc(
                    jsonProcessingService.getFieldFromContext(String.format(VISIT_TIME_ISO_VALUE, timeSlotId), applicantAnswersContext, String.class)
            );
            startDate = new DtStart(vDateTimeUtc);
            endDate = new DtEnd(vDateTimeUtc);
        } catch (ParseException e) {
            throw new ValidationException("Incorrect start date for service (event calendar)");
        }
        dates.put("startDate", startDate);
        dates.put("endDate", endDate);
        return dates;
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

    private String calculateLocation(String mapServiceId, String timeSlotId, DocumentContext applicantAnswersContext) {

        if (StringUtils.hasText(mapServiceId)) {
            return Stream.of(ADDRESS_VALUE, ADDRESS_UPPERCASE_VALUE, ADDRESS_OUT_UPPERCASE_VALUE, ADDRESS_MO_VALUE)
                    .map(value -> String.format(value, mapServiceId))
                    .map(field -> jsonProcessingService.getFieldFromContext(field, applicantAnswersContext, String.class))
                    .filter(StringUtils::hasText)
                    .findFirst()
                    .orElse(DEFAULT_VALUE);
        }
        return Optional.ofNullable(jsonProcessingService.getFieldFromContext(String.format(ADDRESS_MO_DOC_LOOKUP_VALUE, timeSlotId), applicantAnswersContext, String.class))
                .orElse(DEFAULT_VALUE);
    }

    private String calculateDepartment(String mapServiceId, String medRefId, String refNumId, DocumentContext applicantAnswersContext) {
        String departmentField = DEFAULT_VALUE;
        if (StringUtils.hasText(mapServiceId)) {
            departmentField = String.format(DEPARTMENT_VALUE, mapServiceId);
        } else if (StringUtils.hasText(refNumId)) {
            departmentField = String.format(DEPARTMENT_REF_NUMBER_VALUE, refNumId);
        } else if (StringUtils.hasText(medRefId)) {
            departmentField = String.format(DEPARTMENT_MED_REF_VALUE, medRefId);
        }
        return StringUtils.hasText(departmentField)
                ? Optional.ofNullable(jsonProcessingService.getFieldFromContext(departmentField, applicantAnswersContext, String.class)).orElse(DEFAULT_VALUE)
                : DEFAULT_VALUE;
    }
}
