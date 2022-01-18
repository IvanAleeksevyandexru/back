package ru.gosuslugi.pgu.fs.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.atc.carcass.security.rest.model.EsiaContact;
import ru.gosuslugi.pgu.dto.ScenarioDto;
import ru.gosuslugi.pgu.common.esia.search.dto.UserPersonalData;
import ru.gosuslugi.pgu.fs.esia.EsiaRestContactDataClient;
import ru.gosuslugi.pgu.fs.esia.dto.EsiaContactDto;
import ru.gosuslugi.pgu.fs.esia.dto.EsiaContactState;
import ru.gosuslugi.pgu.fs.service.PersonContactService;

import java.util.Comparator;
import java.util.Optional;

import static ru.gosuslugi.pgu.components.ComponentAttributes.VERIFIED_ATTR;

@Service
@Slf4j
@RequiredArgsConstructor
public class PersonContactServiceImpl implements PersonContactService {

    private final EsiaRestContactDataClient esiaRestContactDataClient;
    private final UserPersonalData userPersonalData;

    @Override
    public EsiaContactState validatePhoneNumber(String phoneNumber) {
        return esiaRestContactDataClient.checkPhone(phoneNumber);
    }

    @Override
    public boolean updatePhoneNumber(String phoneNumber, ScenarioDto scenarioDto) {
        EsiaContactDto updatedContact = createOrUpdateContact(EsiaContact.Type.MOBILE_PHONE, phoneNumber);
        scenarioDto.setNewContactId(String.valueOf(updatedContact.getId()));
        return true;
    }

    private EsiaContactDto createOrUpdateContact(EsiaContact.Type type, String contactValue) {
        Optional<EsiaContact> esiaContactOptional = userPersonalData.getContacts().stream()
            .filter(c -> c.getType().equals(type.getCode()))

            // VERIFIED to first
            .sorted(Comparator.comparing(c -> VERIFIED_ATTR.equals(c.getVrfStu()) ? 0 : 1))
            .findFirst();
        EsiaContactDto esiaContactDto = new EsiaContactDto();
        esiaContactDto.setValue(contactValue);
        if (esiaContactOptional.isEmpty()) {
            esiaContactDto.setType(type.getCode());
            return esiaRestContactDataClient.addContact(esiaContactDto);
        }
        esiaContactDto.setId(Long.valueOf(esiaContactOptional.get().getId()));
        esiaContactDto.setEtag(esiaContactOptional.get().getETag());
        esiaContactDto.setVerifyingValue(esiaContactOptional.get().getValue());
        esiaContactDto.setVrfValStu(esiaContactOptional.get().getVrfStu());
        esiaContactDto.setType(esiaContactOptional.get().getType());

        return esiaRestContactDataClient.changeContact(esiaContactDto);

    }

    @Override
    public void resendPhoneConfirmationCode(ScenarioDto scenarioDto) {
        esiaRestContactDataClient.resendCode(scenarioDto.getNewContactId());
    }

    @Override
    public boolean checkConfirmationCode(String confirmationCode, ScenarioDto scenarioDto) {
        boolean result =  esiaRestContactDataClient.confirmContact(confirmationCode);
        if(result){
            scenarioDto.setNewContactId(null);
        }
        return result;
    }

    @Override
    public void resendEmailConfirmation(ScenarioDto scenarioDto) {
        esiaRestContactDataClient.resendCode(scenarioDto.getNewContactId());
    }

    @Override
    public boolean isEmailIsAlreadyUsed(String email) {
        return esiaRestContactDataClient.checkEmail(email);
    }

    @Override
    public boolean isEmailConfirmedAndLinkedToUser(String email) {
        return esiaRestContactDataClient.isEmailConfirmed(EsiaContact.Type.EMAIL.getCode(), email);
    }

    @Override
    public boolean updateUserEmail(ScenarioDto scenarioDto, String email) {
        EsiaContactDto updatedContact = createOrUpdateContact(EsiaContact.Type.EMAIL, email);
        scenarioDto.setNewContactId(String.valueOf(updatedContact.getId()));
        return true;
    }

    @Override
    public String preparePhoneNumberForEsia(String phoneNumber) {
        return phoneNumber.replace(" ", "").replace("-", "");
    }
}
