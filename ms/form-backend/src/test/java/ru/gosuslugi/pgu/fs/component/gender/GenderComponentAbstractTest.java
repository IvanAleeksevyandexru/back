package ru.gosuslugi.pgu.fs.component.gender;

import org.junit.Test;
import ru.atc.carcass.security.rest.model.person.Person;
import ru.gosuslugi.pgu.common.esia.search.dto.UserPersonalData;
import ru.gosuslugi.pgu.dto.ScenarioDto;
import ru.gosuslugi.pgu.dto.descriptor.FieldComponent;
import ru.gosuslugi.pgu.fs.component.AbstractGenderComponent;
import java.util.HashMap;
import java.util.Map;
import static org.junit.Assert.*;

/**
 * Проверка метода processGender() в классе AbstractGenderComponent
 * @see AbstractGenderComponent#processGender(ru.gosuslugi.pgu.dto.descriptor.FieldComponent, ru.gosuslugi.pgu.dto.ScenarioDto)
 */
public class GenderComponentAbstractTest {

    UserPersonalData userPersonalData = new UserPersonalData();
    GenderComponentTestItem genderComponentTestItem = new GenderComponentTestItem(userPersonalData);

    /**
     * Проверка на равенство засеченных атрибутов в FieldComponent
     */
    @Test
    public void processGenderTest() {
        FieldComponent fieldComponent = new FieldComponent();
        ScenarioDto scenarioDto = new ScenarioDto();
        userPersonalData.setUserId(4L);
        Person person = new Person();
        userPersonalData.setPerson(person);

        genderComponentTestItem.processGender(fieldComponent,scenarioDto);
        Map<String, Object> expectedMap = new HashMap<>();
        expectedMap.put("key","value");
        assertEquals("",expectedMap,fieldComponent.getAttrs() );
    }
}
