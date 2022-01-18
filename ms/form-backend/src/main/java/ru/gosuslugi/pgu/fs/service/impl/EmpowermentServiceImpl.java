package ru.gosuslugi.pgu.fs.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.gosuslugi.pgu.common.esia.search.dto.UserOrgData;
import ru.gosuslugi.pgu.common.esia.search.dto.UserPersonalData;
import ru.gosuslugi.pgu.fs.pgu.client.PguClient;
import ru.gosuslugi.pgu.fs.pgu.client.PguEmpowermentClient;
import ru.gosuslugi.pgu.fs.pgu.client.PguEmpowermentClientV2;
import ru.gosuslugi.pgu.fs.service.EmpowermentService;

import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmpowermentServiceImpl implements EmpowermentService {

    private final UserOrgData userOrgData;
    private final UserPersonalData userPersonalData;
    private final PguEmpowermentClient pguEmpowermentClient;
    private final PguEmpowermentClientV2 pguEmpowermentClientV2;
    private final PguClient pguClient;

    @Override
    public boolean hasEmpowerment(List<String> empowerment) {
        boolean hasEmpowerment = true;
        if (Objects.nonNull(empowerment) && !empowerment.isEmpty()
                && Objects.nonNull(userOrgData.getOrg())
                && Objects.nonNull(userOrgData.getOrgRole())
                && !Boolean.parseBoolean(userOrgData.getOrgRole().getChief())
                && !hasEmpowermentInEsia(empowerment)) {
            hasEmpowerment = false;
        }
        return hasEmpowerment;
    }

    @Override
    public Set<String> getUserEmpowerments() {
        Set<String> setOne = pguEmpowermentClientV2.getUserEmpowerment(userPersonalData.getToken(), userPersonalData.getUserId());
        Set<String> setTwo = pguEmpowermentClient.getUserEmpowerment(userPersonalData.getToken(), userPersonalData.getUserId(), userPersonalData.getOrgId());
        return Stream.concat(setOne.stream(), setTwo.stream()).collect(Collectors.toSet());
    }

    private boolean hasEmpowermentInEsia(List<String> empowerment) {
        return getUserEmpowerments().stream().anyMatch(empowerment::contains);
    }


    @Override
    public boolean checkServiceAvailableForUser(String targetId) {
        var authorityId = userPersonalData.getAuthorityId();
        if(Objects.isNull(userOrgData.getOrg())){
            return true;
        }
        if(userOrgData.getSystemAuthority().contains("PGU.ORG_CAB.ACCESS")
                || (Objects.nonNull(userPersonalData.getCurrentRole()) && Boolean.parseBoolean(userPersonalData.getCurrentRole().getChief()))){
            return true;
        }
        return pguClient.checkAuthorityForService(authorityId,targetId);
    }

    @Override
    public boolean checkUserUserPermissionForSendOrder(String targetId){
        var authorityId = userPersonalData.getAuthorityId();
        if(Objects.isNull(userOrgData.getOrg())){
            return true;
        }
        if(Objects.nonNull(userPersonalData.getCurrentRole()) && Boolean.parseBoolean(userPersonalData.getCurrentRole().getChief())){
            return true;
        }
        if(Objects.isNull(authorityId)){
            return false;
        }
        return pguClient.checkAuthorityForService(authorityId,targetId);
    }
}
