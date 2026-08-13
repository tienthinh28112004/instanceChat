package com.bai72.quickchat.store;

import com.bai72.quickchat.config.AppProperties;
import com.bai72.quickchat.model.FileRecord;
import com.fasterxml.jackson.core.type.TypeReference;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.stereotype.Component;

@Component
public class FileStore {
    private final JsonStore jsonStore;
    private final Path filePath;
    private final Map<String, FileRecord> files = new ConcurrentHashMap<>();

    public FileStore(JsonStore jsonStore, AppProperties properties) {
        this.jsonStore = jsonStore;
        this.filePath = properties.getDataDir().resolve("files.json");
        load();
    }

    public synchronized void load() {
        List<FileRecord> loaded = jsonStore.read(filePath, new TypeReference<>() {
        }, new ArrayList<>());
        files.clear();
        for (FileRecord record : loaded) {
            files.put(record.getStoredFileName(), record);
        }
    }

    public synchronized void persist() {
        List<FileRecord> snapshot = files.values().stream()
                .sorted(Comparator.comparing(FileRecord::getUploadedAt, Comparator.nullsLast(Comparator.naturalOrder())))
                .toList();
        jsonStore.write(filePath, snapshot);
    }

    public synchronized FileRecord save(FileRecord record) {
        files.put(record.getStoredFileName(), record);
        persist();
        return record;
    }

    public Optional<FileRecord> find(String storedFileName) {
        return Optional.ofNullable(files.get(storedFileName));
    }
}
