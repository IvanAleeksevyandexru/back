package ru.gosuslugi.pgu.fs.helper.input;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jayway.jsonpath.Configuration;
import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.internal.ParseContextImpl;
import com.jayway.jsonpath.spi.mapper.JacksonMappingProvider;
import org.apache.commons.io.IOUtils;
import org.junit.Test;
import ru.gosuslugi.pgu.common.core.json.JsonProcessingUtil;
import ru.gosuslugi.pgu.components.descriptor.types.FullAddress;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class EnrichAddressTest {

    private static final ObjectMapper objectMapper = JsonProcessingUtil.getObjectMapper();

    @Test
    public void testRegistrationAddrHelper() throws IOException, URISyntaxException {

        Configuration configuration = Configuration.builder().mappingProvider(new JacksonMappingProvider(objectMapper)).build();
        ParseContextImpl parseContext = new ParseContextImpl(configuration);
        //check JSON
        String address = readFile("ru/gosuslugi/pgu/fs/input/EnrichAddressTest_RegistrationAddrHelper.json");
        assertNotNull(address);
        DocumentContext documentContext = parseContext.parse(address);
        assertNotNull(documentContext);
        FullAddress fullAddress = documentContext.read("$['regAddr']", FullAddress.class);
        assertNotNull(documentContext);
        documentContext.set("$['regAddr']", fullAddress);
        String resultAddress = documentContext.jsonString();

        // suppress field orders
        assertEquals(
            objectMapper.readTree(readFile("ru/gosuslugi/pgu/fs/input/EnrichAddressTest_RegistrationAddrHelper_excpected.json")),
            objectMapper.readTree(resultAddress)
        );
    }


    private String readFile(String fileName) throws IOException {
        return IOUtils.toString(getClass().getClassLoader().getResourceAsStream(fileName), StandardCharsets.UTF_8.name());
    }
}