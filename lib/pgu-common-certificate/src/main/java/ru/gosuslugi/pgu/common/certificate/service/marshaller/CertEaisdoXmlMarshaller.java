package ru.gosuslugi.pgu.common.certificate.service.marshaller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.xml.bind.marshaller.NamespacePrefixMapper;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.gosuslugi.pgu.common.certificate.xsd.dto.UniversalCertificateRequest;
import ru.gosuslugi.pgu.common.certificate.xsd.dto.UniversalCertificateResponse;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.Map;

@Component
@Slf4j
public class CertEaisdoXmlMarshaller implements CommonXmlMarshallerUnmarshaller<UniversalCertificateRequest, UniversalCertificateResponse>{
    private static final String BIND_NAMESPACE_PREFIX_MAPPER = "com.sun.xml.bind.namespacePrefixMapper";

    @SneakyThrows
    public String marshal(UniversalCertificateRequest dto) {
        JAXBContext context = JAXBContext.newInstance(UniversalCertificateRequest.class);
        Marshaller marshaller = context.createMarshaller();
        marshaller.setProperty(Marshaller.JAXB_ENCODING, "UTF-8");
        marshaller.setProperty(BIND_NAMESPACE_PREFIX_MAPPER, namespacePrefixMapper);
        StringWriter stringWriter = new StringWriter();
        marshaller.marshal(dto, stringWriter);
        return stringWriter.toString();
    }

    @SneakyThrows
    @Override
    public UniversalCertificateResponse unmarshal(String xml) {
        JAXBContext context = JAXBContext.newInstance(UniversalCertificateResponse.class);
        Unmarshaller unmarshaller = context.createUnmarshaller();
        StringReader stringReader = new StringReader(xml);
        return (UniversalCertificateResponse) unmarshaller.unmarshal(stringReader);
    }

    @SneakyThrows
    @Override
    public String unmarshalToString(String xmlString) {
        JAXBContext context = JAXBContext.newInstance(UniversalCertificateResponse.class);
        Unmarshaller unmarshaller = context.createUnmarshaller();
        StringReader stringReader = new StringReader(xmlString);
        Object response = unmarshaller.unmarshal(stringReader);
        ObjectMapper mapper = new ObjectMapper();
        return mapper.writeValueAsString(response);

    }

    private final NamespacePrefixMapper namespacePrefixMapper = new NamespacePrefixMapper() {
        private final Map<String, String> namespaceMap = Map.of(
                "http://epgu.gosuslugi.ru/services-get-pf-certificate/1.1.1", "tns",
                "http://www.w3.org/2001/XMLSchema-instance", "xsi");

        @Override
        public String getPreferredPrefix(String namespaceUri, String suggestion, boolean requirePrefix) {
            return namespaceMap.getOrDefault(namespaceUri, suggestion);
        }
    };

}
