package ru.gosuslugi.pgu.fs.service.impl;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;
import ru.atc.carcass.security.rest.model.EsiaAddress;
import ru.gosuslugi.pgu.common.core.service.OkatoHolder;
import ru.gosuslugi.pgu.common.esia.search.dto.UserPersonalData;
import ru.gosuslugi.pgu.pgu_common.nsi.dto.DadataAddressResponse;
import ru.gosuslugi.pgu.pgu_common.nsi.service.NsiDadataService;

import java.util.Arrays;
import java.util.Collections;

/**
 * Tests for {@link PersonOkatoServiceImpl} methods
 *
 * @author ebalovnev
 */
public class PersonOkatoServiceImplTest {

    private static final String REGISTRATION_ADDRESS = "115533, г. Москва, пр-кт. Андропова, д. 5, кв. 25";
    private static final String LOCAL_ADDRESS = "623610, обл. Свердловская, р-н. Талицкий, с. Бутка, ул. Ленина, д. 15, к. 2, стр. 14, кв. 3";

    private UserPersonalData userPersonalData;
    private NsiDadataService nsiDadataService;

    private PersonOkatoServiceImpl personOkatoService;

    @Before
    public void init () {
        userPersonalData  = Mockito.mock(UserPersonalData.class);
        nsiDadataService  = Mockito.mock(NsiDadataService.class);
        personOkatoService = new PersonOkatoServiceImpl(userPersonalData, nsiDadataService);
    }

    @Test
    public void testNoAddresses() {
        Mockito.when(userPersonalData.getAddresses())
            .thenReturn(Collections.emptyList());

        Assert.assertEquals(OkatoHolder.DEFAULT_OKATO, personOkatoService.getOkato());

        // Проверяем замоканные вызванные функции
        Mockito.verify(userPersonalData, Mockito.times(1))
            .getAddresses();
        Mockito.verify(nsiDadataService, Mockito.times(0))
            .getAddress(Mockito.anyString(), Mockito.anyString());
    }

    @Test
    public void testTwice() {
        Mockito.when(userPersonalData.getAddresses())
                .thenReturn(Collections.emptyList());

        personOkatoService.getOkato();
        personOkatoService.getOkato();

        // Проверяем замоканные вызванные функции
        Mockito.verify(userPersonalData, Mockito.times(1))
            .getAddresses();
        Mockito.verify(nsiDadataService, Mockito.times(0))
            .getAddress(Mockito.anyString(), Mockito.anyString());
    }

    @Test
    public void testUnsuitableAddress() {
        EsiaAddress esiaAddress = new EsiaAddress();
        esiaAddress.setType("1");
        Mockito.when(userPersonalData.getAddresses())
            .thenReturn(Collections.singletonList(esiaAddress));

        Assert.assertEquals(OkatoHolder.DEFAULT_OKATO, personOkatoService.getOkato());

        // Проверяем замоканные вызванные функции
        Mockito.verify(userPersonalData, Mockito.times(1))
            .getAddresses();
        Mockito.verify(nsiDadataService, Mockito.times(0))
            .getAddress(Mockito.anyString(), Mockito.anyString());
    }

    @Test
    public void testAddressException() {
        String newOkato = "new_okato";
        Mockito.when(userPersonalData.getAddresses())
                .thenReturn(Collections.singletonList(getRegEsiaAddress()));
        Mockito.when(nsiDadataService.getAddress(Mockito.anyString(), Mockito.anyString()))
                .thenThrow(new RuntimeException("Test exception"));

        Assert.assertEquals(OkatoHolder.DEFAULT_OKATO, personOkatoService.getOkato());

        // Проверяем замоканные вызванные функции
        Mockito.verify(userPersonalData, Mockito.times(1))
            .getAddresses();
        Mockito.verify(nsiDadataService, Mockito.times(1))
            .getAddress(ArgumentMatchers.eq(REGISTRATION_ADDRESS), ArgumentMatchers.eq(OkatoHolder.DEFAULT_OKATO));
    }


    @Test
    public void testAddress() {
        String newOkato = "new_okato";
        Mockito.when(userPersonalData.getAddresses())
            .thenReturn(Collections.singletonList(getRegEsiaAddress()));
        DadataAddressResponse response = new DadataAddressResponse();
        response.setOkato(newOkato);
        Mockito.when(nsiDadataService.getAddress(Mockito.anyString(), Mockito.anyString()))
            .thenReturn(response);

        Assert.assertEquals(newOkato, personOkatoService.getOkato());

        // Проверяем замоканные вызванные функции
        Mockito.verify(userPersonalData, Mockito.times(1))
            .getAddresses();
        Mockito.verify(nsiDadataService, Mockito.times(1))
            .getAddress(ArgumentMatchers.eq(REGISTRATION_ADDRESS), ArgumentMatchers.eq(OkatoHolder.DEFAULT_OKATO));
    }

    @Test
    public void testTwoAddresses() {
        String newOkato = "new_okato";
        Mockito.when(userPersonalData.getAddresses())
            .thenReturn(Arrays.asList(getLocalEsiaAddress(),  getRegEsiaAddress()));
        DadataAddressResponse response = new DadataAddressResponse();
        response.setOkato(newOkato);
        Mockito.when(nsiDadataService.getAddress(Mockito.anyString(), Mockito.anyString()))
                .thenReturn(response);

        Assert.assertEquals(newOkato, personOkatoService.getOkato());

        // Проверяем замоканные вызванные функции
        Mockito.verify(userPersonalData, Mockito.times(1))
            .getAddresses();
        Mockito.verify(nsiDadataService, Mockito.times(1))
            .getAddress(ArgumentMatchers.eq(REGISTRATION_ADDRESS), ArgumentMatchers.eq(OkatoHolder.DEFAULT_OKATO));
    }

    private EsiaAddress getLocalEsiaAddress() {
        EsiaAddress localAddress = new EsiaAddress();
        localAddress.setType(EsiaAddress.Type.LOCATION.getCode());
        localAddress.setAddressStr(LOCAL_ADDRESS);
        return localAddress;
    }

    private EsiaAddress getRegEsiaAddress() {
        EsiaAddress esiaAddress = new EsiaAddress();
        esiaAddress.setType(EsiaAddress.Type.REGISTRATION.getCode());
        esiaAddress.setAddressStr(REGISTRATION_ADDRESS);
        return esiaAddress;
    }
}
