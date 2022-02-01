package ru.gosuslugi.pgu.fs.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import lombok.RequiredArgsConstructor;
import org.apache.logging.log4j.util.Strings;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.gosuslugi.pgu.dto.QuizRequest;
import ru.gosuslugi.pgu.dto.QuizResponse;
import ru.gosuslugi.pgu.fs.service.QuizService;

import java.util.Objects;

@RestController
@RequestMapping(value = "/quiz/scenario", produces = MediaType.APPLICATION_JSON_VALUE)
@RequiredArgsConstructor
public class QuizController {

    private final QuizService quizService;

    @Operation(summary = "Преобразование результатов квиза в заявление")
    @PostMapping("/toOrder")
    public ResponseEntity<QuizResponse> transformToOrder(@Parameter(description = "Результаты квиза", required=true) @RequestBody QuizRequest quizRequest){
        if (Strings.isEmpty(quizRequest.getServiceId()) ||
                Strings.isEmpty(quizRequest.getTargetId()) ||
                Objects.isNull(quizRequest.getScenarioDto())
        ) {
            return ResponseEntity.badRequest().build();
        }
        return ResponseEntity.ok(quizService.transformToOrder(quizRequest));
    }


}
