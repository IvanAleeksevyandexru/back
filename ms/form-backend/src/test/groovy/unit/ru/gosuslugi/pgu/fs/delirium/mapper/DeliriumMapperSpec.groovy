package unit.ru.gosuslugi.pgu.fs.delirium.mapper

import ru.gosuslugi.pgu.dto.ApplicantDto
import ru.gosuslugi.pgu.dto.ApplicantRole
import ru.gosuslugi.pgu.dto.DisplayRequest
import ru.gosuslugi.pgu.dto.ScenarioDto
import ru.gosuslugi.pgu.dto.descriptor.ScreenDescriptor
import ru.gosuslugi.pgu.fs.delirium.mapper.DeliriumMapper
import spock.lang.Shared
import spock.lang.Specification

class DeliriumMapperSpec extends Specification {

    @Shared DeliriumMapper deliriumMapper

    def setupSpec() {
        deliriumMapper = new DeliriumMapper()
    }

    def 'Map to delirium dto'() {
        given:
        def scenarioDto = new ScenarioDto()
        scenarioDto.setServiceCode('serviceCode')
        scenarioDto.setTargetCode('targetCode')
        scenarioDto.setOrderId(567890L)
        scenarioDto.setDisplay(new DisplayRequest(new ScreenDescriptor(), null))

        Map<String, ApplicantDto> participants = new LinkedHashMap<>()
        participants.put("1111", ApplicantDto.builder().role(ApplicantRole.Coapplicant).build());
        participants.put("2222", ApplicantDto.builder().role(ApplicantRole.Coapplicant).build());
        participants.put("3333", ApplicantDto.builder().role(ApplicantRole.Approval).build());
        participants.put("4444", ApplicantDto.builder().role(ApplicantRole.ApprovalParent).build());
        participants.put("5555", ApplicantDto.builder().role(ApplicantRole.ChildrenAbove14).build());
        scenarioDto.setParticipants(participants)

        when:
        def deliriumRequestDto = deliriumMapper.mapToDeliriumDto(scenarioDto, 12345L)

        then:
        deliriumRequestDto.getOid() == 12345
        deliriumRequestDto.getOrderId() == 567890L
        deliriumRequestDto.getContextData().getServiceCode() == 'serviceCode'
        deliriumRequestDto.getContextData().getTargetCode() == 'targetCode'
        deliriumRequestDto.getContextData().isAccept()
        deliriumRequestDto.getContextData().getApplicants().size() == 6
        deliriumRequestDto.getContextData().getApplicants().get(0).getOid() == 12345
        deliriumRequestDto.getContextData().getApplicants().get(0).getRole() == ApplicantRole.Applicant
        deliriumRequestDto.getContextData().getApplicants().get(1).getOid() == 1111
        deliriumRequestDto.getContextData().getApplicants().get(1).getRole() == ApplicantRole.Coapplicant
        deliriumRequestDto.getContextData().getApplicants().get(2).getOid() == 2222
        deliriumRequestDto.getContextData().getApplicants().get(2).getRole() == ApplicantRole.Coapplicant
        deliriumRequestDto.getContextData().getApplicants().get(3).getOid() == 3333
        deliriumRequestDto.getContextData().getApplicants().get(3).getRole() == ApplicantRole.Approval
        deliriumRequestDto.getContextData().getApplicants().get(4).getOid() == 4444
        deliriumRequestDto.getContextData().getApplicants().get(4).getRole() == ApplicantRole.ApprovalParent
        deliriumRequestDto.getContextData().getApplicants().get(5).getOid() == 5555
        deliriumRequestDto.getContextData().getApplicants().get(5).getRole() == ApplicantRole.ChildrenAbove14

    }
}
