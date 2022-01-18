package ru.gosuslugi.pgu.fs.component.userdata.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import lombok.EqualsAndHashCode;
import ru.gosuslugi.pgu.pgu_common.gibdd.dto.VehicleFullInfo;

@EqualsAndHashCode(callSuper = true)
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class CarDetailInfoComponentDto extends CarInfoComponentDto {
    private VehicleFullInfo vehicleInfo;
}
