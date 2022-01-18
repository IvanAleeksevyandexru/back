package ru.gosuslugi.pgu.fs.component.userdata;

import com.fasterxml.jackson.core.type.TypeReference;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import ru.gosuslugi.pgu.dto.ApplicantAnswer;
import ru.gosuslugi.pgu.common.core.json.JsonProcessingUtil;
import ru.gosuslugi.pgu.dto.descriptor.FieldComponent;
import ru.gosuslugi.pgu.dto.descriptor.types.ComponentType;
import ru.gosuslugi.pgu.components.descriptor.types.EmployeeHistory;
import ru.gosuslugi.pgu.fs.common.component.AbstractComponent;
import ru.gosuslugi.pgu.fs.common.component.validation.NotBlankValidation;
import ru.gosuslugi.pgu.fs.common.component.validation.ValidationRule;

import java.time.YearMonth;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static ru.gosuslugi.pgu.components.ComponentAttributes.NON_STOP_ATTR;
import static ru.gosuslugi.pgu.components.ComponentAttributes.YEARS_ATTR;
import static ru.gosuslugi.pgu.common.core.date.util.DateUtil.convertDateToNumber;
import static ru.gosuslugi.pgu.common.core.date.util.DateUtil.getYearMonthFromMonthNumber;

@Slf4j
@Component
public class EmployeeHistoryComponent extends AbstractComponent<String> {

    private static final List<String> TYPES_WITHOUT_POSITION = Arrays.asList("student", "unemployed");
    private static final String INVALID_CHARS_PATTERN = "[^a-zA-Zа-яА-ЯёЁ\\d\\s\\[\\]()?\\.\",#№:;\\-\\+/'*<>&\\\\]";

    @Override
    public ComponentType getType() {
        return ComponentType.EmployeeHistory;
    }

    @Override
    public List<ValidationRule> getValidations() {
        return List.of(new NotBlankValidation("Необходимо указать хотя бы одно значение"));
    }

    @Override
    protected void validateAfterSubmit(Map<String, String> incorrectAnswers, Map.Entry<String, ApplicantAnswer> entry, FieldComponent fieldComponent) {
        List<EmployeeHistory> activities = JsonProcessingUtil.fromJson(entry.getValue().getValue(), new TypeReference<>() {});
        validate(incorrectAnswers, fieldComponent, activities);
        if (incorrectAnswers.isEmpty()) {
            deleteIllegalChars(activities);
            List<EmployeeHistory> sortedList = sortEmployeeHistory(activities);
            String sortedActivitiesValue = JsonProcessingUtil.toJson(sortedList);
            entry.getValue().setValue(sortedActivitiesValue);
        }
    }

    public static Map<String, String> validate(Map<String, String> incorrectAnswers, FieldComponent fieldComponent, List<EmployeeHistory> activities) {
        Integer numberOfYears = Integer.valueOf(fieldComponent.getAttrs().getOrDefault(YEARS_ATTR, "10").toString());
        boolean nonStop = Boolean.parseBoolean(fieldComponent.getAttrs().getOrDefault(NON_STOP_ATTR, true).toString());
        for (int i = 0; i < activities.size(); i++) {
            EmployeeHistory activity = activities.get(i);
            String aType = activity.getType();
            String aPosition = activity.getPosition();
            if (aType == null || aType.isBlank()) {
                incorrectAnswers.put(String.format("%s.activities[%s].type", fieldComponent.getId(), i) , "Activity type cannot be null or blank");
            }
            if (activity.getFrom() == null) {
                incorrectAnswers.put(String.format("%s.activities[%s].from", fieldComponent.getId(), i) , "Date from cannot be null");
            }
            if (activity.getTo() == null) {
                incorrectAnswers.put(String.format("%s.activities[%s].to", fieldComponent.getId(), i) , "Date to cannot be null");
            }
            if(!TYPES_WITHOUT_POSITION.contains(aType) && (aPosition == null || aPosition.isBlank())) {
                incorrectAnswers.put(String.format("%s.activities[%s].position", fieldComponent.getId(), i), "Position cannot be null or blank");
            }
            if ((activity.getPlace() == null || activity.getPlace().isBlank()) && !"unemployed".equals(activity.getType())) {
                incorrectAnswers.put(String.format("%s.activities[%s].place", fieldComponent.getId(), i) , "Place cannot be null or blank");
            }
            if (activity.getAddress() == null || activity.getAddress().isBlank()) {
                incorrectAnswers.put(String.format("%s.activities[%s].address", fieldComponent.getId(), i) , "Address cannot be null or blank");
            }
        }

        if (nonStop) {
            List<String> missingPeriods = validateNonStopActivityForLastXYears(activities, numberOfYears);
            if (!missingPeriods.isEmpty()) {
                incorrectAnswers.put(fieldComponent.getId(), String.format("Missing periods: %s", String.join(", ", missingPeriods)));
            }
        }

        return incorrectAnswers;
    }

