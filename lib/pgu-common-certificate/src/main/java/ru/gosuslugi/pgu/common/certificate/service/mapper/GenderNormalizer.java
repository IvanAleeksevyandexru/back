package ru.gosuslugi.pgu.common.certificate.service.mapper;

import ru.gosuslugi.pgu.common.certificate.dto.PersonCertificateEaisdoRequestGenderType;

import java.util.Map;

public class GenderNormalizer {
    public static String normalizeGenderType(String genderType) {
        String male = PersonCertificateEaisdoRequestGenderType.Male.name();
        String female = PersonCertificateEaisdoRequestGenderType.Female.name();
        Map<String, String> genderMap = Map.of("M", male,
                "М"/*русская М*/, male,
                "F", female,
                "Ж", female);
        return genderMap.getOrDefault(String.valueOf(genderType.toUpperCase().charAt(0)), "");
    }

}
