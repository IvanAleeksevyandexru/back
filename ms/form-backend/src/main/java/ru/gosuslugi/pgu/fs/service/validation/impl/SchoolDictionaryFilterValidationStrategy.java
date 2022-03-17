package ru.gosuslugi.pgu.fs.service.validation.impl;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.gosuslugi.pgu.dto.ApplicantAnswer;
import ru.gosuslugi.pgu.dto.ScenarioDto;
import ru.gosuslugi.pgu.dto.descriptor.FieldComponent;
import ru.gosuslugi.pgu.fs.common.component.ComponentResponse;

import java.util.Map;
import java.util.function.Supplier;

@Slf4j
@Service
public class SchoolDictionaryFilterValidationStrategy extends AbstractDictionaryFilterValidationStrategy {

    @Override
    public DictType getDictUrlType() {
        return DictType.schoolDictionaryUrl;
    }

    @Override
    public void validateAfterSubmit(Map<String, String> incorrectAnswers,
                                    Map.Entry<String, ApplicantAnswer> entry, ScenarioDto scenarioDto,
                                    FieldComponent fieldComponent, Supplier<ComponentResponse<String>> supplier) {

    }
}
