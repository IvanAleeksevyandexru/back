package ru.gosuslugi.pgu.fs.pgu.dto.feed;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class Params {
    /** Наименование организации - работодателя */
    @JsonProperty("orgName")
    private String orgName;

    /** ИНН организации */
    @JsonProperty("inn")
    private String orgINN;

    /** Номер телефона */
    @JsonProperty("corpPhoneData")
    private String phoneNumber;

    /** Опсос */
    @JsonProperty("operatorName")
    private String mobileNetworkOperator;

    /** Код маршрутизации - УИД ИС Опсоса */
    @JsonProperty("routeNumber")
    private String routeNumber;
}
