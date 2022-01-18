package ru.gosuslugi.pgu.fs.component.userdata;

import org.junit.Test;
import ru.gosuslugi.pgu.dto.ApplicantAnswer;
import ru.gosuslugi.pgu.dto.cycled.CycledApplicantAnswerItem;
import ru.gosuslugi.pgu.dto.descriptor.FieldComponent;

import static org.junit.Assert.*;
import static ru.gosuslugi.pgu.components.ComponentAttributes.SNILS;

/**
 * Проверка метода addToCycledItemEsiaData() в классе SnilsComponent
 * @see SnilsComponent#addToCycledItemEsiaData(ru.gosuslugi.pgu.dto.descriptor.FieldComponent, ru.gosuslugi.pgu.dto.ApplicantAnswer, CycledApplicantAnswerItem)
 */
public class SnilsComponentAddToCycledItemEsiaDataTest {

    SnilsComponent snilsComponent = new SnilsComponent(null, null, null, null,null, null);

    /**
     * когда ApplicantAnswer null мапа esiaData должна быть пустой
     */
    @Test
    public void testAddToCycledItemEsiaDataApplicantAnswerIsNull() {
        ApplicantAnswer applicantAnswer = null;
        CycledApplicantAnswerItem item = new CycledApplicantAnswerItem();

        snilsComponent.addToCycledItemEsiaData(null,applicantAnswer,item);
        assertTrue("Ожидается не заполненная мапа", item.getEsiaData().isEmpty());
    }

    /**
     * когда ApplicantAnswer пустой мапа esiaData должна быть пустой
     */
    @Test
    public void testAddToCycledItemEsiaDataApplicantAnswerIsEmpty() {
        ApplicantAnswer applicantAnswer = new ApplicantAnswer();
        CycledApplicantAnswerItem item = new CycledApplicantAnswerItem();

        snilsComponent.addToCycledItemEsiaData(null,applicantAnswer,item);
        assertTrue("Ожидается не заполненная мапа", item.getEsiaData().isEmpty());
    }

    /**
     * когда ApplicantAnswer не снилс - мапа esiaData должна быть пустой
     */
    @Test
    public void testAddToCycledItemEsiaDataApplicantAnswerIsNotSnils() {
        ApplicantAnswer applicantAnswer = new ApplicantAnswer();
        applicantAnswer.setValue("{\"key\": \"someValue\"}");
        CycledApplicantAnswerItem item = new CycledApplicantAnswerItem();

        snilsComponent.addToCycledItemEsiaData(null,applicantAnswer,item);
        assertTrue("Ожидается не заполненная мапа", item.getEsiaData().isEmpty());
    }

    /**
     * когда ApplicantAnswer  снилс - мапа esiaData должна быть заполнена снилсом, переданном в ApplicantAnswer
     */
    @Test
    public void testAddToCycledItemEsiaDataApplicantAnswerIsSnils() {
        ApplicantAnswer applicantAnswer = new ApplicantAnswer();
        applicantAnswer.setValue("{\"snils\": \"someValue\"}");
        CycledApplicantAnswerItem item = new CycledApplicantAnswerItem();
        FieldComponent fieldComponent = new FieldComponent();
        fieldComponent.setId("f1");

        snilsComponent.addToCycledItemEsiaData(fieldComponent, applicantAnswer,item);
        assertFalse("Ожидается заполненный снилс", item.getEsiaData().isEmpty());
        assertEquals(1,item.getEsiaData().size());
        assertEquals("someValue", item.getEsiaData().get(SNILS));
    }
}
