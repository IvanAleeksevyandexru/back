package ru.gosuslugi.pgu.pgu_common.nsi.dto.filter;

import lombok.Data;


@Data
public class NsiDictionaryFilterSimple {
    private String attributeName;
    private String condition;
    private NsiDictionaryFilterSimpleValue value;

    public static class Builder {
        private static final String AS_STRING = "asString";
        private String attributeName;
        private String condition;
        private String attributeTypeValue;
        private Object value;

        public Builder setAttributeName(String attributeName) {
            this.attributeName = attributeName;
            return this;
        }

        public Builder setCondition(String condition) {
            this.condition = condition;
            return this;
        }

        public Builder setValue(String attributeTypeValue, Object value) {
            this.attributeTypeValue = attributeTypeValue == null ? AS_STRING : attributeTypeValue;
            this.value = value;
            return this;
        }

        public Builder setStringValue(String stringValue) {
            return setValue(AS_STRING, stringValue);
        }

        public NsiDictionaryFilterSimple build() {
            NsiDictionaryFilterSimple nsiDictionaryFilterSimple = new NsiDictionaryFilterSimple();

            nsiDictionaryFilterSimple.setAttributeName(this.attributeName);
            nsiDictionaryFilterSimple.setCondition(this.condition);
            NsiDictionaryFilterSimpleValue presetValue = new NsiDictionaryFilterSimpleValue();
            presetValue.putAttributeValue(this.attributeTypeValue, this.value);
            nsiDictionaryFilterSimple.setValue(presetValue);
            return nsiDictionaryFilterSimple;
        }
    }
}
