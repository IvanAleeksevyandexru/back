package ru.gosuslugi.pgu.fs.component.userdata.model;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * Информация о владельце
 */
@Data
@AllArgsConstructor
public class OwnerDto {
    private String fullName;
    private OwnerDocumentDto document;
}
