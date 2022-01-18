package ru.gosuslugi.pgu.fs.component.input;

import com.google.common.base.Charsets;
import com.jayway.jsonpath.DocumentContext;
import org.junit.Before;
import org.junit.Test;
import ru.gosuslugi.pgu.common.core.json.JsonProcessingUtil;
import ru.gosuslugi.pgu.components.descriptor.attr_factory.AttrsFactory;
import ru.gosuslugi.pgu.dto.DisplayRequest;
import ru.gosuslugi.pgu.dto.ScenarioDto;
import ru.gosuslugi.pgu.dto.descriptor.FieldComponent;
import ru.gosuslugi.pgu.dto.descriptor.LinkedValue;
import ru.gosuslugi.pgu.dto.descriptor.types.ComponentType;
import ru.gosuslugi.pgu.fs.common.component.input.FieldListComponent;
import ru.gosuslugi.pgu.fs.common.service.ComponentReferenceService;
import ru.gosuslugi.pgu.fs.common.service.JsonProcessingService;
import ru.gosuslugi.pgu.fs.common.service.LinkedValuesService;
import ru.gosuslugi.pgu.fs.common.service.UserCookiesService;
import ru.gosuslugi.pgu.fs.common.service.impl.ComponentReferenceServiceImpl;
import ru.gosuslugi.pgu.fs.common.service.impl.JsonProcessingServiceImpl;
import ru.gosuslugi.pgu.fs.common.service.impl.UserCookiesServiceImpl;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Paths;

import static org.junit.Assert.assertEquals;

public class FieldListComponentTest {


    FieldListComponent fieldListComponent;

    FieldComponent fieldComponent;

    JsonProcessingService jsonProcessingService = new JsonProcessingServiceImpl(JsonProcessingUtil.getObjectMapper());

    UserCookiesService userCookiesService = new UserCookiesServiceImpl();

    LinkedValuesService linkedValuesService = new LinkedValuesService() {

        @Override
        public void fillLinkedValues(FieldComponent fieldComponent, ScenarioDto scenarioDto, DocumentContext... externalContexts) {

        }

        @Override
        public void fillLinkedValues(DisplayRequest displayRequest, ScenarioDto scenarioDto) {

        }

        @Override
        public String getValue(LinkedValue linkedValue, ScenarioDto scenarioDto, AttrsFactory attrsFactory, DocumentContext... externalContexts) {
            return null;
        }
    };

    @Before
    public void setUp() throws Exception {
        // инициализация сервисов
        ComponentReferenceService componentReferenceService = new ComponentReferenceServiceImpl(jsonProcessingService, userCookiesService, linkedValuesService);

        fieldListComponent = new FieldListComponent(componentReferenceService);

        // загрузка тестовых json-ов
        fieldComponent = jsonProcessingService.fromJson(getJson("-fieldInit.json"), FieldComponent.class);
    }

    @Test
    public void preSetComponentValue() throws IOException, URISyntaxException {
        String result = getResult("-scenarioDto.json");
        assertEquals("{\"states\":[{\"groupName\":\"Данные без рефа\",\"fields\":[{\"label\":\"Лейбл без рефа\",\"value\":\"Значение без рефа\",\"rank\":false}]},{\"groupName\":\"Не циклические с разделителем\",\"fields\":[{\"label\":\"Лейбл\",\"value\":\"Значение\",\"rank\":false}],\"needDivider\":true},{\"groupName\":\"Данные с рефом (Анна)\",\"fields\":[{\"label\":\"Лейбл с рефами ( Петрова)\",\"value\":\"Значение с рефом (Выписка из ЕГРН)\",\"rank\":false}]},{\"groupName\":\"Одно поле пустое\",\"fields\":[{\"label\":\"Пустое значение в value\",\"value\":\"\",\"rank\":false}]},{\"groupName\":\"Данные с рефом, неверная ссылка\",\"fields\":[{\"label\":\"Национальность\",\"value\":\"fai2.wrong.path\",\"rank\":false}]},{\"groupName\":\"Циклические все (сидр петров)\",\"fields\":[{\"label\":\"Имя\",\"value\":\"сидр\",\"rank\":false},{\"label\":\"Фамилия\",\"value\":\"петров\",\"rank\":false}]},{\"groupName\":\"Циклические все (коньяк петров)\",\"fields\":[{\"label\":\"Имя\",\"value\":\"коньяк\",\"rank\":false},{\"label\":\"Фамилия\",\"value\":\"петров\",\"rank\":false}]},{\"groupName\":\"Циклические все (сидр петров) с разделителем\",\"fields\":[{\"label\":\"Имя и Фамилия\",\"value\":\"сидр петров\",\"rank\":false}],\"needDivider\":true},{\"groupName\":\"Циклические все (коньяк петров) с разделителем\",\"fields\":[{\"label\":\"Имя и Фамилия\",\"value\":\"коньяк петров\",\"rank\":false}],\"needDivider\":true},{\"groupName\":\"Один цикличный (коньяк петров)\",\"fields\":[{\"label\":\"Имя и Фамилия\",\"value\":\"коньяк петров\",\"rank\":false}]}]}", result);
    }

