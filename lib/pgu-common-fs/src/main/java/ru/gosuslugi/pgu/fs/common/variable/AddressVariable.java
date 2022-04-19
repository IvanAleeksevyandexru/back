package ru.gosuslugi.pgu.fs.common.variable;

import org.springframework.stereotype.Component;
import ru.gosuslugi.pgu.dto.ScenarioDto;
import ru.gosuslugi.pgu.dto.ServiceInfoDto;
import ru.gosuslugi.pgu.dto.UserRegionDto;

import java.util.Objects;

import static java.util.Objects.isNull;

@Component
public class AddressVariable extends AbstractVariable {

    @Override
    public String getValue(ScenarioDto scenarioDto) {
        ServiceInfoDto serviceInfo = isNull(scenarioDto) ? null : scenarioDto.getServiceInfo();
        UserRegionDto userRegion = isNull(serviceInfo) ? null : serviceInfo.getUserRegion();
        String regionPath = isNull(userRegion) ? null : userRegion.getPath();
        return Objects.toString(regionPath, "");
    }

    @Override
    public VariableType getType() {
        return VariableType.userAddress;
    }
}
