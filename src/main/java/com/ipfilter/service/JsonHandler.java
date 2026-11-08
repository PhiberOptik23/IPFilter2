package com.ipfilter.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ipfilter.model.InputData;
import com.ipfilter.model.OutputData;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

public class JsonHandler {
    private final ObjectMapper objectMapper;

    public JsonHandler() {
        this.objectMapper = new ObjectMapper();
        this.objectMapper.findAndRegisterModules();
    }

    // Файл: src/main/java/com/ipfilter/service/JsonHandler.java

    public InputData loadInput(String filePath) throws IOException {
        // Сначала пробуем загрузить из classpath (для тестов)
        InputStream inputStream = getClass().getClassLoader().getResourceAsStream(filePath);

        if (inputStream != null) {
            return objectMapper.readValue(inputStream, InputData.class);
        }

        // Если не нашли в classpath, пробуем как обычный файл (для production)
        File file = new File(filePath);
        if (file.exists()) {
            return objectMapper.readValue(file, InputData.class);
        }

        throw new IOException("File not found: " + filePath);
    }

    public void saveOutput(String filePath, OutputData data) throws IOException {
        objectMapper.writerWithDefaultPrettyPrinter()
                .writeValue(new File(filePath), data);
    }
}