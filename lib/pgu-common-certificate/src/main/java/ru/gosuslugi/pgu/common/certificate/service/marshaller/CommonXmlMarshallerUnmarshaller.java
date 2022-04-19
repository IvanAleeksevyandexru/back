package ru.gosuslugi.pgu.common.certificate.service.marshaller;

public interface CommonXmlMarshallerUnmarshaller<F,T> {
    String marshal(F dto);
    T unmarshal(String xml);
    String unmarshalToString(String xmlString);
}
