package ru.gosuslugi.pgu.fs.common.exception.config;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import ru.gosuslugi.pgu.fs.common.exception.handler.FormCommonExceptionHandler;

@Configuration
@ComponentScan(basePackageClasses = FormCommonExceptionHandler.class)
public class FormCommonExceptionHandlerAutoConfiguration {
}
