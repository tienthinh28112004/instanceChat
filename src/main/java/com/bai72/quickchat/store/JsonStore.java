package com.bai72.quickchat.store;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import org.springframework.stereotype.Component;

@Component
public class JsonStore {
    private final ObjectMapper objectMapper;

    public JsonStore(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public <T> T read(Path path, TypeReference<T> typeReference, T fallback) {
        if (!Files.exists(path)) {
            return fallback;
        }
        try {
            return objectMapper.readValue(path.toFile(), typeReference);
        } catch (IOException ex) {
            throw new IllegalStateException("Cannot read JSON file: " + path, ex);
        }
    }

    public void write(Path path, Object value) {
        try {
            Files.createDirectories(path.getParent());
            Path tmp = path.resolveSibling(path.getFileName() + ".tmp");
            objectMapper.writerWithDefaultPrettyPrinter().writeValue(tmp.toFile(), value);
            try {
                Files.move(tmp, path, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
            } catch (IOException moveFailure) {
                Files.move(tmp, path, StandardCopyOption.REPLACE_EXISTING);
            }
        } catch (IOException ex) {
            throw new IllegalStateException("Cannot write JSON file: " + path, ex);
        }
    }
}
