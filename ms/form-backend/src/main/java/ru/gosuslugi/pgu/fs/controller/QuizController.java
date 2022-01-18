package ru.gosuslugi.pgu.fs.controller;

import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import lombok.RequiredArgsConstructor;
import org.apache.logging.log4j.util.Strings;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.gosuslugi.pgu.dto.QuizRequest;
import ru.gosuslugi.pgu.dto.QuizResponse;
import ru.gosuslugi.pgu.fs.service.QuizService;

import java.util.Objects;

@RestController
@RequestMapping(value = "/quiz/scenario", produces = MediaType.APPLICATION_JSON_VALUE)
@RequiredArgsConstructor
public class QuizController {

    private final QuizService quizService;

    @ApiOperation(value = "Преобразование результатов квиза в заявление")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "OK"),
            @ApiResponse(code = 400, message = "Неверные параметры"),
            @ApiResponse(code = 401, message = "Не авторизованный пользователь"),
            @ApiResponse(code = 403, message = "Нет прав"),
            @ApiResponse(code = 500, message = "Внутренняя ошибка") })
    @PostMapping("/toOrder")
    public ResponseEntity<QuizResponse> transformToOrder(@ApiParam(value = "Результаты квиза", required=true) @RequestBody QuizRequest quizRequest){
        if (Strings.isEmpty(quizRequest.getServiceId()) ||
                Strings.isEmpty(quizRequest.getTargetId()) ||
                Objects.isNull(quizRequest.getScenarioDto())
        ) {
            return ResponseEntity.badRequest().build();
        }
        return ResponseEntity.ok(quizService.transformToOrder(quizRequest));
    }


}
