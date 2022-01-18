package ru.gosuslugi.pgu.fs.action;

public enum ActionType {

    /**
     * Экшен для добавления в календарь события
     */
    addToCalendar,
    /**
     * Экшен для переотправки СМС кода при смене номера телефона
     */
    resendSmsCode,

    /**
     * Экшен для скачивания квитанции на оплату
     */
    downloadBillPdf,

    /**
     * Экшен для скачивания сведений транспортного средства
     */
    downloadTsReportPdf,

    /**
     * Экшен для формирования ссылки на определенный экран
     */
    createUrl
}
