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
import ru.gosuslugi.pgu.pgu_common.nsi.dto.DadataAddressResponse;
import ru.gosuslugi.pgu.pgu_common.nsi.service.NsiDadataService;

import java.lang.reflect.Field;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;

import static org.junit.Assert.fail;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;

/**
 * Integration tests for {@link NsiDadataServiceImpl} methods
 *
 * @author ebalovnev
 */
public class NsiDadataServiceAddressMockTest {

    private static final String OKATO = "okato";
    private static final String DADATA_NORMALIZE_URL = "http://localhost:8072/api/nsi/v1/dadata/normalize";
    private static final String FILLED_QUERY_STRING = "?q={q}";
    private static final String ADDRESS_QUERY_STRING = "?q=address";

    private final RestTemplate restTemplate = new RestTemplate();
    private MockRestServiceServer mockServer;

    protected NsiDadataService apiClient;
    protected HealthHolder healthHolder;
    protected OkatoHolder okatoHolder;

    @Before
    public void init() throws Exception {
        restTemplate.setErrorHandler(new RestResponseErrorHandler());

        mockServer = MockRestServiceServer.createServer(restTemplate);
        healthHolder = new HealthHolderImpl();
        okatoHolder = new OkatoHolderTestItem(OKATO);
        apiClient = new NsiDadataServiceImpl(restTemplate, healthHolder, okatoHolder);
        Field pguUrlField = NsiDadataServiceImpl.class.getDeclaredField("pguUrl");
        pguUrlField.setAccessible(true);
        pguUrlField.set(apiClient, "http://localhost:8072/");
    }

    @Test
    public void testGet() throws URISyntaxException {

        mockServer.expect(ExpectedCount.once(),
                requestTo(new URI(DADATA_NORMALIZE_URL + ADDRESS_QUERY_STRING)))
                .andExpect(method(HttpMethod.GET))
                .andRespond(
                        withStatus(HttpStatus.OK)
                                .contentType(MediaType.APPLICATION_JSON)
                                .body("{\"error\":{\"code\":0}}")
                );

        DadataAddressResponse response = apiClient.getAddress("address");
        Assert.assertNotNull(response);
        Assert.assertNotNull(response.getError());
        Assert.assertEquals(Integer.valueOf(0), response.getError().getCode());

        List<DictionayHealthDto> dictionaries = healthHolder.get().getDictionaries();
        Assert.assertEquals(dictionaries.size(), 1);
        Assert.assertEquals("dadataAddress", dictionaries.get(0).getId());
        Assert.assertEquals(DADATA_NORMALIZE_URL + FILLED_QUERY_STRING,dictionaries.get(0).getUrl());
        Assert.assertEquals(HttpMethod.GET,dictionaries.get(0).getMethod());
        Assert.assertEquals(HttpStatus.OK,dictionaries.get(0).getStatus());
        Assert.assertNull(dictionaries.get(0).getError());
        Assert.assertNull(dictionaries.get(0).getErrorMessage());
        Assert.assertEquals(OKATO, dictionaries.get(0).getOkato());

        // Verify all expectations met
        mockServer.verify();
    }

    @Test
    public void testGetByOkato() throws URISyntaxException {

        mockServer.expect(ExpectedCount.once(),
                requestTo(new URI(DADATA_NORMALIZE_URL + ADDRESS_QUERY_STRING)))
                .andExpect(method(HttpMethod.GET))
                .andRespond(
                        withStatus(HttpStatus.OK)
                                .contentType(MediaType.APPLICATION_JSON)
                                .body("{\"error\":{\"code\":0}}")
                );

        DadataAddressResponse response = apiClient.getAddress("address", "new_okato");
        Assert.assertNotNull(response);
        Assert.assertNotNull(response.getError());
        Assert.assertEquals(Integer.valueOf(0), response.getError().getCode());

        List<DictionayHealthDto> dictionaries = healthHolder.get().getDictionaries();
        Assert.assertEquals(dictionaries.size(), 1);
        Assert.assertEquals("dadataAddress", dictionaries.get(0).getId());
        Assert.assertEquals(DADATA_NORMALIZE_URL + FILLED_QUERY_STRING,dictionaries.get(0).getUrl());
        Assert.assertEquals(HttpMethod.GET,dictionaries.get(0).getMethod());
        Assert.assertEquals(HttpStatus.OK,dictionaries.get(0).getStatus());
        Assert.assertNull(dictionaries.get(0).getError());
        Assert.assertNull(dictionaries.get(0).getErrorMessage());
        Assert.assertEquals("new_okato", dictionaries.get(0).getOkato());

        // Verify all expectations met
        mockServer.verify();
    }

