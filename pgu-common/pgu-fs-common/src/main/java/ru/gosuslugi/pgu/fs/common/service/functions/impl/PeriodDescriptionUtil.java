package ru.gosuslugi.pgu.fs.common.service.functions.impl;

import java.time.LocalDate;
import java.util.List;

public class PeriodDescriptionUtil {

    private static final List<String> MONTHS_FROM = List.of(
            "января", "февраля", "марта", "апреля", "мая", "июня",
            "июля", "августа", "сентября", "октября", "ноября", "декабря"
    );

    private static final List<String> MONTHS_TO = List.of(
            "январь", "февраль", "март", "апрель", "май", "июнь",
            "июль", "август", "сентябрь", "октябрь", "ноябрь", "декабрь"
    );

    public static String createPeriodDescription(List<String> args) {
        LocalDate now = LocalDate.now();
        LocalDate lowerBoundOfPeriod = now.minusMonths(Long.parseLong(args.get(0)));
        LocalDate upperBoundOfPeriod = now.minusMonths(Long.parseLong(args.get(1)));

        int indexOfLowerBound = lowerBoundOfPeriod.getMonthValue() - 1;
        int indexOfUpperBound = upperBoundOfPeriod.getMonthValue() - 1;

        return "с " + MONTHS_FROM.get(indexOfLowerBound) + " " + lowerBoundOfPeriod.getYear()
                + " года по " + MONTHS_TO.get(indexOfUpperBound) + " " + upperBoundOfPeriod.getYear() + " года";
    }
}
