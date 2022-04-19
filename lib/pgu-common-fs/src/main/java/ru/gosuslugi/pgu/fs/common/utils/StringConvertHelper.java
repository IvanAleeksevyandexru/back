package ru.gosuslugi.pgu.fs.common.utils;

import java.util.List;
import java.util.stream.Collectors;

public class StringConvertHelper {

    /**
     * Получить цифры из строки в виде списка чисел
     *
     * @param stringWithNumbers строка, содержащая цифры
     * @return список цифр, приведенных к Integer
     */
    public static List<Integer> getFiguresListFromString(String stringWithNumbers) {
        return stringWithNumbers.chars()
                .filter(Character::isDigit)
                .mapToObj(Character::getNumericValue)
                .collect(Collectors.toList());
    }

    public static String getDigitString(String stringWithNumbers) {
        return stringWithNumbers.chars()
                .filter(Character::isDigit)
                .mapToObj(c -> String.valueOf(Character.getNumericValue(c)))
                .collect(Collectors.joining());
    }
}
