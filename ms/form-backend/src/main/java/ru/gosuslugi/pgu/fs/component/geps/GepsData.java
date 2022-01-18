package ru.gosuslugi.pgu.fs.component.geps;

import lombok.Data;

/**
 * ДТО компонент {@link GepsDataComponent}
 */
@Data
class GepsData {
    /** Наименование организации - работодателя */
    private String orgName;

    /** ИНН организации */
    private String orgINN;

    /** Номер телефона */
    private String phoneNumber;

    /** Опсос */
    private String mobileNetworkOperator;

    /** Код маршрутизации - УИД ИС Опсоса */
    private String routeNumber;

    /** Id оператора */
    private String operatorId;
}
