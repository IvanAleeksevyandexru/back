package ru.gosuslugi.pgu.fs.service.process;

/**
 * Процесс перехода на следующий экран
 */
public interface NextScreenProcess extends ScreenProcess<NextScreenProcess> {

    void buildResponse();

    void tryToCreateOrderId();

    void removeOldAnswers();

    void clearValidateErrors();

    void validate();

    boolean hasValidateErrors();

    void preloadComponents();

    boolean orderShouldExists();

    /**
     * Меняет результат процесса если следующий скрин циклический
     * @return Признак замены результата
     */
    boolean replaceResponseForCycledScreen();

    void calculateNextScreen();

    void fillUserData();

    boolean needReInitScreen();

    void reInitScenario();

    boolean isTerminalAndImpasse();

    boolean isTerminalAndNotImpasse();

    boolean isTerminalAndForceSendToSuggest();

    void deleteOrder();

    boolean hasOrderCreateCustomParameter();

    boolean draftShouldExist();

    void saveChosenValuesForOrder();

    void updateAdditionalAttributes();

    void mergePdfDocuments();

    void forceSendToSuggestion();

    /**
     * Выполнение интеграционных действий (делириум, СП)
     */
    void performIntegrationSteps();

    void clearCacheForComponents();

    void prepareDisplayAfterValidationErrors();

    boolean isNeedToUpdateAdditionalParameters();

    void checkPermissions();

    boolean hasCheckForDuplicate();

    void checkForDuplicate();

    void removeDisclaimers();
}
