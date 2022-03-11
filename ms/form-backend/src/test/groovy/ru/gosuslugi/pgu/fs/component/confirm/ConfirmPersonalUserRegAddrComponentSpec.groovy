package ru.gosuslugi.pgu.fs.component.confirm

import ru.atc.carcass.security.rest.model.EsiaAddress
import ru.gosuslugi.pgu.common.esia.search.dto.UserPersonalData
import ru.gosuslugi.pgu.dto.descriptor.FieldComponent
import ru.gosuslugi.pgu.dto.descriptor.types.ComponentType
import ru.gosuslugi.pgu.fs.common.component.BaseComponent
import ru.gosuslugi.pgu.fs.component.confirm.model.ConfirmPersonalUserRegAddress
import ru.gosuslugi.pgu.fs.esia.EsiaRestContactDataClient
import spock.lang.Specification

class ConfirmPersonalUserRegAddrComponentSpec extends Specification {
    UserPersonalData userPersonalDataMock = Mock(UserPersonalData)
    EsiaRestContactDataClient esiaRestContactDataClient

    def 'Can get initial value'() {
        given:
        BaseComponent<ConfirmPersonalUserRegAddress> confirmPersonalUserRegAddrComponent = new ConfirmPersonalUserRegAddrComponent(userPersonalDataMock)
        def fieldComponent = [type: ComponentType.ConfirmPersonalUserRegAddr, attrs: [
                "addrType": "permanentRegistry",
                fields    : [
                        [fieldName: "regAddr"]
                ]]] as FieldComponent
        userPersonalDataMock.addresses >> [new EsiaAddress(
                type: 'PRG', zipCode: '141070', fiasCode: '00000', addressStr: 'г. Саратов, ул. Чапаева, д. 1')]
        esiaRestContactDataClient = Mock(EsiaRestContactDataClient)

        ConfirmPersonalUserRegAddress expected = new ConfirmPersonalUserRegAddress()
        expected.setRegAddr("141070, г. Саратов, ул. Чапаева, д. 1")

        when:
        def actual = confirmPersonalUserRegAddrComponent.getInitialValue(fieldComponent)

        then:
        expected.regAddr == actual.get().regAddr
        expected.fias == actual.get().fias
        expected.regZipCode == actual.get().regZipCode
    }

    def 'Can get initial value not default address type'() {
        given:
        BaseComponent<ConfirmPersonalUserRegAddress> confirmPersonalUserRegAddrComponent = new ConfirmPersonalUserRegAddrComponent(userPersonalDataMock)
        def fieldComponent = [type: ComponentType.ConfirmPersonalUserRegAddr, attrs: [
                "addrType": "actualResidence",
                fields    : [
                        [fieldName: "regAddr"]
                ]]] as FieldComponent
        userPersonalDataMock.addresses >> [new EsiaAddress(
                type: 'PLV', zipCode: '434340', fiasCode: '11111', addressStr: 'г. Саратов, ул. Восточная, д. 1')]
        esiaRestContactDataClient = Mock(EsiaRestContactDataClient)

        ConfirmPersonalUserRegAddress expected = new ConfirmPersonalUserRegAddress()
        expected.setRegAddr("434340, г. Саратов, ул. Восточная, д. 1")

        when:
        def actual = confirmPersonalUserRegAddrComponent.getInitialValue(fieldComponent)

        then:
        expected.regAddr == actual.get().regAddr
        expected.fias == actual.get().fias
        expected.regZipCode == actual.get().regZipCode
    }

}
