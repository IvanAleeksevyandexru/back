package ru.gosuslugi.pgu.fs.component.confirm


import ru.gosuslugi.pgu.dto.ScenarioDto
import ru.gosuslugi.pgu.dto.descriptor.FieldComponent
import ru.gosuslugi.pgu.dto.descriptor.ServiceDescriptor
import ru.gosuslugi.pgu.fs.component.ComponentTestUtil
import ru.gosuslugi.pgu.fs.service.PersonContactService
import spock.lang.Specification

import static ru.gosuslugi.pgu.dto.descriptor.types.ComponentType.ConfirmNewEmail
import static ru.gosuslugi.pgu.fs.component.ComponentTestUtil.answerEntry

class ConfirmNewEmailComponentTest extends Specification {

    def 'Get initial value for ConfirmNewEmailComponent' () {
        given:
        ConfirmNewEmailComponent component = new ConfirmNewEmailComponent(null)
        ComponentTestUtil.setAbstractComponentServices(component)

        def fieldComponent = new FieldComponent(type: ConfirmNewEmail, arguments: ["email": "test@test.ru"])

        when:
        component.process(fieldComponent, new ScenarioDto(), new ServiceDescriptor())

        then:
        fieldComponent.value == 'test@test.ru'
    }

    def 'Validate submitted value for ConfirmNewEmailComponent' () {
        given:
        def personContactService = Mock(PersonContactService)
        ConfirmNewEmailComponent component = new ConfirmNewEmailComponent(personContactService)
        ComponentTestUtil.setAbstractComponentServices(component)

        def fieldComponent = new FieldComponent(type: ConfirmNewEmail)
        def scenarioDto = new ScenarioDto()

        when:
        def result = component.validate(answerEntry('email', null), scenarioDto, fieldComponent)
        then:
        0 * personContactService.isEmailConfirmedAndLinkedToUser(_)
        assert result == ['email': 'Введите адрес электронной почты']

        when:
        result = component.validate(answerEntry('email', 'test@test.ru'), scenarioDto, fieldComponent)
        then:
        1 * personContactService.isEmailConfirmedAndLinkedToUser('test@test.ru') >> false
        assert result == ['email': 'Адрес электроной почты не подтвержден пользователем']

        when:
        result = component.validate(answerEntry('email', 'test@test.ru'), scenarioDto, fieldComponent)
        then:
        1 * personContactService.isEmailConfirmedAndLinkedToUser('test@test.ru') >> true
        assert result == [:]
    }
}
