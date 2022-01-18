package ru.gosuslugi.pgu.fs.component.gender;

import ru.gosuslugi.pgu.common.esia.search.dto.UserPersonalData;
import ru.gosuslugi.pgu.dto.descriptor.types.ComponentType;
import ru.gosuslugi.pgu.fs.component.AbstractGenderComponent;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/** Класс для теста метода processGender() и получения UserPersonalData в классе AbstractGenderComponent
 * @see GenderComponentAbstractTest
 * @see AbstractGenderComponent#processGender(ru.gosuslugi.pgu.dto.descriptor.FieldComponent, ru.gosuslugi.pgu.dto.ScenarioDto)
 */
public class GenderComponentTestItem extends AbstractGenderComponent<String> {

    /**
     * Для возможности задать UserPersonalData при вызове метода processGender
     */
    public GenderComponentTestItem(UserPersonalData userPersonalData) {
        this.userPersonalData = userPersonalData;
    }

    /**
     * Метод реализован для возможности вызвать его внутри метода processGender и задать тестовые данные
     */
    @Override
    protected Map<String, Object> processAttrs(String gender, Map<String, Object> attrs) {
        Map<String, Object> result = new HashMap<>();
        result.put("key","value");
        return result;
    }

    @Override
    protected ComponentType getTargetComponentType() {
        return null;
    }

    @Override
    public ComponentType getType() {
        return null;
    }
}
