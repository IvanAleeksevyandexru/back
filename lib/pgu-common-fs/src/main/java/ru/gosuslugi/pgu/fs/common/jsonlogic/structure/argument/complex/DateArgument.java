package ru.gosuslugi.pgu.fs.common.jsonlogic.structure.argument.complex;

import lombok.Data;


@Data
public class DateArgument implements Complex<String> {

    private String value;
    private String accuracy;
    private DateArgumentAdd add;

    @Override
    public ComplexType getComplexType() {
        return ComplexType.Date;
    }

    public String getAccuracy() {
        return accuracy.isBlank() ? "day" : accuracy;
    }
}
