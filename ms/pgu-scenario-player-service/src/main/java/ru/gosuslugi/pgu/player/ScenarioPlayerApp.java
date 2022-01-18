package ru.gosuslugi.pgu.player;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.ComponentScan;

@EnableCaching
@ComponentScan({
    "ru.gosuslugi.pgu.fs.common",
    "ru.gosuslugi.pgu.player",
    "ru.gosuslugi.pgu.common.esia.search.dto",
})
@SpringBootApplication
public class ScenarioPlayerApp {

    public static void main(String[] args) {
        new SpringApplicationBuilder(ScenarioPlayerApp.class)
                .run(args);
    }
}
