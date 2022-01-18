package ru.gosuslugi.pgu.fs.component.userdata.model;

import lombok.Data;
import ru.gosuslugi.pgu.pgu_common.gibdd.dto.ExternalServiceCallResult;
import ru.gosuslugi.pgu.pgu_common.gibdd.dto.VehicleFullInfo;

import java.util.List;

@Data
public class CarListComponentDto {
    /** Данные из Витрины ГИБДД */
    private List<VehicleFullInfo> vehicles;

    private ExternalServiceCallResult vehicleServiceCallResult = ExternalServiceCallResult.SUCCESS;
}
