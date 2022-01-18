package ru.gosuslugi.pgu.fs.component.gender;

import org.junit.Test;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import static org.junit.Assert.*;

/**
 * Проверка метода processAttrs() в классе AbstractDictionaryGenderComponent
 * @see AbstractDictionaryGenderComponent#processAttrs(java.lang.String, java.util.Map)
 */
public class AbstractDictionaryGenderComponentTest {

    AbstractDictionaryGenderComponentTestItem abstractDictionaryGenderComponentTestItem = new AbstractDictionaryGenderComponentTestItem();

    Map<String, Object> map = new HashMap<>();
    {
        List<String> listValues = new ArrayList<>();
        listValues.add("STRANI_IST");
        listValues.add("TO_PFR");
        map.put("dictionaryType",listValues);
    }
    Map<String, Object> expectedMap = new HashMap<>();

    /**
     * Если gender = "M", map-а должна заполняться 0-ым значением из листа значений
     */
    @Test
    public void processAttrsTestGenderM() {
        String gender = "M";
        expectedMap.put("dictionaryType", "STRANI_IST");
        assertEquals(expectedMap, abstractDictionaryGenderComponentTestItem.processAttrs(gender,map));
    }

    /**
     * Если gender = "F", map-а должна заполняться 0-ым значением из листа значений
     */
    @Test
    public void processAttrsTestGenderF() {
        String gender = "F";
        expectedMap.put("dictionaryType", "TO_PFR");
        assertEquals(expectedMap, abstractDictionaryGenderComponentTestItem.processAttrs(gender,map));
    }
}
