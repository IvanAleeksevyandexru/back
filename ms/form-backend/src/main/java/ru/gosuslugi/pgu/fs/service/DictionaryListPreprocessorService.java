package ru.gosuslugi.pgu.fs.service;

import ru.gosuslugi.pgu.dto.ScenarioDto;
import ru.gosuslugi.pgu.dto.descriptor.FieldComponent;

public interface DictionaryListPreprocessorService {

    String DICTIONARY_REF_KEY = "dictionaryRef";

    void prepareDictionaryListFromComponent(FieldComponent component, ScenarioDto scenarioDto, String value);

    void prepareDictionaryListFromComponent(FieldComponent component, ScenarioDto scenarioDto);
}
