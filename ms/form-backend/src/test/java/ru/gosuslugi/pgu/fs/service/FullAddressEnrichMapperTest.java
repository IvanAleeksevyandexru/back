package ru.gosuslugi.pgu.fs.service;

import org.junit.Test;
import org.mapstruct.factory.Mappers;
import ru.gosuslugi.pgu.components.descriptor.types.FullAddress;

import static org.junit.Assert.assertEquals;

// As Spring
//@RunWith(SpringRunner.class)
//@ContextConfiguration(classes = {FullAddressEnrichMapperImpl.class})
public class FullAddressEnrichMapperTest {

    //@Autowired
    //FullAddressEnrichMapper mapper;

    private FullAddressEnrichMapper mapper = Mappers.getMapper(FullAddressEnrichMapper.class);

    @Test
    public void testUpdateField() {
        FullAddress address = new FullAddress();
        address.setRegion("region");
        mapper.updateAddress(address,  address);
        assertEquals("область", address.getRegionType());
    }
}
