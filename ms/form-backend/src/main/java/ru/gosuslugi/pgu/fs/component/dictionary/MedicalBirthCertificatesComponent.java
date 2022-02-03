package ru.gosuslugi.pgu.fs.component.dictionary;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.gosuslugi.pgu.common.core.json.JsonProcessingUtil;
import ru.gosuslugi.pgu.common.esia.search.dto.UserPersonalData;
import ru.gosuslugi.pgu.dto.ScenarioDto;
import ru.gosuslugi.pgu.dto.descriptor.FieldComponent;
import ru.gosuslugi.pgu.dto.descriptor.types.ComponentType;
import ru.gosuslugi.pgu.fs.common.component.AbstractComponent;
import ru.gosuslugi.pgu.fs.common.component.ComponentResponse;
import ru.gosuslugi.pgu.fs.pgu.client.PguMedicalBirthCertificatesClient;
import ru.gosuslugi.pgu.fs.pgu.dto.medicalBirthCertificate.MedicalBirthCertificate;
import ru.gosuslugi.pgu.fs.pgu.dto.medicalBirthCertificate.NewBornInfo;
import ru.gosuslugi.pgu.fs.service.DictionaryListPreprocessorService;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static ru.gosuslugi.pgu.components.ComponentAttributes.ORIGINAL_ITEM;
import static ru.gosuslugi.pgu.components.ComponentAttributes.VALUE_ATTR;

@Slf4j
@Component
@RequiredArgsConstructor
public class MedicalBirthCertificatesComponent extends AbstractComponent<String> {

    private final PguMedicalBirthCertificatesClient medicalBirthCertificatesClient;
    private final UserPersonalData userPersonalData;
    private final DictionaryListPreprocessorService dictionaryListPreprocessorService;

    @Override
    public ComponentType getType() {
        return ComponentType.MedicalBirthCertificates;
    }

    @Override
    public ComponentResponse<String> getInitialValue(FieldComponent fieldComponent, ScenarioDto scenarioDto) {
        List<MedicalBirthCertificate> certs = getMedicalBirthCerts();
        String parsedCerts = JsonProcessingUtil.toJson(certs);
        dictionaryListPreprocessorService.prepareDictionaryListFromComponent(fieldComponent, scenarioDto, parsedCerts);
        return ComponentResponse.of(parsedCerts);
    }

    private List<MedicalBirthCertificate> getMedicalBirthCerts() {
        List<MedicalBirthCertificate> certs = medicalBirthCertificatesClient.getMedicalBirthCertificates(
                userPersonalData.getToken(), userPersonalData.getUserId());
        processBirthDate(certs);
        return certs;
    }

    private void processBirthDate(List<MedicalBirthCertificate> certs) {
        for (MedicalBirthCertificate cert : certs) {
            NewBornInfo newBornInfo = cert.getNewBornInfo();
            String[] dateTime = newBornInfo.getBirthDate().trim().split("\\s");
            if (dateTime.length == 2) {
                newBornInfo.setBirthDate(dateTime[0]);
                newBornInfo.setBirthTime(dateTime[1]);
            }
        }
    }

    @Override
    protected void validateAfterSubmit(Map<String, String> incorrectAnswers, String key, String value) {
        Map<String, Object> answer = jsonProcessingService.fromJson(value, LinkedHashMap.class);
        Map<String, Object> originalItem = (Map<String, Object>) answer.get(ORIGINAL_ITEM);
        MedicalBirthCertificate chosenCert = jsonProcessingService.fromJson(JsonProcessingUtil.toJson(originalItem.get(VALUE_ATTR)), MedicalBirthCertificate.class);
        List<MedicalBirthCertificate> certs = getMedicalBirthCerts();
        if (certs.stream().filter(cert -> cert.equals(chosenCert)).findFirst().isEmpty()) {
            incorrectAnswers.put(key, "Выбранное свидетельство о рождении не существует.");
        }
    }
}