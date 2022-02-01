package ru.gosuslugi.pgu.fs.common.variable;


import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.gosuslugi.pgu.dto.ScenarioDto;
import ru.gosuslugi.pgu.dto.ServiceInfoDto;
import ru.gosuslugi.pgu.dto.UserRegionDto;
import ru.gosuslugi.pgu.fs.common.exception.ConfigurationException;

import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

import static java.util.Objects.isNull;
import static ru.gosuslugi.pgu.common.core.logger.LoggerUtil.error;

@Slf4j
@Component
public class VariableRegistry {

    private ConcurrentHashMap<VariableType, Variable> variableRegistry = new ConcurrentHashMap<>();

    public VariableRegistry() {
        this.variableRegistry.put(USER_REGION_CODE.getType(), USER_REGION_CODE);
    }

    public void register(Variable variable) {
        VariableType variableType = variable.getType();
        Variable oldVariable = variableRegistry.putIfAbsent(variableType, variable);
        if(Objects.nonNull(oldVariable)){
            String errorMessage = String.format(
                    "Переменная %s уже зарегистрирована: %s",
                    variable.getClass().getCanonicalName(),
                    oldVariable.getClass().getCanonicalName());
            error(log, () -> errorMessage);
            throw new ConfigurationException(errorMessage);
        }
    }

    public Variable getVariable(VariableType variableType){
        return variableRegistry.get(variableType);
    }

    private static Variable USER_REGION_CODE = new Variable() {

        public static final String REGION_LEVEL_OKATO_SUFFIX = "000000000";

        @Override
        public String getValue(ScenarioDto scenarioDto) {
            ServiceInfoDto serviceInfo = isNull(scenarioDto) ? null : scenarioDto.getServiceInfo();
            UserRegionDto userRegion = isNull(serviceInfo) ? null : serviceInfo.getUserRegion();
            if (isNull(userRegion) || isNull(userRegion.getCodes()))
                return "";

            return userRegion.getCodes().stream()
                    .filter(code -> code.endsWith(REGION_LEVEL_OKATO_SUFFIX))
                    .findFirst().orElse("");
        }

        @Override
        public VariableType getType() {
            return VariableType.userRegionCode;
        }
    };
}
