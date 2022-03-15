package ru.gosuslugi.pgu.fs.common.utils;

import ru.gosuslugi.pgu.fs.common.component.userdata.InnValidationHelper;

import java.util.List;
import java.util.regex.Pattern;

public class InnUtil {

    /**
     * Допустимая маска ввода XXXXXXXXXXXX или XXXX-XXXXXX-XX
     */
    public static final Pattern PERSON_INN_PATTERN = Pattern.compile("\\d{12}|\\d{4}-\\d{6}-\\d{2}");

    /**
     * Допустимая маска ввода XXXXXXXXXX или XXXX-XXXXX-X
     */
    public static final Pattern LEGAL_INN_PATTERN = Pattern.compile("\\d{10}|\\d{4}-\\d{5}-\\d");

    public static final List<Integer> personFactors1 = List.of(7, 2, 4, 10, 3, 5, 9, 4, 6, 8);
    public static final List<Integer> personFactors2 = List.of(3, 7, 2, 4, 10, 3, 5, 9, 4, 6, 8);

    public static final List<Integer> legalFactors = List.of(2, 4, 10, 3, 5, 9, 4, 6, 8);

    /**
     * Валидация контрольной суммы ИНН ЮЛ
     *
     * @param innString ИНН
     * @return false - валидация не пройдена
     */
    public static boolean personInnValidation(String innString) {
        List<Integer> figures = StringConvertHelper.getFiguresListFromString(innString);
        Integer firstSum = InnValidationHelper.getCheckSum(personFactors1, figures);
        int figureValue = InnValidationHelper.getFigureValue(firstSum, InnValidationHelper.INN_DIVIDER_VALUE);
        figureValue = figureValue == 10 ? 0 : figureValue;
        if (figures.get(10) != figureValue) return false;
        Integer secondSum = InnValidationHelper.getCheckSum(personFactors2, figures);
        int secondFigureValue = InnValidationHelper.getFigureValue(secondSum, InnValidationHelper.INN_DIVIDER_VALUE);
        return figures.get(11) == secondFigureValue | figures.get(11) == 0;
    }

    /**
     * Валидация контрольной суммы ИНН ЮЛ
     *
     * @param innString ИНН
     * @return fasle - валидация не пройдена
     */
    public static boolean legalInnValidation(String innString) {
        List<Integer> figures = StringConvertHelper.getFiguresListFromString(innString);
        Integer sum = InnValidationHelper.getCheckSum(legalFactors, figures);
        int tenthFigure = InnValidationHelper.getFigureValue(sum, InnValidationHelper.INN_DIVIDER_VALUE);
        tenthFigure = tenthFigure == 10 ? 0 : tenthFigure;
        return tenthFigure == figures.get(9);
    }


}