    @Test
    public void preSetComponentValueEmptyApplicantAnswers() throws IOException, URISyntaxException {
        String result = getResult("-scenarioDto-emptyApplicantAnswers.json");
        assertEquals("{\"states\":[{\"groupName\":\"Данные без рефа\",\"fields\":[{\"label\":\"Лейбл без рефа\",\"value\":\"Значение без рефа\",\"rank\":false}]},{\"groupName\":\"Не циклические с разделителем\",\"fields\":[{\"label\":\"Лейбл\",\"value\":\"Значение\",\"rank\":false}],\"needDivider\":true},{\"groupName\":\"Данные с рефом (pd1.value.storedValues.firstName)\",\"fields\":[{\"label\":\"Лейбл с рефами (pd1.value.storedValues.middleName pd1.value.storedValues.lastName)\",\"value\":\"Значение с рефом (pd6.value)\",\"rank\":false}]},{\"groupName\":\"Одно поле пустое\",\"fields\":[{\"label\":\"Национальность\",\"value\":\"fai2.value.text\",\"rank\":false},{\"label\":\"Первый или повторный брак\",\"value\":\"fai5.value\",\"rank\":false},{\"label\":\"Пустое значение в value\",\"value\":\"\",\"rank\":false}]},{\"groupName\":\"Все поля пустые (fai2.value.text)\",\"fields\":[{\"label\":\"Образование\",\"value\":\"fai1.value.text\",\"rank\":false},{\"label\":\"Первый или повторный брак\",\"value\":\"fai5.value\",\"rank\":false}]},{\"groupName\":\"Данные с рефом, неверная ссылка\",\"fields\":[{\"label\":\"Национальность\",\"value\":\"fai2.wrong.path\",\"rank\":false}]},{\"groupName\":\"Циклические все (сидр петров)\",\"fields\":[{\"label\":\"Имя\",\"value\":\"сидр\",\"rank\":false},{\"label\":\"Фамилия\",\"value\":\"петров\",\"rank\":false}]},{\"groupName\":\"Циклические все (коньяк петров)\",\"fields\":[{\"label\":\"Имя\",\"value\":\"коньяк\",\"rank\":false},{\"label\":\"Фамилия\",\"value\":\"петров\",\"rank\":false}]},{\"groupName\":\"Циклические все (сидр петров) с разделителем\",\"fields\":[{\"label\":\"Имя и Фамилия\",\"value\":\"сидр петров\",\"rank\":false}],\"needDivider\":true},{\"groupName\":\"Циклические все (коньяк петров) с разделителем\",\"fields\":[{\"label\":\"Имя и Фамилия\",\"value\":\"коньяк петров\",\"rank\":false}],\"needDivider\":true}]}", result);
    }

    @Test
    public void preSetComponentValueEmptyCycledApplicantAnswers() throws IOException, URISyntaxException {
        String result = getResult("-scenarioDto-emptyCycledApplicantAnswers.json");
        assertEquals("{\"states\":[{\"groupName\":\"Данные без рефа\",\"fields\":[{\"label\":\"Лейбл без рефа\",\"value\":\"Значение без рефа\",\"rank\":false}]},{\"groupName\":\"Не циклические с разделителем\",\"fields\":[{\"label\":\"Лейбл\",\"value\":\"Значение\",\"rank\":false}],\"needDivider\":true},{\"groupName\":\"Данные с рефом (Анна)\",\"fields\":[{\"label\":\"Лейбл с рефами (Петровна Петрова)\",\"value\":\"Значение с рефом (Выписка из ЕГРН)\",\"rank\":false}]},{\"groupName\":\"Одно поле пустое\",\"fields\":[{\"label\":\"Национальность\",\"value\":\"АБХАЗИЯ\",\"rank\":false},{\"label\":\"Пустое значение в value\",\"value\":\"\",\"rank\":false}]},{\"groupName\":\"Данные с рефом, неверная ссылка\",\"fields\":[{\"label\":\"Национальность\",\"value\":\"fai2.wrong.path\",\"rank\":false}]}]}", result);
    }

