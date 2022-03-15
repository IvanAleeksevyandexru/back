package ru.gosuslugi.pgu.fs.common.utils;

import ru.gosuslugi.pgu.components.ComponentAttributes;
import ru.gosuslugi.pgu.dto.ApplicantAnswer;
import ru.gosuslugi.pgu.dto.ScenarioDto;
import ru.gosuslugi.pgu.dto.cycled.CycledApplicantAnswer;
import ru.gosuslugi.pgu.dto.cycled.CycledApplicantAnswerItem;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class SnilsUtil {

    public static final Pattern SNILS_PATTERN = Pattern.compile("\\d{3}-\\d{3}-\\d{3}[ ]{1}\\d{2}");
    public static final String REPEATED_CHILDREN_SNILS_ERROR_MESSAGE = "У детей не могут быть одинаковые СНИЛС";

    public static boolean isMatches(String value) {
        return SNILS_PATTERN.matcher(value).matches();
    }

    /**
     * Валидация контрольной суммы СНИЛС
     *
     * @param value СНИЛС
     * @return false - валидация не пройдкна
     */
    public static boolean checkSumValidation(String value) {
        List<Integer> snilsFigures = StringConvertHelper.getFiguresListFromString(value);
        if (snilsFigures.size() != 11) {
            return false;
        }
        List<Integer> checkFigures = snilsFigures.subList(0, 9);
        Long checkedValue = Long.parseLong(checkFigures.stream().map(String::valueOf).collect(Collectors.joining()));
        if (checkedValue <= 1001998) return true;
        List<Integer> multipleFigures = new ArrayList<>(9);
        for (int i = 0; i < checkFigures.size(); i++) {
            multipleFigures.add((9 - i) * checkFigures.get(i));
        }
        int sumToCheck = multipleFigures.stream().mapToInt(Integer::intValue).sum();
        int calcValue = 0;
        if (sumToCheck < 100) {
            calcValue = sumToCheck;
        }
        if (sumToCheck > 100) {
            calcValue = sumToCheck % 101;
            if (calcValue == 100) {
                calcValue = 0;
            }
        }
        String controlValue = snilsFigures.subList(9, 11).stream().map(String::valueOf).collect(Collectors.joining());
        return String.format("%02d", calcValue).equals(controlValue);
    }


    public static boolean checkRepeatedChildrenSnils(String componentId, String snils, ScenarioDto scenarioDto) {

        List<CycledApplicantAnswer> answers = scenarioDto.getCycledApplicantAnswers().getAnswers();
        for (final var answer: answers) {

            List<CycledApplicantAnswerItem> items = answer.getItems();                      // дети
            for (final var item: items) {

                ApplicantAnswer applicantAnswer = item.getItemAnswers().get(componentId);
                if (applicantAnswer == null) {
                    continue;
                }

                Map<String, String> valueMap = (Map<String, String>) AnswerUtil.tryParseToMap(applicantAnswer.getValue());
                String childSnils = valueMap.get(ComponentAttributes.SNILS);
                if (snils.equals(childSnils)) {
                    return false;
                }

            }
        }
        return true;
    }
}
