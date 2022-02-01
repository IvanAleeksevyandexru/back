package ru.gosuslugi.pgu.fs.controller.data;

import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.gosuslugi.pgu.dto.ScenarioDto;
import ru.gosuslugi.pgu.dto.descriptor.FieldComponent;
import ru.gosuslugi.pgu.fs.component.userdata.CarDetailInfoComponent;
import ru.gosuslugi.pgu.fs.utils.TracingHelper;
import ru.gosuslugi.pgu.pgu_common.gibdd.dto.FederalNotaryInfo;
import ru.gosuslugi.pgu.pgu_common.gibdd.dto.GibddServiceResponse;
import ru.gosuslugi.pgu.pgu_common.gibdd.dto.VehicleFullInfo;

import java.util.Optional;

@RequestMapping(value = "/data/gibdd", produces = "application/json")
@RestController
@RequiredArgsConstructor
public class GibddController {

    private final CarDetailInfoComponent carDetailInfoComponent;
    private final TracingHelper tracingHelper;

    @Operation(summary = "Получение информации о ТС из ГИБДД по VIN, СТС и гос. номеру")
    @PostMapping(value = "/vehicleFullInfo")
    public GibddServiceResponse<VehicleFullInfo> getVehicleFullInfo(@RequestBody ScenarioDto scenarioDto) {
        tracingHelper.addServiceCodeAndOrderId(scenarioDto.getServiceCode(), scenarioDto);
        return carDetailInfoComponent.getVehicleInfo(getCurrentComponent(scenarioDto), false);
    }

    @Operation(summary = "Получение данных о ТС из Федеральной нотариальной палаты")
    @PostMapping(value = "/notaryInfo")
    public GibddServiceResponse<FederalNotaryInfo> getNotaryInfo(@RequestBody ScenarioDto scenarioDto) {
        tracingHelper.addServiceCodeAndOrderId(scenarioDto.getServiceCode(), scenarioDto);
        return carDetailInfoComponent.getFederalNotaryInfo(
                Optional.ofNullable(scenarioDto.getOrderId()).orElse(0L),
                getCurrentComponent(scenarioDto)
        );
    }

    private FieldComponent getCurrentComponent(ScenarioDto scenarioDto) {
        return scenarioDto.getDisplay().getComponents().get(0);
    }
}
