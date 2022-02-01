package ru.gosuslugi.pgu.fs.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.atc.carcass.security.rest.model.EsiaContact;
import ru.gosuslugi.pgu.common.esia.search.dto.UserOrgData;
import ru.gosuslugi.pgu.dto.ScenarioDto;
import ru.gosuslugi.pgu.fs.esia.EsiaRestContactDataClient;
import ru.gosuslugi.pgu.fs.esia.dto.EsiaContactDto;
import ru.gosuslugi.pgu.fs.service.OrgContactService;

import java.util.Comparator;
import java.util.Optional;

import static ru.gosuslugi.pgu.components.ComponentAttributes.ORG_EMAIL_TYPE_ATTR;
import static ru.gosuslugi.pgu.components.ComponentAttributes.VERIFIED_ATTR;

@Service
@Slf4j
@RequiredArgsConstructor
public class OrgContactServiceImpl implements OrgContactService {

    private final EsiaRestContactDataClient esiaRestContactDataClient;
    private final UserOrgData userOrgData;

    @Override
    public void resendEmailConfirmation(ScenarioDto scenarioDto) {
        esiaRestContactDataClient.resendLegalCode(scenarioDto.getNewContactId());
    }

    @Override
    public boolean isEmailConfirmedAndLinked(String email) {
        return esiaRestContactDataClient.isEmailConfirmed(ORG_EMAIL_TYPE_ATTR, email);
    }

    @Override
    public void updateEmail(ScenarioDto scenarioDto, String email) {
        EsiaContactDto updatedContact = createOrUpdateContact(ORG_EMAIL_TYPE_ATTR, email);
        scenarioDto.setNewContactId(String.valueOf(updatedContact.getId()));
    }

    /**
     * Добавление/обновление контакта организации
     * @param esiaContactTypeCode ESIA код типа контакта
     * @param contactValue значение
     * @return ответ DTO контакта
     */
    private EsiaContactDto createOrUpdateContact(String esiaContactTypeCode, String contactValue) {
        Optional<EsiaContact> esiaContactOptional = userOrgData.getContacts().stream()
                .filter(c -> c.getType().equals(esiaContactTypeCode))

                // VERIFIED to first
                .sorted(Comparator.comparing(c -> VERIFIED_ATTR.equals(c.getVrfStu()) ? 0 : 1))
                .findFirst();
        EsiaContactDto esiaContactDto = new EsiaContactDto();
        esiaContactDto.setValue(contactValue);
        if (esiaContactOptional.isEmpty()) {
            esiaContactDto.setType(esiaContactTypeCode);
            return esiaRestContactDataClient.addLegalContact(esiaContactDto);
        }
        esiaContactDto.setId(Long.valueOf(esiaContactOptional.get().getId()));
        esiaContactDto.setEtag(esiaContactOptional.get().getETag());
        esiaContactDto.setVerifyingValue(esiaContactOptional.get().getValue());
        esiaContactDto.setVrfValStu(esiaContactOptional.get().getVrfStu());
        esiaContactDto.setType(esiaContactOptional.get().getType());
        return esiaRestContactDataClient.changeLegalContact(esiaContactDto);
    }
}
