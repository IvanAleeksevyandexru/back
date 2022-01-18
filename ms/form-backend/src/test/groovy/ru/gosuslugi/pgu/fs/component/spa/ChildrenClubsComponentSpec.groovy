package ru.gosuslugi.pgu.fs.component.spa

import com.fasterxml.jackson.databind.ObjectMapper
import ru.gosuslugi.pgu.dto.descriptor.FieldComponent
import ru.gosuslugi.pgu.dto.descriptor.types.ComponentType
import ru.gosuslugi.pgu.fs.common.component.AbstractComponent
import ru.gosuslugi.pgu.fs.common.component.ComponentResponse
import ru.gosuslugi.pgu.fs.common.component.validation.PredicateValidation
import ru.gosuslugi.pgu.fs.common.service.JsonProcessingService
import ru.gosuslugi.pgu.fs.common.service.impl.JsonProcessingServiceImpl
import spock.lang.Specification

class ChildrenClubsComponentSpec extends Specification{
    ObjectMapper objectMapper = new ObjectMapper()
    JsonProcessingService jsonProcessingService = new JsonProcessingServiceImpl(objectMapper)
    PredicateValidation predicateValidation = Mock(PredicateValidation)
    ChildrenClubsComponent childrenClubsComponent = new ChildrenClubsComponent(predicateValidation)
    FieldComponent fieldComponent = new FieldComponent(
            type: ComponentType.ChildrenClubs,
            arguments: ['vendor' : 'inlearno',
                        'pageSize':'10',
                        'nextSchoolYear' : 'false',
                        'okato' : '79000000000',
                        'denyReason':'потому что'
            ]
    )

    def 'Check getInitialValue'(){
        given:
        childrenClubsComponent.jsonProcessingService=jsonProcessingService

        when:
        ComponentResponse<String> response = childrenClubsComponent.getInitialValue(fieldComponent)

        then:
        '{"state":{"vendor":"inlearno","pageSize":"10","nextSchoolYear":"false","okato":"79000000000","denyReason":"потому что"}}' == response.get()
    }

}
