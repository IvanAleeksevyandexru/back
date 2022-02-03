package ru.gosuslugi.pgu.fs.pgu.client;


import ru.gosuslugi.pgu.fs.pgu.dto.medicalBirthCertificate.MedicalBirthCertificate;

import java.util.List;

/**
 * Клиент для получения мед. свидетельств
 */
public interface PguMedicalBirthCertificatesClient {
    List<MedicalBirthCertificate> getMedicalBirthCertificates(String token, Long oid);
}