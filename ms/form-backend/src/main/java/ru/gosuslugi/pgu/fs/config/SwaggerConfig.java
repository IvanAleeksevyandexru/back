package ru.gosuslugi.pgu.fs.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.service.Contact;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

import java.util.Collections;

@Configuration
@EnableSwagger2
public class SwaggerConfig {

    @Bean
    public Docket api() {
        return new Docket(DocumentationType.SWAGGER_2)
                .apiInfo(apiInfo())
                .select()
                .apis(RequestHandlerSelectors.basePackage("ru.gosuslugi.pgu.fs.controller"))
                .paths(PathSelectors.regex("(/service/.*/scenario/(getService|getNextStep|getPrevStep)|/quiz/scenario/toOrder)"))
                .build();
    }

    private ApiInfo apiInfo() {
        return new ApiInfo("Form Service REST API Documentation",
                "Form Service REST API Documentation",
                "1.0",
                "",
                new Contact("", "", ""),
                "", "",
                Collections.emptyList());
    }
}
