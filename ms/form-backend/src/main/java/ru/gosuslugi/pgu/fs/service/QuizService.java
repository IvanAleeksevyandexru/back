package ru.gosuslugi.pgu.fs.service;

import ru.gosuslugi.pgu.dto.QuizRequest;
import ru.gosuslugi.pgu.dto.QuizResponse;

public interface QuizService {

    QuizResponse transformToOrder(QuizRequest quizRequest);

}
