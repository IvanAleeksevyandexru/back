package ru.gosuslugi.pgu.fs.utils;

import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;
import ru.gosuslugi.pgu.dto.ScenarioDto;
import ru.gosuslugi.pgu.dto.ServiceInfoDto;
import ru.gosuslugi.pgu.fs.common.exception.FormBaseException;

import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;

import static ru.gosuslugi.pgu.components.ComponentAttributes.NOT_CORRECT_CONDITION_PARSING;

/** Вспомогательный класс для сценария. */
@Slf4j
@UtilityClass
/** Вспомогательный класс для сценария. */
public class ScenarioFieldUtil {

    private final String QUERY_PARAMS = "serviceInfo.queryParams";

    /** Мапа, по имени поля возвращающая ссылку на соответствующий get-метод. */
    private final Map<String, Function<ScenarioDto, Object>> FIELD_TO_STRING_MAPPER = Map.ofEntries(
            Map.entry("serviceCode", ScenarioDto::getServiceCode),
            Map.entry("targetCode", ScenarioDto::getTargetCode),
            Map.entry("masterOrderId", ScenarioDto::getMasterOrderId),
            Map.entry("orderId", ScenarioDto::getOrderId),
            Map.entry("currentScenarioId", ScenarioDto::getCurrentScenarioId),
            Map.entry("serviceId", ScenarioDto::getServiceId),
            Map.entry("serviceDescriptorId", ScenarioDto::getServiceDescriptorId),
            Map.entry("targetId", ScenarioDto::getTargetId),
            Map.entry("currentUrl", ScenarioDto::getCurrentUrl),
            Map.entry("serviceInfo", ScenarioDto::getServiceInfo),
            Map.entry(QUERY_PARAMS,
                    scenarioDto -> Optional.ofNullable(scenarioDto.getServiceInfo()).map(ServiceInfoDto::getQueryParams).orElse(null)),
            Map.entry("serviceInfo.infSysCode",
                    scenarioDto -> Optional.ofNullable(scenarioDto.getServiceInfo()).map(ServiceInfoDto::getInfSysCode).orElse(null))
    );


    /**
     * Возвращает ссылку на get-метод по имени поля.
     * @param fieldPath имя поля или путь к имени через точку
     * @param scenarioDto сценарий
     * @return ссылка на метод
     */
    public String getValueByFieldName(String fieldPath, ScenarioDto scenarioDto) {
        Function<ScenarioDto, Object> objectFunction = FIELD_TO_STRING_MAPPER.get(fieldPath);
        if (objectFunction != null) {
            return objectFunction.apply(scenarioDto).toString();
        }
        int index = fieldPath.indexOf(QUERY_PARAMS);
        if (index > -1) {
            objectFunction = FIELD_TO_STRING_MAPPER.get(QUERY_PARAMS);
        if (objectFunction != null) {
                @SuppressWarnings("unchecked")
                Map<String, String> queryParams = (Map<String, String>) objectFunction.apply(scenarioDto);
                index += QUERY_PARAMS.length();
                String fieldName = fieldPath.substring(index + 1);
                if (Objects.nonNull(queryParams) && queryParams.containsKey(fieldName)) {
                    return queryParams.get(fieldName);
            }
        }
        }
        log.error("Ошибка во время формирования условия. fieldName = {}", fieldPath);
        throw new FormBaseException(NOT_CORRECT_CONDITION_PARSING);

    }
}
