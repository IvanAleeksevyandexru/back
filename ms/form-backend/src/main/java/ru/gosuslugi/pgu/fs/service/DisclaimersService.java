package ru.gosuslugi.pgu.fs.service;

import ru.gosuslugi.pgu.dto.DisclaimerDto;
import ru.gosuslugi.pgu.dto.DisplayRequest;
import ru.gosuslugi.pgu.dto.ScenarioDto;

import java.util.List;

public interface DisclaimersService {
    List<DisclaimerDto> getDisclaimers(String serviceCode, String targetCode);

    DisplayRequest getDisplayForCriticalDisclaimer(DisclaimerDto disclaimer, ScenarioDto scenarioDto);
}
