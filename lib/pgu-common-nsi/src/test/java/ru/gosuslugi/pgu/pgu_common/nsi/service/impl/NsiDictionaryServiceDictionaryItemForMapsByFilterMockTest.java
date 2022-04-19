package ru.gosuslugi.pgu.pgu_common.nsi.service.impl;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.ExpectedCount;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestTemplate;
import ru.gosuslugi.pgu.common.core.exception.NsiExternalException;
import ru.gosuslugi.pgu.common.core.exception.handler.RestResponseErrorHandler;
import ru.gosuslugi.pgu.common.core.service.HealthHolder;
import ru.gosuslugi.pgu.common.core.service.OkatoHolder;
import ru.gosuslugi.pgu.common.core.service.dto.DictionayHealthDto;
import ru.gosuslugi.pgu.common.core.service.impl.HealthHolderImpl;
import ru.gosuslugi.pgu.pgu_common.nsi.dto.NsiDictionary;
import ru.gosuslugi.pgu.pgu_common.nsi.dto.filter.NsiDictionaryFilterRequest;
import ru.gosuslugi.pgu.pgu_common.nsi.service.NsiDictionaryService;

import java.lang.reflect.Field;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;

import static org.springframework.test.web.client.match.MockRestRequestMatchers.*;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;

/**
 * Integration tests for {@link NsiDictionaryService} methods
 *
 * @author ebalovnev
 */
public class NsiDictionaryServiceDictionaryItemForMapsByFilterMockTest {

    private static final String OKATO = "okato";

    private RestTemplate restTemplate = new RestTemplate();
    private MockRestServiceServer mockServer;

    protected NsiDictionaryService apiClient;
    protected HealthHolder healthHolder;
    protected OkatoHolder okatoHolder;

    @Before
    public void init() throws Exception {
        restTemplate.setErrorHandler(new RestResponseErrorHandler());

        mockServer = MockRestServiceServer.createServer(restTemplate);
        healthHolder = new HealthHolderImpl();
        okatoHolder = new OkatoHolderTestItem(OKATO);
        apiClient = new NsiDictionaryServiceImpl(restTemplate, healthHolder, okatoHolder);
        Field pguUrlField = NsiDictionaryServiceImpl.class.getDeclaredField("pguUrl");
        pguUrlField.setAccessible(true);
        pguUrlField.set(apiClient, "http://localhost:8072");
    }

    @Test
    public void test() throws URISyntaxException {

        NsiDictionaryFilterRequest filterRequest = new NsiDictionaryFilterRequest();
        filterRequest.setPageNum("13");

        mockServer.expect(ExpectedCount.once(),
                requestTo(new URI("http://localhost:8072/api/nsi/v1/dictionary/address")))
                .andExpect(method(HttpMethod.POST))
                .andExpect(content().json("{\"pageNum\":\"13\"}"))
                .andRespond(
                        withStatus(HttpStatus.OK)
                                .contentType(MediaType.APPLICATION_JSON)
                                .body("{\"error\":{\"code\":0}, \"total\": \"15\"}")
                );

        NsiDictionary response = apiClient.getDictionaryItemForMapsByFilter("address", filterRequest);
        Assert.assertNotNull(response);
        Assert.assertNotNull(response.getError());
        Assert.assertEquals(Integer.valueOf(0), response.getError().getCode());

        List<DictionayHealthDto> dictionaries = healthHolder.get().getDictionaries();
        Assert.assertEquals(dictionaries.size(), 1);
        Assert.assertEquals("address", dictionaries.get(0).getId());
        Assert.assertEquals("http://localhost:8072/api/nsi/v1/dictionary/address",dictionaries.get(0).getUrl());
        Assert.assertEquals(HttpMethod.GET,dictionaries.get(0).getMethod());
        Assert.assertEquals(HttpStatus.OK,dictionaries.get(0).getStatus());
        Assert.assertNull(dictionaries.get(0).getError());
        Assert.assertNull(dictionaries.get(0).getErrorMessage());
        Assert.assertEquals(OKATO, dictionaries.get(0).getOkato());

        // Verify all expectations met
        mockServer.verify();
    }

    @Test
    public void testWithCode() throws URISyntaxException {

        NsiDictionaryFilterRequest filterRequest = new NsiDictionaryFilterRequest();
        filterRequest.setPageNum("13");

        mockServer.expect(ExpectedCount.once(),
                requestTo(new URI("http://localhost:8072/api/nsi/v1/dictionary/address")))
                .andExpect(method(HttpMethod.POST))
                .andExpect(content().json("{\"pageNum\":\"13\"}"))
                .andRespond(
                        withStatus(HttpStatus.OK)
                                .contentType(MediaType.APPLICATION_JSON)
                                .body("{\"error\":{\"code\":1}}")
                );

        try {
            apiClient.getDictionaryItemForMapsByFilter("address", filterRequest);
        } catch (NsiExternalException e) {
            Assert.assertEquals("address", e.getMessage());
            Assert.assertNotNull(e.getValue());
            Assert.assertEquals("address", e.getValue().getId());
            Assert.assertEquals("http://localhost:8072/api/nsi/v1/dictionary/address", e.getValue().getUrl());
            Assert.assertEquals(HttpMethod.POST, e.getValue().getMethod());
            Assert.assertEquals("Не удалось получить справочник. код = " + 1, e.getValue().getMessage());
            Assert.assertEquals(OKATO, e.getValue().getOkato());
            Assert.assertTrue(healthHolder.get().getDictionaries().isEmpty());
        }

        // Verify all expectations met
        mockServer.verify();
    }

