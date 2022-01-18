package ru.gosuslugi.pgu.fs.service.process;

import ru.gosuslugi.pgu.dto.QuizRequest;
import ru.gosuslugi.pgu.dto.QuizResponse;

public interface QuizProcess<T extends QuizRequest> {

    QuizProcess findAndRemoveOldOrderIfExists();

    QuizProcess createNewOrder();

    QuizProcess convertToNewDisplayId();

    QuizProcess convertToNewAnswers();

    QuizProcess setInitScreen();

    QuizProcess calculateNextStep();

    QuizProcess convertToNewFinishedScreens();

    QuizProcess convertToNewCachedAnswers();

    QuizProcess enrichWithCatalogInfo();

    QuizProcess of(T q);

    QuizResponse finish();


}
