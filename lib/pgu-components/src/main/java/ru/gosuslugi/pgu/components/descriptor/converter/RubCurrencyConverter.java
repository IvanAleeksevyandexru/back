package ru.gosuslugi.pgu.components.descriptor.converter;

import lombok.extern.slf4j.Slf4j;
import org.springframework.util.StringUtils;

import java.math.RoundingMode;
import java.text.NumberFormat;
import java.util.Locale;
import java.util.Map;

@Slf4j
public class RubCurrencyConverter implements Converter {
    private static final String ROUND_FLOOR_ATTR = "roundFloor";
    private static final String EXCLUDE_SYMBOL_ATTR = "excludeSymbol";

    @Override
    public String convert(Object value, Map<String, Object> attrs) {
        if (StringUtils.isEmpty(value)) {
            return "";
        }
        try {
            var formatter = NumberFormat.getCurrencyInstance(new Locale("ru", "RU"));
            if (attrs.containsKey(ROUND_FLOOR_ATTR) && Boolean.TRUE.equals(attrs.get(ROUND_FLOOR_ATTR))) {
                formatter.setMaximumFractionDigits(0);
                formatter.setRoundingMode(RoundingMode.DOWN);
            }

            var formattedValue = formatter.format(Double.valueOf(value.toString()));

            if (attrs.containsKey(EXCLUDE_SYMBOL_ATTR) && Boolean.TRUE.equals(attrs.get(EXCLUDE_SYMBOL_ATTR))) {
                return formattedValue.replace("\u00A0₽", "");
            }

            return formattedValue;
        } catch (IllegalArgumentException  e) {
            log.warn("Error parsing \"{}\" to double. Details: {}", value, e.getMessage());
        }
        return value.toString();
    }
}
