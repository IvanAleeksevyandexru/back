package ru.gosuslugi.pgu.fs.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.gosuslugi.pgu.dto.QuizRequest;
import ru.gosuslugi.pgu.dto.QuizResponse;
import ru.gosuslugi.pgu.fs.service.QuizService;
import ru.gosuslugi.pgu.fs.service.process.QuizProcess;

@Service
@RequiredArgsConstructor
@Slf4j
public class QuizServiceImpl implements QuizService {

    private final QuizProcess quizProcess;


    @Override
    public QuizResponse transformToOrder(QuizRequest quizRequest) {
        return quizProcess.of(quizRequest)
                .findAndRemoveOldOrderIfExists()
                .createNewOrder()
                .convertToNewDisplayId()
                .convertToNewAnswers()
                .convertToNewCachedAnswers()
                .convertToNewFinishedScreens()
                .setInitScreen()
                .calculateNextStep()
                .enrichWithCatalogInfo()
                .finish();
    }

}
