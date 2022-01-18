package ru.gosuslugi.pgu.fs.component.child;

import org.springframework.util.StringUtils;
import ru.atc.carcass.security.rest.model.person.Kids;

import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Утилитный класс для проверки эквивалентности объектов Kids и esiaData
 */
public class ChildComparator {

    private ChildComparator() {
        throw new IllegalStateException("Utility class");
    }
    /**
     * Проверка на наличие эквивалентного объекта Kids в списке esiaData
     */
    public static boolean contains(List<Map<String, Object>> esiaDataList, Kids kid) {
        return esiaDataList.stream().anyMatch(item -> equals(kid, item));
    }
    /**
     * Проверка на эквивалентность объекта Kids и esiaData
     */
    public static boolean equals(Kids kid, Map<String, Object> esiaData) {

        String id = kid.getId();
        if (StringUtils.hasText(id) && id.equals(esiaData.get("id"))) {
            return true;
        }
        if (!Objects.equals(id, esiaData.get("id"))) {
            return false;
        }

        String snils = kid.getSnils();
        if (StringUtils.hasText(snils) && snils.equals(esiaData.get(ChildAttributes.CHILDREN_SNILS_ATTR.name))) {
            return true;
        }
        if (!Objects.equals(snils, esiaData.get(ChildAttributes.CHILDREN_SNILS_ATTR.name))) {
            return false;
        }

        if (!Objects.equals(kid.getGender(), esiaData.get(ChildAttributes.CHILDREN_GENDER_ATTR.name))) {
            return false;
        }
        if (!Objects.equals(kid.getFirstName(), esiaData.get(ChildAttributes.CHILDREN_FIRST_NAME_ATTR.name))) {
            return false;
        }
        if (!Objects.equals(kid.getLastName(), esiaData.get(ChildAttributes.CHILDREN_LAST_NAME_ATTR.name))) {
            return false;
        }
        if (!Objects.equals(kid.getMiddleName(), esiaData.get(ChildAttributes.CHILDREN_MIDDLE_NAME_ATTR.name))) {
            return false;
        }
        return true;
    }
}
