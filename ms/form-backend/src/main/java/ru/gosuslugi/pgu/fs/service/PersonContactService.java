package ru.gosuslugi.pgu.fs.service;

import ru.gosuslugi.pgu.dto.ScenarioDto;
import ru.gosuslugi.pgu.fs.esia.dto.EsiaContactState;

public interface PersonContactService {

    EsiaContactState validatePhoneNumber(String phoneNumber);

    boolean updatePhoneNumber(String phoneNumber, ScenarioDto scenarioDto);

    boolean checkConfirmationCode(String confirmationCode, ScenarioDto scenarioDto);

    void resendPhoneConfirmationCode(ScenarioDto scenarioDto);

    void resendEmailConfirmation(ScenarioDto scenarioDto);

    boolean isEmailIsAlreadyUsed(String email);

    boolean isEmailConfirmedAndLinkedToUser(String email);

    boolean updateUserEmail(ScenarioDto scenarioDto, String email);

    String preparePhoneNumberForEsia(String phoneNumber);
}
