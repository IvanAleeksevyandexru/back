package ru.gosuslugi.pgu.pgu_common.gibdd.dto;

import lombok.Builder;
import lombok.Data;
import lombok.Setter;

/**
 * Запрос для получения данных о ТС из Витрины ГИБДД
 */
@Data
@Builder
@Setter
public class VehicleInfoRequest {
    /** Фамилия */
    private String lastName;
    /** Имя */
    private String firstName;
    /** Отчество */
    private String middleName;
    /** VIN */
    private String vin;
    /** СТС */
    private String sts;
    /** Государственный регистрационный номер */
    private String govRegNumber;
    /** Идентификатор транзакции */
    private String tx;
    /** Идентификатор присутствия конфиденциальных данных */
    private boolean hasSensitiveData;
}
