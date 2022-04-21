package ru.gosuslugi.pgu.common.certificate.converters;

import lombok.extern.slf4j.Slf4j;
import org.junit.Assert;
import org.junit.Test;
import org.mapstruct.factory.Mappers;
import ru.gosuslugi.pgu.common.certificate.service.mapper.RegAddressMapper;
import ru.gosuslugi.pgu.common.certificate.xsd.dto.RegAddress;

@Slf4j
public class RegAddressConverterTest {

    @Test
    public void convertTest() {
        String regAddrString = "{\"regAddr\":{\"cityType\":\"город\",\"streetShortType\":\"ул\",\"additionalStreetShortType\":\"\",\"postalCode\":null,\"districtType\":\"район\"," +
                "\"geoLat\":\"52.5912896\",\"building2Type\":\"\",\"okato\":\"80404000000\",\"building1Type\":\"\"," +
                "\"regionType\":\"республика\",\"additionalStreet\":\"\",\"lat\":\"52.5912896\"," +
                "\"apartmentCheckboxClosed\":false,\"houseCheckboxClosed\":false," +
                "\"streetType\":\"улица\",\"town\":\"\",\"lng\":\"58.3110998\",\"index\":\"453632\"," +
                "\"townShortType\":\"\",\"district\":\"Баймакский\"," +
                "\"fullAddress\":\"453632, Респ. Башкортостан, г. Баймак, р-н. Баймакский, ул. А.Алибаева, д. 1, кв. 1\"," +
                "\"houseCheckbox\":false,\"oktmoName\":null,\"region\":\"Башкортостан\"," +
                "\"geoLon\":\"58.3110998\",\"additionalArea\":\"\",\"apartmentType\":\"квартира\"," +
                "\"city\":\"Баймак\",\"apartmentShortType\":\"кв\",\"house\":\"1\",\"apartmentCheckbox\":false," +
                "\"townType\":\"\",\"additionalAreaType\":\"\",\"regionShortType\":\"Респ\"," +
                "\"regionCode\":\"02\",\"districtShortType\":\"р-н\",\"street\":\"А.Алибаева\"," +
                "\"additionalStreetType\":\"\",\"inCityDistShortType\":\"\",\"additionalAreaShortType\":\"\"," +
                "\"cityShortType\":\"г\",\"kladrCode\":\"0200600100000030001\",\"building1ShortType\":\"\"," +
                "\"fiasCode\":\"72cc79b7-c0d5-4a7f-9934-aca2b2d2d2aa\"," +
                "\"regionFias\":\"72cc79b7-c0d5-4a7f-9934-aca2b2d2d2ab\"," +
                "\"hasErrors\":0,\"inCityDistType\":\"\",\"houseType\":\"дом\",\"inCityDist\":\"\",\"addressStr\":\"Респ. Башкортостан, г. Баймак, р-н. Баймакский, ул. А.Алибаева\",\"oktmo\":null,\"building1\":\"\",\"building2\":\"\",\"houseShortType\":\"д\",\"building2ShortType\":\"\",\"apartment\":\"1\"}}";
        RegAddressMapper converter = Mappers.getMapper(RegAddressMapper.class);
        RegAddress regAddress = converter.convert(regAddrString);
        log.info("{}", regAddress.getFullAddress());
        Assert.assertEquals("72cc79b7-c0d5-4a7f-9934-aca2b2d2d2ab", regAddress.getFiasCode());

    }

}