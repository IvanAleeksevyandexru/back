package ru.gosuslugi.pgu.fs.component.userdata;

import org.junit.Test;
import ru.gosuslugi.pgu.dto.descriptor.FieldComponent;
import java.util.*;
import static org.junit.Assert.assertEquals;

/**
 * Проверка метода getErrorMessage в классе SnilsComponent
 * @see SnilsComponent#getErrorMessage(ru.gosuslugi.pgu.dto.descriptor.FieldComponent, java.lang.String)
 */
public class SnilsComponentGetErrorMessageTest {

    SnilsComponent snilsComponent = new SnilsComponent(null,null,null,null,null, null);

    /**
     * когда errorMessage - пустой должно возвращаться defaultMessage
     */
    @Test
    public void testGetErrorMessageIsEmpty() {
        FieldComponent fieldComponent = new FieldComponent();
        Map<String, Object> attrs = new HashMap<>();
        attrs.put("components", new LinkedList<FieldComponent>());
        fieldComponent.setAttrs(attrs);
        String defaultMessage = "someDefaultMessage";

        String errorMessage = snilsComponent.getErrorMessage(fieldComponent, defaultMessage);
        assertEquals("должно быть someDefaultMessage", defaultMessage, errorMessage);
    }

    /**
     * когда fieldComponent - не пустой - errorMessage должен заполниться значением из fieldComponent согласно validationRule.get(REG_EXP_ERROR_MESSAGE
     */
    @Test
    public void testGetErrorMessage() {
        FieldComponent fieldComponent = new FieldComponent();
        Map<String, Object> attrs = new HashMap<>();
        List<Map<String,String>> list = new ArrayList<>();
        Map<String,String> map = new HashMap<>();
        String errorMsgValue = "errorMsgValue";
        map.put("errorMsg", errorMsgValue);
        list.add(map);
        attrs.put("validation", list);
        fieldComponent.setAttrs(attrs);

        String errorMsg = snilsComponent.getErrorMessage(fieldComponent,"someDefaultMessage");
        assertEquals(errorMsgValue, errorMsg);
    }
}
