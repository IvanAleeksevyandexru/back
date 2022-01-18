package ru.gosuslugi.pgu.fs.delirium.mapper;

import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Service;
import ru.gosuslugi.pgu.dto.ApplicantDto;
import ru.gosuslugi.pgu.dto.ScenarioDto;
import ru.gosuslugi.pgu.fs.exception.InnerDataError;
import ru.gosuslugi.pgu.dto.ApplicantRole;
import ru.gosuslugi.pgu.fs.delirium.model.declaration.DeliriumApplicantDto;
import ru.gosuslugi.pgu.fs.delirium.model.declaration.DeliriumContextDataDto;
import ru.gosuslugi.pgu.fs.delirium.model.DeliriumRequestDto;

import java.util.Map;

@Service
public class DeliriumMapper {

    /**
     * Переносит данные из сценария сервиса форм в dto запроса в Delirium с проверкой исходных данных.
     * Эти проверки специально сделны именно здесь, чтобы при каких-то изменениях в структуре было видно,
     * что необходима доработка Delirium.
     * Иначе несогласованность данных станет заметна в лучшем случае в runtime на стендах.
     * В настоящее время Delirium живёт собственной жизнью, это не часть сервиса форм, у него свой, независимый, план развития.
     *
     * @param scenarioDto Сценарий сервиса форм
     * @return dto запроса в Delirium
     */
    public DeliriumRequestDto mapToDeliriumDto(ScenarioDto scenarioDto, Long userId){
        long orderId = scenarioDto.getOrderId();
        boolean isAccept = scenarioDto.getDisplay().isAccepted();
        String serviceCode = scenarioDto.getServiceCode();
        String targetCode = scenarioDto.getTargetCode();

        if (StringUtils.isBlank(serviceCode)) {
            throw new InnerDataError("Parameter 'scenarioDto.serviceCode' is blank.");
        }
        if (StringUtils.isBlank(targetCode)) {
            throw new InnerDataError("Parameter 'scenarioDto.targetCode' is blank.");
        }

        DeliriumRequestDto deliriumRequestDto = new DeliriumRequestDto();
        deliriumRequestDto.setOid(userId);
        deliriumRequestDto.setOrderId(orderId);

        DeliriumContextDataDto deliriumContextDataDto = new DeliriumContextDataDto();
        deliriumRequestDto.setContextData(deliriumContextDataDto);
        deliriumContextDataDto.setAccept(isAccept);
        deliriumContextDataDto.setServiceCode(serviceCode);
        deliriumContextDataDto.setTargetCode(targetCode);

        // Main applicant
        // Обратить внимание - список участников отличается от сценария
        DeliriumApplicantDto mainApplicantDto = new DeliriumApplicantDto(userId, ApplicantRole.Applicant);
        deliriumContextDataDto.addApplicant(mainApplicantDto);

        // Others applicants
        for (Map.Entry<String, ApplicantDto> participant : scenarioDto.getParticipants().entrySet()) {
            long  participantOid = Long.parseLong(participant.getKey());

            ApplicantRole role = participant.getValue().getRole(); // Обратить внимание на соответствие ролей. Сейчас роли в сценарии - строка.
            if (role == ApplicantRole.Applicant) {
                throw new InnerDataError("The Applicant role is not allowed there.");
            }

            DeliriumApplicantDto dto = new DeliriumApplicantDto(participantOid, role);
            deliriumContextDataDto.addApplicant(dto);
        }

        return deliriumRequestDto;
    }

    public DeliriumRequestDto mapToDeliriumDto(ScenarioDto scenarioDto, String deliriumAction, Long userId){
        DeliriumRequestDto deliriumRequestDto = mapToDeliriumDto(scenarioDto, userId);
        DeliriumContextDataDto contextData = deliriumRequestDto.getContextData();
        contextData.setAction(deliriumAction);
        return deliriumRequestDto;
    }
}
