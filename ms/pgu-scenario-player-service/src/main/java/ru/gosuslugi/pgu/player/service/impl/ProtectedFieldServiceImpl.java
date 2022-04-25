package ru.gosuslugi.pgu.player.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.atc.carcass.security.rest.model.person.Person;
import ru.atc.carcass.security.rest.model.person.PersonDoc;
import ru.gosuslugi.pgu.common.esia.search.dto.UserOrgData;
import ru.gosuslugi.pgu.common.esia.search.dto.UserPersonalData;
import ru.gosuslugi.pgu.fs.common.service.ProtectedFieldService;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

import static ru.gosuslugi.pgu.common.core.date.util.DateUtil.ESIA_DATE_FORMAT;
import static ru.gosuslugi.pgu.components.ComponentAttributes.MDCL_PLCY_ATTR;
import static ru.gosuslugi.pgu.components.ComponentAttributes.VERIFIED_ATTR;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProtectedFieldServiceImpl implements ProtectedFieldService {

    private static final String ORGANIZATION_TYPE = "orgType";
    private static final String ORGANIZATION_USER_ROLE = "userRole";

    private final UserPersonalData userPersonalData;
    private final UserOrgData userOrgData;

    private final Map<String, Function<UserPersonalData, Object>> methodMap = new HashMap<>() {{
        put("citizenshipCode", userPersonalData -> userPersonalData.getPerson().getCitizenshipCode());
        put("gender", userPersonalData -> userPersonalData.getPerson().getGender());
        put("birthDate", userPersonalData -> LocalDate.parse(userPersonalData.getPerson().getBirthDate(), DateTimeFormatter.ofPattern(ESIA_DATE_FORMAT)).format(DateTimeFormatter.ISO_DATE));
        put("snils", userPersonalData -> userPersonalData.getPerson().getSnils());
        put("omsNumber", userPersonalData -> {
            PersonDoc doc = getOmsDoc(userPersonalData);
            return doc != null ? doc.getNumber(): null;
        });
        put("omsSeries", userPersonalData -> {
            PersonDoc doc = getOmsDoc(userPersonalData);
            return doc != null ? doc.getSeries(): null;
        });
    }};

    public Object getValue(String name) {
        Person person = userPersonalData.getPerson();
        if(Objects.isNull(person)){
            return null;
        }
        if (methodMap.containsKey(name)) {
            return methodMap.get(name).apply(userPersonalData);
        }
        if (Objects.nonNull(userOrgData.getOrg())) {
            return getOrgValue(name);
        }

        return null;
    }

    @Override
    public Map<String, Object> getAllValues() {
        return methodMap.entrySet().stream()
                .collect(HashMap::new, (map, e) -> map.put(e.getKey(), getValue(e.getKey())), HashMap::putAll);
    }

    private Object getOrgValue(String name){
        Object result = null;
        if(ORGANIZATION_TYPE.equals(name)){
            if(Objects.nonNull(userOrgData.getOrg())){
                result = userOrgData.getOrg().getType().toString();
            }
        }
        if(ORGANIZATION_USER_ROLE.equals(name) && Objects.nonNull(userPersonalData.getPerson())){
            result = userPersonalData.getPerson().isChief();
        }
        return result;
    }

    private PersonDoc getOmsDoc(UserPersonalData userPersonalData) {
        List<PersonDoc> omsDocs = userPersonalData.getDocs().stream().filter(x -> (MDCL_PLCY_ATTR.equals(x.getType()))).collect(Collectors.toList());
        return omsDocs.stream().filter(doc -> doc.getVrfStu().equals(VERIFIED_ATTR)).findFirst().orElse(omsDocs.stream().findFirst().orElse(null));
    }
}
