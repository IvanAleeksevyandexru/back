package ru.gosuslugi.pgu.components.descriptor.converter;

import ru.gosuslugi.pgu.common.core.date.util.DateUtil;

import java.util.Map;

public class DateToAgeConverter implements Converter {

    @Override
    public String convert(Object value, Map<String, Object> attrs) {
        return String.valueOf(DateUtil.calcAgeFromBirthDate(String.valueOf(value)));
    }
}