    @Test
    public void preSetComponentValueEmptyAll() throws IOException, URISyntaxException {
        String result = getResult("-scenarioDto-emptyAll.json");
        assertEquals("{\"states\":[{\"groupName\":\"Данные без рефа\",\"fields\":[{\"label\":\"Лейбл без рефа\",\"value\":\"Значение без рефа\",\"rank\":false}]},{\"groupName\":\"Не циклические с разделителем\",\"fields\":[{\"label\":\"Лейбл\",\"value\":\"Значение\",\"rank\":false}],\"needDivider\":true},{\"groupName\":\"Данные с рефом (pd1.value.storedValues.firstName)\",\"fields\":[{\"label\":\"Лейбл с рефами (pd1.value.storedValues.middleName pd1.value.storedValues.lastName)\",\"value\":\"Значение с рефом (pd6.value)\",\"rank\":false}]},{\"groupName\":\"Одно поле пустое\",\"fields\":[{\"label\":\"Национальность\",\"value\":\"fai2.value.text\",\"rank\":false},{\"label\":\"Первый или повторный брак\",\"value\":\"fai5.value\",\"rank\":false},{\"label\":\"Пустое значение в value\",\"value\":\"\",\"rank\":false}]},{\"groupName\":\"Все поля пустые (fai2.value.text)\",\"fields\":[{\"label\":\"Образование\",\"value\":\"fai1.value.text\",\"rank\":false},{\"label\":\"Первый или повторный брак\",\"value\":\"fai5.value\",\"rank\":false}]},{\"groupName\":\"Данные с рефом, неверная ссылка\",\"fields\":[{\"label\":\"Национальность\",\"value\":\"fai2.wrong.path\",\"rank\":false}]}]}", result);
    }

    @Test
    public void preSetComponentValueNoAnswers() throws IOException, URISyntaxException {
        String result = getResult("-scenarioDto-noAnswers.json");
        assertEquals("{\"states\":[{\"groupName\":\"Данные без рефа\",\"fields\":[{\"label\":\"Лейбл без рефа\",\"value\":\"Значение без рефа\",\"rank\":false}]},{\"groupName\":\"Не циклические с разделителем\",\"fields\":[{\"label\":\"Лейбл\",\"value\":\"Значение\",\"rank\":false}],\"needDivider\":true},{\"groupName\":\"Данные с рефом (pd1.value.storedValues.firstName)\",\"fields\":[{\"label\":\"Лейбл с рефами (pd1.value.storedValues.middleName pd1.value.storedValues.lastName)\",\"value\":\"Значение с рефом (pd6.value)\",\"rank\":false}]},{\"groupName\":\"Одно поле пустое\",\"fields\":[{\"label\":\"Национальность\",\"value\":\"fai2.value.text\",\"rank\":false},{\"label\":\"Первый или повторный брак\",\"value\":\"fai5.value\",\"rank\":false},{\"label\":\"Пустое значение в value\",\"value\":\"\",\"rank\":false}]},{\"groupName\":\"Все поля пустые (fai2.value.text)\",\"fields\":[{\"label\":\"Образование\",\"value\":\"fai1.value.text\",\"rank\":false},{\"label\":\"Первый или повторный брак\",\"value\":\"fai5.value\",\"rank\":false}]},{\"groupName\":\"Данные с рефом, неверная ссылка\",\"fields\":[{\"label\":\"Национальность\",\"value\":\"fai2.wrong.path\",\"rank\":false}]}]}", result);
    }

