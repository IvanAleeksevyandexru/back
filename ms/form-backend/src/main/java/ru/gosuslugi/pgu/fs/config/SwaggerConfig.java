package ru.gosuslugi.pgu.fs.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.responses.ApiResponse;
import org.springdoc.core.customizers.OpenApiCustomiser;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Map;

@Configuration
public class SwaggerConfig {

    private static final Map<String, ApiResponse> COMMON_RESPONSES = Map.of(
            "200", new ApiResponse().description("OK"),
            "400", new ApiResponse().description("Неверные параметры"),
            "401", new ApiResponse().description("Не авторизованный пользователь"),
            "403", new ApiResponse().description("Нет прав"),
            "500", new ApiResponse().description("Внутренняя ошибка")
    );

    @Bean
    public OpenAPI openAPI() {
        return new OpenAPI().info(new Info()
                .title("Form Service REST API Documentation")
                .description("Form Service REST API Documentation")
                .version("1.0")
        );
    }

    @Bean
    public OpenApiCustomiser commonResponseCustomizer() {
        return api -> api.getPaths().values().stream()
                .flatMap(item -> item.readOperations().stream())
                .map(Operation::getResponses)
                .forEach(resp -> COMMON_RESPONSES.forEach(resp::putIfAbsent));
    }
}
