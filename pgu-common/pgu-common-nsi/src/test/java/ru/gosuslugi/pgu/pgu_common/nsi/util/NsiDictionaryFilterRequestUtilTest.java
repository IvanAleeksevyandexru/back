package ru.gosuslugi.pgu.pgu_common.nsi.util;

import org.junit.Assert;
import org.junit.Test;
import ru.gosuslugi.pgu.pgu_common.nsi.dto.filter.*;

import java.net.URISyntaxException;
import java.util.Collections;

/**
 * Tests for {@link NsiDictionaryFilterRequestUtil} methods
 *
 * @author ebalovnev
 */
public class NsiDictionaryFilterRequestUtilTest {

    @Test
    public void test() throws URISyntaxException {
        NsiDictionaryFilterRequest filterRequest = new NsiDictionaryFilterRequest();

        Assert.assertNull(NsiDictionaryFilterRequestUtil.getOkato(filterRequest));
    }

    @Test
    public void testNsiSimpleDictionaryFilterContainer() throws URISyntaxException {
        String newOkato = "new_okato";
        NsiDictionaryFilterRequest filterRequest = getNsiSimpleDictionaryFilterContainer(NsiDictionaryFilterRequestUtil.OKATO_ATTRIBUTE_NAME, newOkato);

        Assert.assertEquals(newOkato, NsiDictionaryFilterRequestUtil.getOkato(filterRequest));
    }

    @Test
    public void testNsiSimpleDictionaryFilterContainerNo() throws URISyntaxException {
        String newOkato = "new_okato";
        NsiDictionaryFilterRequest filterRequest = getNsiSimpleDictionaryFilterContainer("Another", newOkato);

        Assert.assertNull(NsiDictionaryFilterRequestUtil.getOkato(filterRequest));
    }


    private NsiDictionaryFilterRequest getNsiSimpleDictionaryFilterContainer(String okatoAttributeName, String newOkato) {
        NsiDictionaryFilterRequest filterRequest = new NsiDictionaryFilterRequest();
        NsiSimpleDictionaryFilterContainer filter = new NsiSimpleDictionaryFilterContainer();
        NsiDictionaryFilterSimple nsiDictionaryFilterSimple = new NsiDictionaryFilterSimple();
        nsiDictionaryFilterSimple.setAttributeName(okatoAttributeName);
        NsiDictionaryFilterSimpleValue value = new NsiDictionaryFilterSimpleValue();
        value.putAttributeValue("asString", newOkato);
        nsiDictionaryFilterSimple.setValue(value);
        filter.setSimple(nsiDictionaryFilterSimple);
        filterRequest.setFilter(filter);
        return filterRequest;
    }

    @Test
    public void testNsiUnionDictionaryFilterContainer() throws URISyntaxException {
        String newOkato = "new_okato";
        NsiDictionaryFilterRequest filterRequest = getNsiUnionDictionaryFilterContainer(NsiDictionaryFilterRequestUtil.OKATO_ATTRIBUTE_NAME, newOkato);

        Assert.assertEquals(newOkato, NsiDictionaryFilterRequestUtil.getOkato(filterRequest));
    }

    @Test
    public void testNsiUnionDictionaryFilterContainerNo() throws URISyntaxException {
        String newOkato = "new_okato";
        NsiDictionaryFilterRequest filterRequest = getNsiUnionDictionaryFilterContainer("Another", newOkato);

        Assert.assertNull(NsiDictionaryFilterRequestUtil.getOkato(filterRequest));
    }

    private NsiDictionaryFilterRequest getNsiUnionDictionaryFilterContainer(String okatoAttributeName, String newOkato) {
        NsiDictionaryFilterRequest filterRequest = new NsiDictionaryFilterRequest();
        NsiDictionaryFilterSimple.Builder conditionBuilder =
            new NsiDictionaryFilterSimple.Builder()
                .setAttributeName(okatoAttributeName)
                .setCondition(NsiFilterCondition.EQUALS.toString())
                .setStringValue(newOkato);

        filterRequest.setFilter(
            new NsiUnionDictionaryFilterContainer.Builder()
                .setFilterBuilders(Collections.singletonList(conditionBuilder))
                .setNsiDictionaryUnionType(NsiDictionaryUnionType.AND)
                .build()
        );
        return filterRequest;
    }
}
