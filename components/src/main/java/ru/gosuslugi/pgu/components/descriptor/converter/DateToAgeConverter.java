package ru.gosuslugi.pgu.components.descriptor.converter;

import lombok.extern.slf4j.Slf4j;
import ru.gosuslugi.pgu.common.core.date.util.DateUtil;

import java.util.Map;

@Slf4j
public class DateToAgeConverter implements Converter {

    @Override
    public String convert(Object value, Map<String, Object> attrs) {
        return String.valueOf(DateUtil.calcAgeFromBirthDate(String.valueOf(value)));
    }

}
