package ru.gosuslugi.pgu.fs.component.confirm.internal;

import ru.atc.carcass.security.rest.model.person.PersonDoc;

import java.util.Map;

import static ru.gosuslugi.pgu.components.ComponentAttributes.*;

/**
 * Пасспорт гражданина РФ
 */
public final class PassportDoc extends PersonDoc {
    /**
     * Создание из мапы с данными
     * @param externalData данные для заполнения
     */
    public PassportDoc(Map<String, Object> externalData) {
        String passportSeries = externalData.getOrDefault(RF_PASSPORT_SERIES_ATTR, "").toString();
        String passportNumber = externalData.getOrDefault(RF_PASSPORT_NUMBER_ATTR, "").toString();
        String issueDate = externalData.getOrDefault(RF_PASSPORT_ISSUE_DATE_ATTR, "").toString();
        String issueBy = externalData.getOrDefault(RF_PASSPORT_ISSUED_BY_ATTR, "").toString();
        String issueId = externalData.getOrDefault(RF_PASSPORT_ISSUED_BY_ID_ATTR, "").toString();

        this.setType(RF_PASSPORT_ATTR);
        this.setSeries(passportSeries);
        this.setNumber(passportNumber);
        this.setIssueDate(issueDate);
        this.setIssuedBy(issueBy);
        this.setIssueId(issueId);
        this.setVrfStu(VERIFIED_ATTR);
    }
}
