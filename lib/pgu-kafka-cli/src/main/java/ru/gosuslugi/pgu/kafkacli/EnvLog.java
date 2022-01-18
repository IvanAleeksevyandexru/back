package ru.gosuslugi.pgu.kafkacli;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MapPropertySource;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
public class EnvLog {

    private final ConfigurableEnvironment environment;

    @EventListener
    public void onContextRefresh(ContextRefreshedEvent ignored) {

        val enabled = environment.getProperty("env-log.enabled", Boolean.class, true);
        if (!enabled) {
            return;
        }

        @SuppressWarnings("unchecked")
        val maskPatternList = (List<String>) environment.getProperty("env-log.mask-patterns", List.class, List.of("^.*password.*$"));
        val maskPatterns = maskPatternList.stream().map(Pattern::compile).collect(Collectors.toList());

        val processedProps = new HashSet<String>();
        val msg = new StringBuilder("========[ Application configuration ]========");
        for (val ps: environment.getPropertySources()) {
            if (!(ps instanceof MapPropertySource)) {
                continue;
            }
            val mps = (MapPropertySource) ps;
            msg.append("\n\n========> From ").append(mps.getName()).append("\n");
            for (val entry: mps.getSource().entrySet()) {

                if (!processedProps.add(entry.getKey())) {
                    continue;
                }

                val value = Objects.toString(entry.getValue());
                val valueToLog = maskPatterns.stream().anyMatch(p -> p.matcher(entry.getKey()).matches())
                    ? "*".repeat(value.length())
                    : value;

                msg.append("\n").append(entry.getKey()).append(" = ").append(valueToLog);

            }
        }

        log.info(msg.toString());

    }

}
