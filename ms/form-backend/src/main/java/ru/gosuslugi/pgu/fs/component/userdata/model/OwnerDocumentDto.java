package ru.gosuslugi.pgu.fs.component.userdata.model;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * Документ
 */
@Data
@AllArgsConstructor
public class OwnerDocumentDto {
    private String seriesAndNumber;
    private String issueDate;
    private String documentType;
}
