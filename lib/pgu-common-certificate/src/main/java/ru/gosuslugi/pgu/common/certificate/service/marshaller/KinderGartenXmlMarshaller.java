package ru.gosuslugi.pgu.common.certificate.service.marshaller;

import com.sun.xml.bind.marshaller.NamespacePrefixMapper;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.gosuslugi.pgu.common.core.json.JsonProcessingUtil;
import ru.gosuslugi.pgu.common.kindergarten.xsd.dto.FormData;
import ru.gosuslugi.pgu.common.kindergarten.xsd.dto.FormDataResponse;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import java.io.StringReader;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.util.Map;

@Slf4j
@Component
public class KinderGartenXmlMarshaller implements CommonXmlMarshallerUnmarshaller<FormData, FormDataResponse> {
    private static final String BIND_NAMESPACE_PREFIX_MAPPER = "com.sun.xml.bind.namespacePrefixMapper";
    private static final NamespacePrefixMapper NAMESPACE_PREFIX_MAPPER = new KinderGartenNamespacePrefixMapper();
    private static final JAXBContext jaxbContext;

    static {
        // статический экземпляр JAXBContext потокобезопасен, его можно использовать внутри бина.
        try {
            jaxbContext = JAXBContext.newInstance(FormData.class, FormDataResponse.class);
        } catch (JAXBException e) {
            throw new IllegalStateException("Cannot initialize JAXBContext for kindergarten marshalling", e);
        }
    }

    @SneakyThrows
    @Override
    public String marshal(FormData dto) {
        Marshaller marshaller = jaxbContext.createMarshaller();
        marshaller.setProperty(Marshaller.JAXB_ENCODING, StandardCharsets.UTF_8.name());
        marshaller.setProperty(BIND_NAMESPACE_PREFIX_MAPPER, NAMESPACE_PREFIX_MAPPER);
        StringWriter stringWriter = new StringWriter();
        marshaller.marshal(dto, stringWriter);
        return stringWriter.toString();
    }

    @SneakyThrows
    @Override
    public FormDataResponse unmarshal(String xmlString) {
        Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
        StringReader stringReader = new StringReader(xmlString);
        return (FormDataResponse) unmarshaller.unmarshal(stringReader);
    }

    @SneakyThrows
    @Override
    public String unmarshalToString(String xmlString) {
        Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
        StringReader stringReader = new StringReader(xmlString);
        Object response = unmarshaller.unmarshal(stringReader);
        String responseStr = JsonProcessingUtil.getObjectMapper().writeValueAsString(response);
        return responseStr;
    }

    private static class KinderGartenNamespacePrefixMapper extends NamespacePrefixMapper {
        private static final Map<String, String> namespaceMap = Map.of(
                "http://epgu.gosuslugi.ru/concentrator/kindergarten/3.2.1", "tns",
                "http://www.w3.org/2001/XMLSchema-instance", "xsi");

        @Override
        public String getPreferredPrefix(String namespaceUri, String suggestion, boolean requirePrefix) {
            return namespaceMap.getOrDefault(namespaceUri, suggestion);
        }
    }
}
