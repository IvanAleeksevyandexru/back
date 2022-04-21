package ru.gosuslugi.pgu.fs.common.jsonlogic.structure.argument.complex;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.util.StringUtils;
import ru.gosuslugi.pgu.common.core.date.util.DateUtil;

@EqualsAndHashCode(callSuper = true)
@Data
public class DateToStringArgument extends DateArgument {
    private String format;

    @Override
    public ComplexType getComplexType() {
        return ComplexType.DateToString;
    }

    public String getFormat() {
        return StringUtils.hasText(format) ? format : DateUtil.ESIA_DATE_FORMAT;
    }
}
