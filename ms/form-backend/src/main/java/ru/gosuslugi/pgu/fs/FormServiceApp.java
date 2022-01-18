package ru.gosuslugi.pgu.fs;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;

@SpringBootApplication
public class FormServiceApp {

    public static void main(String[] args) {
        new SpringApplicationBuilder(FormServiceApp.class)
                .run(args);
    }
}
