package ru.gosuslugi.pgu.fs.component.dictionary

import com.fasterxml.jackson.core.type.TypeReference
import ru.gosuslugi.pgu.dto.descriptor.FieldComponent
import ru.gosuslugi.pgu.fs.component.dictionary.dto.AttrValue
import ru.gosuslugi.pgu.fs.component.dictionary.dto.DictionaryFilter
import ru.gosuslugi.pgu.fs.component.dictionary.dto.MapServiceAttributes
import ru.gosuslugi.pgu.fs.component.dictionary.dto.PresetField
import spock.lang.Specification

import static ru.gosuslugi.pgu.fs.utils.AttributeValueTypes.PRESET
import static ru.gosuslugi.pgu.fs.utils.AttributeValueTypes.REF
import static ru.gosuslugi.pgu.pgu_common.nsi.dto.filter.NsiFilterCondition.EQUALS

class AttributesDtoSpec extends Specification {

    def 'successfully convert attrs as Map to attrs as DTO'() {
        given:
        AttributesDto<MapServiceAttributes> attrsToDtoConverter = new AttributesDto<MapServiceAttributes>() {
            @Override
            TypeReference<MapServiceAttributes> getAttributesDtoType() {
                return new TypeReference<MapServiceAttributes>() {};
            }
        }
        Map<String, Object> mapServiceAttrsAsMap = [
                "addressString"               : [
                        "type" : "REF",
                        "value": "address"
                ],
                "dictionaryFilters"           : [
                        [
                                "attributeName": "okato_in",
                                "condition"    : "EQUALS",
                                "value"        : "regOkato",
                                "valueType"    : "preset",
                        ]
                ],
                "fields"                      : [
                        [
                                "fieldName": "userId",
                                "label"    : "some label",
                                "type"     : "hidden"
                        ]
                ],
                "validationOn"                : false, // по умолчанию true
                "dictionaryType"              : "pgu_mvd_org",
                "personalData"                : [
                        "type" : "REF",
                        "value": "pd1.storedValues"
                ],
                "federalLevelUnitsFias"       : [
                        "c2deb16a-0330-4f05-821f-1d09c93331e6",
                        "0c5b2444-70a0-4932-980c-b4dc0d3f02b5"
                ],
                "districtLevelUnitsFias"      : [
                        "27c5bc66-61bf-4a17-b0cd-ca0eb64192d6"
                ],
                "isMvdFiltersActivatedOnFront": [
                        "expr"     : ["some expr"],
                        "valueType": "calc"
                ]
        ]
        FieldComponent fieldComponent = new FieldComponent(
                attrs: mapServiceAttrsAsMap
        );

        when:
        MapServiceAttributes attrs = attrsToDtoConverter.getAttributesDto(fieldComponent)

        then:
        attrs.getAddressString() == new AttrValue(REF, List.of("address"))
        attrs.getDictionaryFilters() == List.of(new DictionaryFilter("okato_in", EQUALS, "regOkato", PRESET))
        attrs.getFields() == List.of(new PresetField("userId", "some label", "hidden"))
        !attrs.isValidationOn()
        attrs.getDictionaryType() == "pgu_mvd_org"
        attrs.getPersonalData() == new AttrValue(REF, List.of("pd1.storedValues"))
        attrs.getFederalLevelUnitsFias() == Set.of("c2deb16a-0330-4f05-821f-1d09c93331e6", "0c5b2444-70a0-4932-980c-b4dc0d3f02b5")
        attrs.getDistrictLevelUnitsFias() == Set.of("27c5bc66-61bf-4a17-b0cd-ca0eb64192d6")
    }
}
