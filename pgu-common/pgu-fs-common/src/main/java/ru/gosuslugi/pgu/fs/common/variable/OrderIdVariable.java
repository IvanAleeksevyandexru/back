package ru.gosuslugi.pgu.fs.common.variable;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.gosuslugi.pgu.dto.ScenarioDto;

@Slf4j
@Component
@RequiredArgsConstructor
public class OrderIdVariable extends AbstractVariable {

    @Override
    public String getValue(ScenarioDto scenarioDto) {
        return String.valueOf(scenarioDto.getOrderId());
    }

    @Override
    public VariableType getType() {
        return VariableType.orderId;
    }
}
