package ru.gosuslugi.pgu.fs.service.process;

/**
 * Процесс перехода на следующий экран
 */
public interface PrevScreenProcess extends ScreenProcess<PrevScreenProcess> {

    Boolean onlyInitScreenWasShow();

    void getInitScreen();

    boolean isCycledScreen();

    void setResponseIfPrevScreenCycled();

    boolean checkResponseIsNull();

    void putAnswersToCache();

    void removeAnswersFromDto();

    void setResponseIfScreenHasCycledComponent();

    void calculateNextScreen();

    void removeScreenFromFinished();

    void removeCycledAnswers();
}
