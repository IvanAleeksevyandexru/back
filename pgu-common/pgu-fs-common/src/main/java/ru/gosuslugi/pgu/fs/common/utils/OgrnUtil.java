package ru.gosuslugi.pgu.fs.common.utils;

import java.util.regex.Pattern;

public class OgrnUtil {

    public static final Pattern OGRN_PATTERN = Pattern.compile("\\d{15}|\\d-\\d{2}-\\d{2}-\\d{9}-\\d");
    public static final Pattern OGRNIP_PATTERN = Pattern.compile("\\d{13}|\\d-\\d{2}-\\d{2}-\\d{9}-\\d");

    /**
     * Значение делителя, используемое в алгоритме проверки ОГРН
     */
    public static final int OGRN_DIVIDER = 11;

    /**
     * Значение делителя, используемое в алгоритме проверки ОГРНИП
     */
    public static final int OGRNIP_DIVIDER = 13;

    /**
     * Проверка контрольной суммы ОГРН
     *
     * @param value ОГРН
     * @return false - валидация не пройдена
     */
    public static boolean checkOgrn(boolean ip, String value) {
        final var pattern = ip ? OGRNIP_PATTERN : OGRN_PATTERN;
        if (!pattern.matcher(value).matches()) {
            return false;
        }

        String digitsValue = StringConvertHelper.getDigitString(value);
        String checkValueString = digitsValue.substring(0, digitsValue.length() - 1);
        final long checkValue = Long.parseLong(checkValueString);
        final int lastFigure = Integer.parseInt(digitsValue.substring(digitsValue.length() - 1));

        final int divider = ip ? OGRNIP_DIVIDER : OGRN_DIVIDER;
        long resultFigure = checkValue % divider;
        // сравнивается только младший разряд
        resultFigure = resultFigure % 10;
        return resultFigure == lastFigure;
    }

}
