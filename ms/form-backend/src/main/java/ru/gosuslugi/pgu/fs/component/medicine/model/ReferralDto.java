package ru.gosuslugi.pgu.fs.component.medicine.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import lombok.val;
import ru.gosuslugi.pgu.fs.utils.DateTimeUtil;

import java.time.LocalDate;
import java.util.Objects;

@Data
public class ReferralDto {
    private static final String DATE_FORMAT = "yyyy-MM-dd";

    private String referralId;
    private String referralNumber;
    private String referralTypeId;
    private String referralStartDate;
    private String referralEndDate;
    private String paymentSourceId;
    private String toMoOid;
    private String toMoName;
    private String toServiceId;
    private String toServiceName;
    private String toSpecsId;
    private String toSpecsName;
    private String toResourceName;
    private String fromMoOid;
    private String fromMoName;
    private String fromSpecsId;
    private String fromSpecsName;
    private String fromResourceName;

    @JsonIgnore
    public boolean isExpired() {
        val endDate = DateTimeUtil.parseDate(referralEndDate, DATE_FORMAT);
        return Objects.nonNull(endDate) ? endDate.isBefore(LocalDate.now()) : false;
    }
}
