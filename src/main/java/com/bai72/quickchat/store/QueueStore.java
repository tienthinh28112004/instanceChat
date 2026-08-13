package com.bai72.quickchat.store;

import com.bai72.quickchat.config.AppProperties;
import com.bai72.quickchat.model.QueuedMessage;
import com.fasterxml.jackson.core.type.TypeReference;
import java.nio.file.Path;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.stereotype.Component;

@Component
public class QueueStore {
    private final JsonStore jsonStore;
    private final Path filePath;
    private final Map<String, Deque<QueuedMessage>> queues = new ConcurrentHashMap<>();

    public QueueStore(JsonStore jsonStore, AppProperties properties) {
        this.jsonStore = jsonStore;
        this.filePath = properties.getDataDir().resolve("queues.json");
        load();
    }

    public synchronized void load() {
        Map<String, List<QueuedMessage>> loaded = jsonStore.read(filePath, new TypeReference<>() {
        }, new HashMap<>());
        queues.clear();
        for (Map.Entry<String, List<QueuedMessage>> entry : loaded.entrySet()) {
            queues.put(entry.getKey(), new ArrayDeque<>(entry.getValue()));
        }
    }

    public synchronized void persist() {
        Map<String, List<QueuedMessage>> snapshot = new HashMap<>();
        for (Map.Entry<String, Deque<QueuedMessage>> entry : queues.entrySet()) {
            snapshot.put(entry.getKey(), new ArrayList<>(entry.getValue()));
        }
        jsonStore.write(filePath, snapshot);
    }

    public synchronized void enqueue(String username, QueuedMessage message) {
        queues.computeIfAbsent(username, ignored -> new ArrayDeque<>()).addLast(message);
        persist();
    }

    public synchronized List<QueuedMessage> drain(String username) {
        Deque<QueuedMessage> queue = queues.get(username);
        if (queue == null || queue.isEmpty()) {
            return List.of();
        }
        List<QueuedMessage> batch = new ArrayList<>(queue);
        queue.clear();
        persist();
        return batch;
    }

    public synchronized boolean hasMessages(String username) {
        Deque<QueuedMessage> queue = queues.get(username);
        return queue != null && !queue.isEmpty();
    }
}