    public static List<EmployeeHistory> sortEmployeeHistory(List<EmployeeHistory> activities){
        return activities.stream()
                .sorted(Comparator.comparingInt(activity -> activity.getFrom().getYear()*12+activity.getFrom().getMonth()+1))
                .collect(Collectors.toList());
    }

    private static List<String> validateNonStopActivityForLastXYears(List<EmployeeHistory> activities, Integer numberOfYears) {
        YearMonth now = YearMonth.now();
        YearMonth xYearsBefore = now.minusYears(numberOfYears);
        List<Integer> listOfMonthsToCheck = createListForNumbers(convertDateToNumber(xYearsBefore), convertDateToNumber(now));
        for (EmployeeHistory activity: activities) {
            if (activity.getFrom() == null || activity.getTo() == null) {
                continue;
            }
            YearMonth yearMonthFrom = YearMonth.of(activity.getFrom().getYear(), activity.getFrom().getMonth()+1);
            YearMonth yearMonthTo = YearMonth.of(activity.getTo().getYear(), activity.getTo().getMonth()+1);
            List<Integer> activeMonths = createListForNumbers(convertDateToNumber(yearMonthFrom), convertDateToNumber(yearMonthTo));
            listOfMonthsToCheck.removeAll(activeMonths);
        }
        return getMissingPeriods(listOfMonthsToCheck);
    }

    private static List<String> getMissingPeriods(List<Integer> notWorkingMonths) {
        List<String> missingPeriods = new ArrayList<>();
        if (notWorkingMonths != null && !notWorkingMonths.isEmpty()) {
            Integer startMissingMonth = null;
            Integer previousMissingMonth = null;
            for (Integer month : notWorkingMonths) {
                if (startMissingMonth == null) {
                    previousMissingMonth = month;
                    startMissingMonth = month;
                    continue;
                }
                if (month - previousMissingMonth > 1) {
                    missingPeriods.add(String.format("%s — %s",
                            getYearMonthFromMonthNumber(startMissingMonth).toString(),
                            getYearMonthFromMonthNumber(previousMissingMonth).toString()));
                    startMissingMonth = month;
                }
                previousMissingMonth = month;
            }
            missingPeriods.add(String.format("%s — %s",
                    getYearMonthFromMonthNumber(startMissingMonth).toString(),
                    getYearMonthFromMonthNumber(previousMissingMonth).toString()));
        }
        return missingPeriods;
    }

    public static List<Integer> createListForNumbers(int from, int to) {
        return IntStream.range(from, to + 1).boxed().collect(Collectors.toList());
    }

    private void deleteIllegalChars(List<EmployeeHistory> activities) {
        for (EmployeeHistory activity: activities) {
            activity.setAddress(getClearedValue(activity.getAddress()));
            activity.setPosition(getClearedValue(activity.getPosition()));
            activity.setPlace(getClearedValue(activity.getPlace()));
        }
    }

    private static String getClearedValue(String initialValue) {
        return StringUtils.isEmpty(initialValue) ? initialValue : initialValue.replaceAll(INVALID_CHARS_PATTERN, "");
    }

}
