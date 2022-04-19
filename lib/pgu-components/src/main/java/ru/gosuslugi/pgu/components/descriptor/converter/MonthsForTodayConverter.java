package ru.gosuslugi.pgu.components.descriptor.converter;

import org.springframework.util.StringUtils;
import ru.gosuslugi.pgu.common.core.date.util.DateUtil;

import java.util.Map;

public class MonthsForTodayConverter implements Converter {

    @Override
    public String convert(Object value, Map<String, Object> attrs) {
        if (StringUtils.isEmpty(value))
            return "";
        int monthCount = DateUtil.calcMonthsForToday(value.toString());
        return String.valueOf(monthCount);
    }

}
