package ru.gosuslugi.pgu.fs.utils;

import org.apache.commons.lang3.StringUtils;
import org.json.JSONException;
import org.json.JSONObject;
import ru.gosuslugi.pgu.dto.ScenarioDto;
import ru.gosuslugi.pgu.fs.common.exception.FormBaseException;

import java.util.Map;

import static ru.gosuslugi.pgu.components.ComponentAttributes.AS_STRING_FILTER_ATTRIBUTE_NAME;
import static ru.gosuslugi.pgu.components.ComponentAttributes.NOT_CORRECT_FILTER_VALUE_TYPE;

public enum AttributeValueTypes {

    VALUE {
        @Override
        public String getPresetValue(ParseAttrValuesHelper parseAttrValuesHelper, String valueType, String attributeType, String presetAttrNameVal, ScenarioDto scenarioDto,
                                     Map<String, String> presetProperties) throws JSONException {
            String type = attributeType;
            if (StringUtils.isBlank(type)) {
                type = AS_STRING_FILTER_ATTRIBUTE_NAME;
            }
            JSONObject jsonObjValue = new JSONObject(presetAttrNameVal);
            return jsonObjValue.getString(type);
        }
    },
    REF {
        @Override
        public String getPresetValue(ParseAttrValuesHelper parseAttrValuesHelper, String valueType, String attributeType, String presetAttrNameVal, ScenarioDto scenarioDto,
                                     Map<String, String> presetProperties) {
            return parseAttrValuesHelper.getStringValueOfRefAttributeByFullPath(presetAttrNameVal, scenarioDto);
        }
    },
    ROOT {
        @Override
        public String getPresetValue(ParseAttrValuesHelper parseAttrValuesHelper, String valueType, String attributeType, String presetAttrNameVal, ScenarioDto scenarioDto,
                                     Map<String, String> presetProperties) {
            return ScenarioFieldUtil.getValueByFieldName(presetAttrNameVal, scenarioDto);
        }
    },
    PRESET {
        @Override
        public String getPresetValue(ParseAttrValuesHelper parseAttrValuesHelper, String valueType, String attributeType, String presetAttrNameVal, ScenarioDto scenarioDto,
                                     Map<String, String> presetProperties) {
            return presetProperties.get(presetAttrNameVal);
        }
    },
    CALC,
    PROTECTED,
    SERVICEINFO,
    ARGUMENT;

    public String getPresetValue(ParseAttrValuesHelper parseAttrValuesHelper, String valueType, String attributeType, String presetAttrNameVal, ScenarioDto scenarioDto,
                                 Map<String, String> presetProperties) throws JSONException {
        throw new FormBaseException(String.format(NOT_CORRECT_FILTER_VALUE_TYPE, valueType));
    }
}
