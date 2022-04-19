package ru.gosuslugi.pgu.fs.common.variable;

public enum VariableType {
    targetId,
    serviceId,
    orderId,
    today,
    todayTimeStamp,
    /** Значение ОКАТО региона пользователя */
    userRegionCode,
    /** значение адреса пользователя из госбара */
    userAddress,
    /** formId из serviceInfo*/
    formId;

    public static VariableType getInstance(String typeName) {
        for (var type : VariableType.values())
            if (type.name().equals(typeName)) return type;
        return null;
    }
}