    @Test
    public void preSetComponentValueEmptyCycledAnswers() throws IOException, URISyntaxException {
        String result = getResult("-scenarioDto-emptyCycledAnswers.json");
        assertEquals("{\"states\":[{\"groupName\":\"Данные без рефа\",\"fields\":[{\"label\":\"Лейбл без рефа\",\"value\":\"Значение без рефа\",\"rank\":false}]},{\"groupName\":\"Не циклические с разделителем\",\"fields\":[{\"label\":\"Лейбл\",\"value\":\"Значение\",\"rank\":false}],\"needDivider\":true},{\"groupName\":\"Данные с рефом (Анна)\",\"fields\":[{\"label\":\"Лейбл с рефами (Петровна Петрова)\",\"value\":\"Значение с рефом (Выписка из ЕГРН)\",\"rank\":false}]},{\"groupName\":\"Одно поле пустое\",\"fields\":[{\"label\":\"Национальность\",\"value\":\"АБХАЗИЯ\",\"rank\":false},{\"label\":\"Пустое значение в value\",\"value\":\"\",\"rank\":false}]},{\"groupName\":\"Данные с рефом, неверная ссылка\",\"fields\":[{\"label\":\"Национальность\",\"value\":\"fai2.wrong.path\",\"rank\":false}]}]}", result);
    }

    @Test
    public void preSetComponentValueEmptyCycledItems() throws IOException, URISyntaxException {
        String result = getResult("-scenarioDto-emptyCycledItems.json");
        assertEquals("{\"states\":[{\"groupName\":\"Данные без рефа\",\"fields\":[{\"label\":\"Лейбл без рефа\",\"value\":\"Значение без рефа\",\"rank\":false}]},{\"groupName\":\"Не циклические с разделителем\",\"fields\":[{\"label\":\"Лейбл\",\"value\":\"Значение\",\"rank\":false}],\"needDivider\":true},{\"groupName\":\"Данные с рефом (Анна)\",\"fields\":[{\"label\":\"Лейбл с рефами (Петровна Петрова)\",\"value\":\"Значение с рефом (Выписка из ЕГРН)\",\"rank\":false}]},{\"groupName\":\"Одно поле пустое\",\"fields\":[{\"label\":\"Национальность\",\"value\":\"АБХАЗИЯ\",\"rank\":false},{\"label\":\"Пустое значение в value\",\"value\":\"\",\"rank\":false}]},{\"groupName\":\"Данные с рефом, неверная ссылка\",\"fields\":[{\"label\":\"Национальность\",\"value\":\"fai2.wrong.path\",\"rank\":false}]}]}", result);
    }

    @Test
    public void preSetComponentValueEmptyCycledItemsWithIds() throws IOException, URISyntaxException {
        String result = getResult("-scenarioDto-emptyCycledItemsWithIds.json");
        assertEquals("{\"states\":[{\"groupName\":\"Данные без рефа\",\"fields\":[{\"label\":\"Лейбл без рефа\",\"value\":\"Значение без рефа\",\"rank\":false}]},{\"groupName\":\"Не циклические с разделителем\",\"fields\":[{\"label\":\"Лейбл\",\"value\":\"Значение\",\"rank\":false}],\"needDivider\":true},{\"groupName\":\"Данные с рефом (Анна)\",\"fields\":[{\"label\":\"Лейбл с рефами (Петровна Петрова)\",\"value\":\"Значение с рефом (Выписка из ЕГРН)\",\"rank\":false}]},{\"groupName\":\"Одно поле пустое\",\"fields\":[{\"label\":\"Национальность\",\"value\":\"АБХАЗИЯ\",\"rank\":false},{\"label\":\"Пустое значение в value\",\"value\":\"\",\"rank\":false}]},{\"groupName\":\"Данные с рефом, неверная ссылка\",\"fields\":[{\"label\":\"Национальность\",\"value\":\"fai2.wrong.path\",\"rank\":false}]},{\"groupName\":\"Циклические все (bd6.value.storedValues.firstName bd6.value.storedValues.lastName)\",\"fields\":[{\"label\":\"Имя\",\"value\":\"bd6.value.storedValues.firstName\",\"rank\":false},{\"label\":\"Фамилия\",\"value\":\"bd6.value.storedValues.lastName\",\"rank\":false}]},{\"groupName\":\"Циклические все (bd6.value.storedValues.firstName bd6.value.storedValues.lastName)\",\"fields\":[{\"label\":\"Имя\",\"value\":\"bd6.value.storedValues.firstName\",\"rank\":false},{\"label\":\"Фамилия\",\"value\":\"bd6.value.storedValues.lastName\",\"rank\":false}]},{\"groupName\":\"Циклические все (bd6.value.storedValues.firstName bd6.value.storedValues.lastName) с разделителем\",\"fields\":[{\"label\":\"Имя и Фамилия\",\"value\":\"bd6.value.storedValues.firstName bd6.value.storedValues.lastName\",\"rank\":false}],\"needDivider\":true},{\"groupName\":\"Циклические все (bd6.value.storedValues.firstName bd6.value.storedValues.lastName) с разделителем\",\"fields\":[{\"label\":\"Имя и Фамилия\",\"value\":\"bd6.value.storedValues.firstName bd6.value.storedValues.lastName\",\"rank\":false}],\"needDivider\":true},{\"groupName\":\"Один цикличный (bd6.value.storedValues.firstName bd6.value.storedValues.lastName)\",\"fields\":[{\"label\":\"Имя и Фамилия\",\"value\":\"bd6.value.storedValues.firstName bd6.value.storedValues.lastName\",\"rank\":false}]}]}", result);
    }

