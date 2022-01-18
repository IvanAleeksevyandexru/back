package ru.gosuslugi.pgu.fs.component.address

import ru.gosuslugi.pgu.common.core.json.JsonFileUtil
import ru.gosuslugi.pgu.common.core.json.JsonProcessingUtil
import ru.gosuslugi.pgu.dto.ApplicantAnswer
import ru.gosuslugi.pgu.dto.ScenarioDto
import ru.gosuslugi.pgu.dto.descriptor.FieldComponent
import ru.gosuslugi.pgu.dto.descriptor.types.ComponentType
import ru.gosuslugi.pgu.fs.common.utils.AnswerUtil
import ru.gosuslugi.pgu.fs.service.LkNotifierService
import ru.gosuslugi.pgu.pgu_common.nsi.service.NsiDadataService
import spock.lang.Specification

class CityInputComponentSpec extends Specification{
    String DADATA_JSON = JsonFileUtil.getJsonFromFile(this.getClass(), "-dadata.json")
    ScenarioDto SCENARIO_DTO = JsonProcessingUtil.fromJson(JsonFileUtil.getJsonFromFile(this.getClass(), "-scenario-dto.json"),ScenarioDto)
    LkNotifierService lkNotifierService = Mock(LkNotifierService)
    NsiDadataService nsiDadataService = Mock(NsiDadataService)
    CityInputComponent cityInputComponent = new CityInputComponent(nsiDadataService,lkNotifierService)
    FieldComponent fieldComponent = new FieldComponent(
            type: ComponentType.CityInput,
            attrs: ["hideLevels": ["inCityDist", "street", "additionalArea", "additionalStreet", "house", "building1",
                                   "building2", "apartment", "index"],
                    "validation": ["type": "RegExp", "value": ".+", "errorMsg": "Поле не может быть пустым"],
                    "searchType": "town"
            ]
    )

    def 'Check validateAfterSubmit'(){
        given:
        Map<String, String> incorrectAnswers = new HashMap<>()
        Map.Entry<String, ApplicantAnswer> entry= AnswerUtil.createAnswerEntry('value', DADATA_JSON)
        when:
        cityInputComponent.validateAfterSubmit(incorrectAnswers,entry,fieldComponent)

        then:
        incorrectAnswers.isEmpty()

        when:
        entry = AnswerUtil.createAnswerEntry('value', DADATA_JSON.replace("\"dadataQc\": \"0\"","\"dadataQc\": \"456789\""))
        cityInputComponent.validateAfterSubmit(incorrectAnswers,entry,fieldComponent)

        then:
        'Адрес не распознан'.equals(incorrectAnswers.get('value'))

        when:
        entry.getValue().setValue("")
        cityInputComponent.validateAfterSubmit(incorrectAnswers,entry,fieldComponent)

        then:
        'Адрес не задан'.equals(incorrectAnswers.get('value'))
    }

    def 'Check getInitialValue'(){
        when:
        cityInputComponent.getInitialValue(fieldComponent,SCENARIO_DTO,null)

        then:
        0 * nsiDadataService.getAddress('Ярославская область/Ярославль г')

        when:
        fieldComponent.getAttrs().put('useBarLocation',true)
        cityInputComponent.getInitialValue(fieldComponent,SCENARIO_DTO,null)

        then:
        1 * nsiDadataService.getAddress('Ярославская область/Ярославль г')
    }

    def 'Check postProcess'(){
        given:
        Map.Entry<String, ApplicantAnswer> entry= AnswerUtil.createAnswerEntry('value', DADATA_JSON)

        when:
        cityInputComponent.postProcess(entry,SCENARIO_DTO,fieldComponent)

        then:
        0 * lkNotifierService.updateOrderRegion(764234668,40000000000,null)

        when:
        cityInputComponent.postProcess(entry,SCENARIO_DTO,fieldComponent)

        then:
        0 * lkNotifierService.updateOrderRegion(764234668,'40000000000')
    }
}