    @Test
    public void testGetWithErrorCode() throws URISyntaxException {

        mockServer.expect(ExpectedCount.once(),
                requestTo(new URI(DADATA_NORMALIZE_URL + ADDRESS_QUERY_STRING)))
                .andExpect(method(HttpMethod.GET))
                .andRespond(
                        withStatus(HttpStatus.OK)
                                .contentType(MediaType.APPLICATION_JSON)
                                .body("{\"error\":{\"code\":1}}")
                );

        try {
            apiClient.getAddress("address");
            fail();
        } catch (NsiExternalException e) {
            Assert.assertEquals("dadataAddress", e.getMessage());
            Assert.assertNotNull(e.getValue());
            Assert.assertEquals("dadataAddress", e.getValue().getId());
            Assert.assertEquals(DADATA_NORMALIZE_URL + FILLED_QUERY_STRING, e.getValue().getUrl());
            Assert.assertEquals(HttpMethod.GET, e.getValue().getMethod());
            Assert.assertEquals("Не удалось получить проверить адрес в Dadata сервисе. Код = " + 1, e.getValue().getMessage());
            Assert.assertEquals(OKATO, e.getValue().getOkato());
            Assert.assertTrue(healthHolder.get().getDictionaries().isEmpty());
        }

        // Verify all expectations met
        mockServer.verify();
    }

    @Test
    public void testGetWithNull() throws URISyntaxException {

        mockServer.expect(ExpectedCount.once(),
                requestTo(new URI(DADATA_NORMALIZE_URL + ADDRESS_QUERY_STRING)))
                .andExpect(method(HttpMethod.GET))
                .andRespond(
                        withStatus(HttpStatus.OK)
                                .contentType(MediaType.APPLICATION_JSON)
                );

        try {
            apiClient.getAddress("address");
            fail();
        } catch (NsiExternalException e) {
            Assert.assertEquals("dadataAddress", e.getMessage());
            Assert.assertNotNull(e.getValue());
            Assert.assertEquals("dadataAddress", e.getValue().getId());
            Assert.assertEquals(DADATA_NORMALIZE_URL + FILLED_QUERY_STRING, e.getValue().getUrl());
            Assert.assertEquals(HttpMethod.GET, e.getValue().getMethod());
            Assert.assertEquals("Сервис не вернул результат", e.getValue().getMessage());
            Assert.assertEquals(OKATO, e.getValue().getOkato());
            Assert.assertTrue(healthHolder.get().getDictionaries().isEmpty());
        }

        // Verify all expectations met
        mockServer.verify();
    }

    @Test
    public void testGetWithException() throws URISyntaxException {

        mockServer.expect(ExpectedCount.once(),
                requestTo(new URI(DADATA_NORMALIZE_URL + ADDRESS_QUERY_STRING)))
                .andExpect(method(HttpMethod.GET))
                .andRespond(
                        withStatus(HttpStatus.NOT_FOUND)
                                .contentType(MediaType.APPLICATION_JSON)
                );

        try {
            apiClient.getAddress("address");
            fail();
        } catch (NsiExternalException e) {
            Assert.assertEquals("dadataAddress", e.getMessage());
            Assert.assertNotNull(e.getValue());
            Assert.assertEquals("dadataAddress", e.getValue().getId());
            Assert.assertEquals(DADATA_NORMALIZE_URL + FILLED_QUERY_STRING, e.getValue().getUrl());
            Assert.assertEquals(HttpMethod.GET, e.getValue().getMethod());
            Assert.assertEquals("Исключение при вызове метода", e.getValue().getMessage());
            Assert.assertEquals(OKATO, e.getValue().getOkato());
            Assert.assertTrue(healthHolder.get().getDictionaries().isEmpty());
        }

        // Verify all expectations met
        mockServer.verify();
    }
}
