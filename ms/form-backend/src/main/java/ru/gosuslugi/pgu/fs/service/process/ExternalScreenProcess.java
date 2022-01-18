package ru.gosuslugi.pgu.fs.service.process;

import ru.gosuslugi.pgu.dto.ScenarioFromExternal;
import ru.gosuslugi.pgu.dto.ScenarioResponse;
import ru.gosuslugi.pgu.fs.service.process.impl.screen.ExternalScreenProcessImpl;

public interface ExternalScreenProcess  extends  Process<ExternalScreenProcessImpl, ScenarioResponse>{
    ExternalScreenProcess of(ScenarioFromExternal scenarioFromExternal);
    ExternalScreenProcess of(ScenarioFromExternal scenarioFromExternal, boolean deleteOrderOnPguException);

    void prepareApplicantAnswer();

    void prepareDisplay();

    void createOrder();

    void saveDraft();


}
