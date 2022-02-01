package ru.gosuslugi.pgu.pgu_common.gibdd.util;

import org.springframework.util.StringUtils;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

public class VehicleInfoMapperUtil {

    public static final String RESTRICTION_STATUS_O = "снято";
    public static final String RESTRICTION_STATUS_1 = "действует";
    public static final String WHEEL_LOCATION_1 = "Левостороннее";
    public static final String WHEEL_LOCATION_2 = "Правостороннее";
    public static final String TRANSMISSION_TYPE_1 = "Механическая";
    public static final String TRANSMISSION_TYPE_2 = "Автоматическая";

    private static final Map<String, String> DRIVE_TYPE_TO_DESC = new HashMap<>() {{
        put("1", "Передний");
        put("2", "Задний");
        put("3", "Полный");
        put("4", "Прочее");
    }};


    private static final Map<Character, Character> SYMBOLS = new HashMap<>() {{
        put('А', 'A');
        put('В', 'B');
        put('Е', 'E');
        put('К', 'K');
        put('М', 'M');
        put('Н', 'H');
        put('О', 'O');
        put('Р', 'P');
        put('С', 'C');
        put('Т', 'T');
        put('У', 'Y');
        put('Х', 'X');
    }};

    public static String getRestrictionStatusAsText(String status) {
        if (StringUtils.isEmpty(status)) {
            return "";
        }

        String textStatus = status;
        if ("0".equals(status)) {
            textStatus = RESTRICTION_STATUS_O;
        } else if ("1".equals(status)) {
            textStatus = RESTRICTION_STATUS_1;
        }

        return textStatus;
    }

    public static String getFormattedNumber(String number) {
        if (StringUtils.isEmpty(number) || number.trim().length() != 10) {
            return number;
        }

        number = number.trim();

        return number.substring(0, 4) + " " + number.substring(4);
    }

    public static String convertToLatin(String value) {
        return value.toUpperCase().chars()
                .mapToObj(i -> ((char) i))
                .map(it -> SYMBOLS.getOrDefault(it, it).toString())
                .collect(Collectors.joining());
    }

    public static String getWheelLocationDesc(String value) {
        return formatValue(value, WHEEL_LOCATION_1, WHEEL_LOCATION_2);
    }

    public static String getTransmissionType(String value) {
        return formatValue(value, TRANSMISSION_TYPE_1, TRANSMISSION_TYPE_2);
    }

    public static String getDriveUnitType(String value) {
        return DRIVE_TYPE_TO_DESC.get(value);
    }

    public static String getLeasingFlagAsText(String value) {
        if ("0".equals(value) || "false".equals(value)) {
            return "Нет";
        }
        return "Да";
    }

    private static String formatValue(String value, String option1, String option2) {
        if (StringUtils.isEmpty(value)) {
            return value;
        }

        value = value.trim();
        if (value.equals("1")) {
            return option1;
        } else if (value.equals("2")) {
            return option2;
        }

        return value;
    }
}
