package ru.gosuslugi.pgu.components.descriptor.converter;

import lombok.RequiredArgsConstructor;

import java.util.function.Supplier;

/**
 * Список конверторов
 */
@RequiredArgsConstructor
public enum ConverterType {
    REPLACE(StringReplaceConverter::new),
    DATE(DateConverter::new),
    UNIXDATE(UnixDateConverter::new),
    SPEL(SpelConverter::new),
    DATE_TO_AGE(DateToAgeConverter::new),
    MONTHS_FOR_TODAY(MonthsForTodayConverter::new),
    TO_UNIXDATE(ToUnixDateConverter::new);

    private final Supplier<Converter> converterSupplier;

    public Converter getConverter() {
        return converterSupplier.get();
    }
}
