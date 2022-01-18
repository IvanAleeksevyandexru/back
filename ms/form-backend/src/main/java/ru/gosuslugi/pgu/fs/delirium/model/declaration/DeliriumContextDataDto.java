package ru.gosuslugi.pgu.fs.delirium.model.declaration;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class DeliriumContextDataDto {

    /**
     * Код услуги в ПГУ
     */
    private String serviceCode;

    /**
     * Код цели обращения в ПГУ
     */
    private String targetCode;
    
    /**
     * Принято или отклонено заявление
     * Если со-заявитель или согласователь отклоняет заявление, то здесь будет false
     * во всех остальных случаях - true
     */
    private boolean accept;

    /**
     * Событие для делириум по нажатию кнопки на экране
     * Если action не пустой, делириум не учитывает флаг accept
     */
    private String action;

    /**
     * Список всех участников заявления
     */
    private List<DeliriumApplicantDto> applicants = new ArrayList<>();

    public void addApplicant(DeliriumApplicantDto applicant) {
        applicants.add(applicant);
    }
    
}
