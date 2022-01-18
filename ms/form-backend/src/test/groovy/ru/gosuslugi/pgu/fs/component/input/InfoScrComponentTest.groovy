package ru.gosuslugi.pgu.fs.component.input

import ru.atc.carcass.security.rest.model.EsiaAddress
import ru.atc.carcass.security.rest.model.person.Kids
import ru.atc.carcass.security.rest.model.person.Person
import ru.gosuslugi.pgu.dto.ScenarioDto
import ru.gosuslugi.pgu.common.esia.search.dto.UserPersonalData
import ru.gosuslugi.pgu.dto.descriptor.FieldComponent
import ru.gosuslugi.pgu.dto.descriptor.ServiceDescriptor
import ru.gosuslugi.pgu.dto.descriptor.types.ComponentType
import ru.gosuslugi.pgu.fs.common.component.input.InfoScrComponent
import spock.lang.Ignore
import spock.lang.Specification

// TODO Tests: Переписать тест
@Ignore
class InfoScrComponentTest extends Specification {

    def 'InfoScr helper test' () {
        given:
        InfoScrComponent helper = new InfoScrComponent()
        UserPersonalData userPersonalData = new UserPersonalData()
        userPersonalData.setKids(new LinkedList<Kids>())
        userPersonalData.setPerson(new Person())
        userPersonalData.setAddresses(new LinkedList<EsiaAddress>())
        FieldComponent fieldComponent = new FieldComponent()
        fieldComponent.setType(ComponentType.InfoScr)
        Map<String, Object> attrs = new LinkedHashMap<>()
        attrs.put("refs", new LinkedHashMap<String, String>())
        fieldComponent.setAttrs(attrs)
        fieldComponent.setLabel("Label")
        ScenarioDto scenarioDto = new ScenarioDto()
        ServiceDescriptor serviceDescriptor = new ServiceDescriptor()
        serviceDescriptor.setApplicationFields(new LinkedList<FieldComponent>())
        expect:
//        assert helper.preSetComponentValue(fieldComponent, scenarioDto, serviceDescriptor) == ""
        assert helper.getType() == ComponentType.InfoScr
    }
}
