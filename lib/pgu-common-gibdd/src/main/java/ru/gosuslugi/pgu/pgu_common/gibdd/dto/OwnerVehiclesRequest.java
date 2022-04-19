package ru.gosuslugi.pgu.pgu_common.gibdd.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class OwnerVehiclesRequest {
    /** Фамилия */
    private String lastName;
    /** Имя */
    private String firstName;
    /** Дата рождения */
    private String birthDay;
    /** Дата рождения */
    private String userType;
    /** Тип документа */
    private OwnerDocumentType documentType ;
    /** Серия и номер документа */
    private String documentNumSer ;
    /** Идентификатор транзакции */
    private String tx;
}
