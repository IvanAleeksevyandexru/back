package ru.gosuslugi.pgu.fs.component.userdata.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import ru.gosuslugi.pgu.pgu_common.gibdd.dto.ExternalServiceCallResult;
import ru.gosuslugi.pgu.pgu_common.gibdd.dto.FederalNotaryInfo;
import ru.gosuslugi.pgu.pgu_common.gibdd.dto.VehicleInfo;

/**
 * Данные о ТС
 */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class CarInfoComponentDto {
    /** VIN по которому производился поиск */
    private String vin;
    /** Данные из Витрины ГИБДД */
    private VehicleInfo vehicleInfo;
    /** Данные из Нотариальной палаты */
    private FederalNotaryInfo notaryInfo;
    /** Дата получения данных из ГИБДД */
    private String approveDate;

    private ExternalServiceCallResult vehicleServiceCallResult = ExternalServiceCallResult.SUCCESS;
    private ExternalServiceCallResult notaryServiceCallResult = ExternalServiceCallResult.SUCCESS;
}