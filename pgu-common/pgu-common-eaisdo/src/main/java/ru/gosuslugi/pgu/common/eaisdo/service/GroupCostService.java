package ru.gosuslugi.pgu.common.eaisdo.service;

import com.sun.xml.bind.marshaller.NamespacePrefixMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import ru.gosuslugi.pgu.common.core.exception.ExternalServiceException;
import ru.gosuslugi.pgu.common.core.json.JsonProcessingUtil;
import ru.gosuslugi.pgu.common.eaisdo.service.model.xsd.GroupCostRequest;
import ru.gosuslugi.pgu.common.eaisdo.service.model.xsd.GroupCostRequestDataType;
import ru.gosuslugi.pgu.dto.descriptor.FieldComponent;
import ru.gosuslugi.pgu.fs.common.service.ExternalService;
import ru.gosuslugi.pgu.fs.common.utils.PguAuthHeadersUtil;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import java.io.Serializable;
import java.io.StringReader;
import java.io.StringWriter;
import java.time.DateTimeException;
import java.time.LocalDate;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static java.util.Objects.nonNull;
import static org.springframework.util.StringUtils.hasText;

@Slf4j
@RequiredArgsConstructor
@Service
public class GroupCostService implements ExternalService {

    private static final String EAISDO_API_PATH = "api/v1/epgu/queries";
    private static final String EAISDO_API_ERROR = "{\"errorList\": [{\"type\": \"externalRequestError\", \"message\": \"Ошибка при запросе сведений\"}]}";
    private static final String BIND_NAMESPACE_PREFIX_MAPPER = "com.sun.xml.bind.namespacePrefixMapper";
    private static final String MODEL_XSD_PACKAGE = "ru.gosuslugi.pgu.common.eaisdo.service.model.xsd";

    @Value("${pgu.eaisdo-url}")
    private String serviceUrl;

    @Value("${eaisdo.uploader-token}")
    private String eaisdoStorageToken;

    private final RestTemplate restTemplate;

    @Override
    public String getServiceCode() {
        return "eaisdoGroupCostRequest";
    }

    @Override
    public String sendRequest(FieldComponent component) {
        var data = new GroupCostRequestDataType();
        data.setGroupGUID(component.getArgument("groupGUID"));
        try {
            String startEducationDate = component.getArgument("startEducationDate");
            var localDate = LocalDate.parse(startEducationDate.substring(0, 10));
            var calendar = Calendar.getInstance();
            calendar.set(localDate.getYear(), localDate.getMonthValue() - 1, localDate.getDayOfMonth());
            data.setStartEducationDate(calendar);
        } catch (IndexOutOfBoundsException | DateTimeException e) {
            log.error("Ошибка при разборе даты: {} {}", component, e);
        }
        data.setCertificateGUID(component.getArgument("certificateGUID"));
        data.setFinancialSource(component.getArgument("financialSource"));

        var request = new GroupCostRequest();
        request.setRequestData(data);
        request.getDatasources().add(component.getArgument("datasource"));
        if (validateRequest(request)) {
            return getGroupCostData(request);
        }

        Map<String, Serializable> errors = Map.of("type", "validationError", "message", request);
        return JsonProcessingUtil.toJson(Map.of("errorList", List.of(errors)));
    }

    private String getGroupCostData(GroupCostRequest request) {
        try {
            JAXBContext context = JAXBContext.newInstance(GroupCostRequest.class);
            Marshaller mar = context.createMarshaller();
            mar.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
            mar.setProperty(BIND_NAMESPACE_PREFIX_MAPPER, namespacePrefixMapper);
            StringWriter sw = new StringWriter();
            mar.marshal(request, sw);
            String xmlString = sw.toString();
            var entity = new HttpEntity<>(xmlString, prepareHttpHeaders(eaisdoStorageToken));
            var response = restTemplate.exchange(serviceUrl + EAISDO_API_PATH, HttpMethod.POST, entity, String.class);
            if (response.getStatusCode().is2xxSuccessful() && nonNull(response.getBody())) {
                return getDataFromXmlValue(response.getBody());
            }
        } catch (RestClientException ex) {
            log.error("Ошибка при запросе сведений Расчет стоимости кружка: {} {}", request, ex);
        } catch (ExternalServiceException ex) {
            log.error("Ошибка соединения с сервисом Расчет стоимости кружка: {} {}", request, ex);
        } catch (JAXBException ex) {
            log.error("Ошибка преобразования XML: {}", request, ex);
        }
        return EAISDO_API_ERROR;
    }

    private String getDataFromXmlValue(String xml) throws JAXBException {
        JAXBContext context = JAXBContext.newInstance(MODEL_XSD_PACKAGE);
        Unmarshaller unmar = context.createUnmarshaller();
        var stringReader = new StringReader(xml);
        Object response = unmar.unmarshal(stringReader);
        Map<String, Object> responseMap = Map.of(
                "errorList", List.of(),
                "responseData", Map.of(
                        "value", response,
                        "type", response.getClass().getSimpleName())
        );
        return JsonProcessingUtil.toJson(responseMap);
    }

    private boolean validateRequest(GroupCostRequest request) {
        if (request.getDatasources().isEmpty()) return false;
        var requestData = request.getRequestData();
        return hasText(requestData.getGroupGUID())
                && nonNull(requestData.getStartEducationDate())
                && hasText(requestData.getCertificateGUID())
                && hasText(requestData.getFinancialSource());
    }

    private HttpHeaders prepareHttpHeaders(String token) {
        HttpHeaders httpHeaders = Optional.ofNullable(token).map(PguAuthHeadersUtil::prepareAuthBearerHeaders)
                .orElseGet(() -> {
                    log.error("eaisdo.uploader-token настройка не сконфигурирована.");
                    return new HttpHeaders();
                });
        httpHeaders.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
        return httpHeaders;
    }

    private static final NamespacePrefixMapper namespacePrefixMapper = new NamespacePrefixMapper() {
        private final Map<String, String> namespaceMap = Map.of("http://epgu.aisdopobr.ru/dataexchanges/group-cost/1.0.0", "tns");

        @Override
        public String getPreferredPrefix(String namespaceUri, String suggestion, boolean requirePrefix) {
            return namespaceMap.getOrDefault(namespaceUri, suggestion);
        }
    };
}
