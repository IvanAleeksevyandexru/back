package ru.gosuslugi.pgu.fs.common.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.http.HttpMethod;
import ru.atc.idecs.common.util.ws.type.AttributeType;
import ru.atc.idecs.common.util.ws.type.AttributeValue;
import ru.atc.idecs.refregistry.ws.Condition;
import ru.atc.idecs.refregistry.ws.ListRefItemsRequest;
import ru.atc.idecs.refregistry.ws.ListRefItemsResponse;
import ru.atc.idecs.refregistry.ws.LogicalUnionPredicate;
import ru.atc.idecs.refregistry.ws.Predicate;
import ru.atc.idecs.refregistry.ws.RefItem;
import ru.atc.idecs.refregistry.ws.SimplePredicate;
import ru.gosuslugi.pgu.fs.common.exception.FormBaseException;
import ru.gosuslugi.pgu.common.core.exception.NsiExternalException;
import ru.gosuslugi.pgu.dto.ScenarioDto;
import ru.gosuslugi.pgu.dto.descriptor.ComputeItem;
import ru.gosuslugi.pgu.fs.common.service.FormServiceNsiClient;

import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;
import java.time.ZonedDateTime;
import java.util.GregorianCalendar;
import java.util.Objects;
import java.util.Optional;

@RequiredArgsConstructor
public class ComputeDictionaryItemService {

    private final FormServiceNsiClient nsiClient;

    @SneakyThrows
    public ComputeItem computeDictionaryItem(ComputeItem nsiComputeItem, ScenarioDto scenarioDto) {
        Predicate filter = nsiComputeItem.getNsiRequest().getFilter();
        if (Objects.nonNull(filter)) {
            processSimplePredicate(filter.getSimple());
            processUnionPredicate(filter.getUnion());
        }
        String dictionary = nsiComputeItem.getValue();
        String nsiItemsResult = nsiClient.getFirstItem(nsiComputeItem.getNsiRequest(), dictionary);
        nsiComputeItem.setResult(nsiItemsResult);
        return nsiComputeItem;
    }

    public ListRefItemsResponse getDictionaryItems(ListRefItemsRequest request, final String dictionaryName) {
        try {
            return nsiClient.getItems(request, dictionaryName);
        } catch (Exception e) {
            throw new NsiExternalException(dictionaryName, null, HttpMethod.POST, "Исключение при вызове метода", null, e);
        }
    }


    public Optional<RefItem> getDictionaryItem(String attrName, String attrValue, Condition condition, String dictionaryName) {
        try {
            return nsiClient.getItem(attrName, attrValue, condition, dictionaryName);
        } catch (Exception e) {
            throw new NsiExternalException(dictionaryName, null, HttpMethod.POST, "Исключение при вызове метода", null, e);
        }
    }

    private void processSimplePredicate(final SimplePredicate simplePredicate) {
        if (Objects.isNull(simplePredicate)) return;

        XMLGregorianCalendar fieldValue = simplePredicate.getValue().getAsDateTime();
        if (Objects.nonNull(fieldValue) && 0L == fieldValue.toGregorianCalendar().getTimeInMillis()) {
            try {
                GregorianCalendar calendar = GregorianCalendar.from(ZonedDateTime.now());
                XMLGregorianCalendar currendDate = DatatypeFactory.newInstance().newXMLGregorianCalendar(calendar);
                simplePredicate.getValue().setAsDateTime(currendDate);
            } catch (DatatypeConfigurationException ex) {
                throw new FormBaseException("Dictionary filter init error", ex);
            }
        }

    }

    private void processUnionPredicate(LogicalUnionPredicate unionPredicate) {
        if (Objects.isNull(unionPredicate)) return;

        unionPredicate.getSubs().forEach(el -> {
            AttributeValue originalAttributeValue = el.getSimple().getValue();
            AttributeValueWrapper valueWrapper = new AttributeValueWrapper(originalAttributeValue);
            el.getSimple().setValue(valueWrapper);
            processSimplePredicate(el.getSimple());
        });
    }

    public static class AttributeValueWrapper extends AttributeValue {

        public AttributeValueWrapper(AttributeValue originalAttributeValue) {
            this.setAsDateTime(originalAttributeValue.getAsDateTime());
            this.setAsDate(originalAttributeValue.getAsDate());
            this.setAsBoolean(originalAttributeValue.isAsBoolean());
            this.setAsDecimal(originalAttributeValue.getAsDecimal());
            this.setAsLong(originalAttributeValue.getAsLong());
            this.setAsString(originalAttributeValue.getAsString());
        }

        @Override
        public Object getValue() {
            return null;
        }

        @Override
        public AttributeType getTypeOfValue() {
            return null;
        }
    }
}