    @Test
    public void testWithNull() throws URISyntaxException {

        NsiDictionaryFilterRequest filterRequest = new NsiDictionaryFilterRequest();
        filterRequest.setPageNum("13");

        mockServer.expect(ExpectedCount.once(),
                requestTo(new URI("http://localhost:8072/api/nsi/v1/dictionary/address")))
                .andExpect(method(HttpMethod.POST))
                .andExpect(content().json("{\"pageNum\":\"13\"}"))
                .andRespond(
                        withStatus(HttpStatus.OK)
                                .contentType(MediaType.APPLICATION_JSON)
                );

        try {
            apiClient.getDictionaryItemForMapsByFilter("address", filterRequest);
        } catch (NsiExternalException e) {
            Assert.assertEquals("address", e.getMessage());
            Assert.assertNotNull(e.getValue());
            Assert.assertEquals("address", e.getValue().getId());
            Assert.assertEquals("http://localhost:8072/api/nsi/v1/dictionary/address", e.getValue().getUrl());
            Assert.assertEquals(HttpMethod.POST, e.getValue().getMethod());
            Assert.assertEquals("При получении справочника сервис не вернул результат", e.getValue().getMessage());
            Assert.assertEquals(OKATO, e.getValue().getOkato());
            Assert.assertTrue(healthHolder.get().getDictionaries().isEmpty());
        }

        // Verify all expectations met
        mockServer.verify();
    }

    @Test
    public void testWithException() throws URISyntaxException {

        NsiDictionaryFilterRequest filterRequest = new NsiDictionaryFilterRequest();
        filterRequest.setPageNum("13");

        mockServer.expect(ExpectedCount.once(),
                requestTo(new URI("http://localhost:8072/api/nsi/v1/dictionary/address")))
                .andExpect(method(HttpMethod.POST))
                .andExpect(content().json("{\"pageNum\":\"13\"}"))
                .andRespond(
                        withStatus(HttpStatus.NOT_FOUND)
                                .contentType(MediaType.APPLICATION_JSON)
                );

        try {
            apiClient.getDictionaryItemForMapsByFilter("address", filterRequest);
        } catch (NsiExternalException e) {
            Assert.assertEquals("address", e.getMessage());
            Assert.assertNotNull(e.getValue());
            Assert.assertEquals("address", e.getValue().getId());
            Assert.assertEquals("http://localhost:8072/api/nsi/v1/dictionary/address", e.getValue().getUrl());
            Assert.assertEquals(HttpMethod.POST, e.getValue().getMethod());
            Assert.assertEquals("Исключение при вызове метода", e.getValue().getMessage());
            Assert.assertEquals(OKATO, e.getValue().getOkato());
            Assert.assertTrue(healthHolder.get().getDictionaries().isEmpty());
        }

        // Verify all expectations met
        mockServer.verify();
    }

    @Test
    public void testWithTotal() throws URISyntaxException {

        NsiDictionaryFilterRequest filterRequest = new NsiDictionaryFilterRequest();
        filterRequest.setPageNum("13");

        mockServer.expect(ExpectedCount.once(),
                requestTo(new URI("http://localhost:8072/api/nsi/v1/dictionary/address")))
                .andExpect(method(HttpMethod.POST))
                .andExpect(content().json("{\"pageNum\":\"13\"}"))
                .andRespond(
                        withStatus(HttpStatus.OK)
                                .contentType(MediaType.APPLICATION_JSON)
                                .body("{\"error\":{\"code\":0}, \"total\": \"many\"}")
                );

        try {
            apiClient.getDictionaryItemForMapsByFilter("address", filterRequest);
        } catch (NsiExternalException e) {
            Assert.assertEquals("address", e.getMessage());
            Assert.assertNotNull(e.getValue());
            Assert.assertEquals("address", e.getValue().getId());
            Assert.assertEquals("http://localhost:8072/api/nsi/v1/dictionary/address", e.getValue().getUrl());
            Assert.assertEquals(HttpMethod.POST, e.getValue().getMethod());
            Assert.assertEquals("Проверка total. Формат total не соответствует ожидаемому. total = \"many\"", e.getValue().getMessage());
            Assert.assertEquals(OKATO, e.getValue().getOkato());
            Assert.assertTrue(healthHolder.get().getDictionaries().isEmpty());
        }

        // Verify all expectations met
        mockServer.verify();
    }

    @Test
    public void testWithTotalNegative() throws URISyntaxException {

        NsiDictionaryFilterRequest filterRequest = new NsiDictionaryFilterRequest();
        filterRequest.setPageNum("13");

        mockServer.expect(ExpectedCount.once(),
                requestTo(new URI("http://localhost:8072/api/nsi/v1/dictionary/address")))
                .andExpect(method(HttpMethod.POST))
                .andExpect(content().json("{\"pageNum\":\"13\"}"))
                .andRespond(
                        withStatus(HttpStatus.OK)
                                .contentType(MediaType.APPLICATION_JSON)
                                .body("{\"error\":{\"code\":0}, \"total\": \"-1\"}")
                );

        try {
            apiClient.getDictionaryItemForMapsByFilter("address", filterRequest);
        } catch (NsiExternalException e) {
            Assert.assertEquals("address", e.getMessage());
            Assert.assertNotNull(e.getValue());
            Assert.assertEquals("address", e.getValue().getId());
            Assert.assertEquals("http://localhost:8072/api/nsi/v1/dictionary/address", e.getValue().getUrl());
            Assert.assertEquals(HttpMethod.POST, e.getValue().getMethod());
            Assert.assertEquals("Проверка total. Значение total не соответствует ожидаемому. total = -1", e.getValue().getMessage());
            Assert.assertEquals(OKATO, e.getValue().getOkato());
            Assert.assertTrue(healthHolder.get().getDictionaries().isEmpty());
        }

        // Verify all expectations met
        mockServer.verify();
    }
}
