package ru.gosuslugi.pgu.fs.component.userdata.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import lombok.EqualsAndHashCode;
import ru.gosuslugi.pgu.pgu_common.gibdd.dto.VehicleFullInfo;

/**
 * Данные о транспортных средствах владельца
 */
@EqualsAndHashCode(callSuper = true)
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class CarOwnerInfoComponentDto extends CarInfoComponentDto {
    /** Данные из Витрины ГИБДД */
    private VehicleFullInfo vehicleInfo;
    /** Данные пользователя */
    private OwnerDto ownerInfo;
}
