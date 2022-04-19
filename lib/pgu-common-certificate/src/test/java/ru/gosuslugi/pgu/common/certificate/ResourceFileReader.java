package ru.gosuslugi.pgu.common.certificate;

import lombok.SneakyThrows;
import org.springframework.core.io.ClassPathResource;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

public class ResourceFileReader {
    @SneakyThrows
    public static String readFromFile(String fileName){
        File resource = new ClassPathResource(fileName).getFile();
        return new String(Files.readAllBytes(resource.toPath()), StandardCharsets.UTF_8);
    }

}
