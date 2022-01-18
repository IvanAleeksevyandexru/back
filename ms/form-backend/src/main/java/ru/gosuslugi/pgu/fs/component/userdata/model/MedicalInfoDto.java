package ru.gosuslugi.pgu.fs.component.userdata.model;

import lombok.Builder;
import lombok.Data;
import ru.gosuslugi.pgu.fs.component.medicine.model.MedDictionaryResponse;

import java.util.List;
import java.util.Map;

@Data
@Builder
public class MedicalInfoDto {
    private Integer smevVersion;
    private String sessionId;
    private String eserviceId;
    private MedDictionaryResponse medicalData;
    private List<Map<String, String>> bookAttributes;

    /**
     * Данные для бронивания таймслота - запись на прием к врачу
     * @see <a href="https://jira.egovdev.ru/browse/EPGUCORE-59196">EPGUCORE-59196</a>
     */
    public void fillBookAttributes() {
            bookAttributes = List.of(Map.of(
                    "name", "Session_Id",
                    "value", sessionId
            ));
    }
}