    @Test
    public void preSetComponentValueEmptyCycledItemAnswers() throws IOException, URISyntaxException {
        String result = getResult("-scenarioDto-emptyCycledItemAnswers.json");
        assertEquals("{\"states\":[{\"groupName\":\"Данные без рефа\",\"fields\":[{\"label\":\"Лейбл без рефа\",\"value\":\"Значение без рефа\",\"rank\":false}]},{\"groupName\":\"Не циклические с разделителем\",\"fields\":[{\"label\":\"Лейбл\",\"value\":\"Значение\",\"rank\":false}],\"needDivider\":true},{\"groupName\":\"Данные с рефом (Анна)\",\"fields\":[{\"label\":\"Лейбл с рефами (Петровна Петрова)\",\"value\":\"Значение с рефом (Выписка из ЕГРН)\",\"rank\":false}]},{\"groupName\":\"Одно поле пустое\",\"fields\":[{\"label\":\"Национальность\",\"value\":\"АБХАЗИЯ\",\"rank\":false},{\"label\":\"Пустое значение в value\",\"value\":\"\",\"rank\":false}]},{\"groupName\":\"Данные с рефом, неверная ссылка\",\"fields\":[{\"label\":\"Национальность\",\"value\":\"fai2.wrong.path\",\"rank\":false}]},{\"groupName\":\"Циклические все (bd6.value.storedValues.firstName bd6.value.storedValues.lastName)\",\"fields\":[{\"label\":\"Имя\",\"value\":\"bd6.value.storedValues.firstName\",\"rank\":false},{\"label\":\"Фамилия\",\"value\":\"bd6.value.storedValues.lastName\",\"rank\":false}]},{\"groupName\":\"Циклические все (bd6.value.storedValues.firstName bd6.value.storedValues.lastName)\",\"fields\":[{\"label\":\"Имя\",\"value\":\"bd6.value.storedValues.firstName\",\"rank\":false},{\"label\":\"Фамилия\",\"value\":\"bd6.value.storedValues.lastName\",\"rank\":false}]},{\"groupName\":\"Циклические все (bd6.value.storedValues.firstName bd6.value.storedValues.lastName) с разделителем\",\"fields\":[{\"label\":\"Имя и Фамилия\",\"value\":\"bd6.value.storedValues.firstName bd6.value.storedValues.lastName\",\"rank\":false}],\"needDivider\":true},{\"groupName\":\"Циклические все (bd6.value.storedValues.firstName bd6.value.storedValues.lastName) с разделителем\",\"fields\":[{\"label\":\"Имя и Фамилия\",\"value\":\"bd6.value.storedValues.firstName bd6.value.storedValues.lastName\",\"rank\":false}],\"needDivider\":true},{\"groupName\":\"Один цикличный (bd6.value.storedValues.firstName bd6.value.storedValues.lastName)\",\"fields\":[{\"label\":\"Имя и Фамилия\",\"value\":\"bd6.value.storedValues.firstName bd6.value.storedValues.lastName\",\"rank\":false}]}]}", result);
    }

    @Test
    public void getType() {
        assertEquals(ComponentType.FieldList, fieldListComponent.getType());
    }

    private String getJson(String fileSuffix) throws IOException, URISyntaxException {
        return String.join("", Files.readAllLines(Paths.get(getClass().getResource(getClass().getSimpleName() + fileSuffix).toURI()), Charsets.UTF_8));
    }

    private String getResult(String testSuffix) throws IOException, URISyntaxException {
        ScenarioDto scenarioDto = jsonProcessingService.fromJson(getJson(testSuffix), ScenarioDto.class);
        return jsonProcessingService.componentDtoToString(fieldListComponent.getInitialValue(fieldComponent, scenarioDto));
    }
}