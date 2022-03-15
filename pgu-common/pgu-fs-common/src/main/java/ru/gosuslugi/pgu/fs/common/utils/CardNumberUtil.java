package ru.gosuslugi.pgu.fs.common.utils;

import org.apache.commons.lang3.StringUtils;

public class CardNumberUtil {
    /**
     * Алгоритм Луна верификации правильности ввода номеров карт с учётом 19-значных карт МИР.
     *
     * @param cardNumber номер карты без пробелов
     * @return результат верификации
     * @see <a href="https://www.geeksforgeeks.org/luhn-algorithm/">Luhn algorithm</a>
     */
    public static boolean checkLuhn(String cardNumber) {
        int sum = 0;
        String digits = StringUtils.getDigits(cardNumber);
        int length = digits.length();
        boolean isEven = length % 2 == 0;
        for (int i = 0; i < length; i++) {
            int cardNum = digits.charAt(i) - '0';
            if (isEven ? i % 2 == 0 : i % 2 == 1) {
                cardNum = cardNum * 2;
                if (cardNum > 9) {
                    cardNum = cardNum - 9;
                }
            }
            sum += cardNum;
        }
        return sum % 10 == 0;
    }
}
