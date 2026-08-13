package com.bai72.quickchat.service;

import com.bai72.quickchat.config.AppProperties;
import com.bai72.quickchat.dto.MessageView;
import com.bai72.quickchat.dto.SendResult;
import com.bai72.quickchat.dto.TextMessageRequest;
import com.bai72.quickchat.exception.ApiException;
import com.bai72.quickchat.model.QueuedMessage;
import com.bai72.quickchat.model.UserAccount;
import com.bai72.quickchat.store.QueueStore;
import com.bai72.quickchat.store.UserStore;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
public class MessageService {
    private final UserStore userStore;
    private final QueueStore queueStore;
    private final FileService fileService;
    private final Duration timeout;
    private final Map<String, InboxWaiter> waiters = new ConcurrentHashMap<>();

    public MessageService(UserStore userStore, QueueStore queueStore, FileService fileService, AppProperties properties) {
        this.userStore = userStore;
        this.queueStore = queueStore;
        this.fileService = fileService;
        this.timeout = properties.getLongPollTimeout();
    }

    public SendResult sendText(UserAccount sender, TextMessageRequest request) {
        if (request == null || request.username() == null || request.username().isBlank()) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "BAD_REQUEST", "Receiver username is required");
        }
        if (request.message() == null || request.message().isBlank()) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "BAD_REQUEST", "Message text is required");
        }
        return send(sender, request.username(), request.message(), null);
    }

    public SendResult sendMultipart(UserAccount sender, String receiver, String text, MultipartFile file) {
        if ((text == null || text.isBlank()) && (file == null || file.isEmpty())) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "BAD_REQUEST", "Message content is required");
        }
        return send(sender, receiver, text, file);
    }

    public List<MessageView> poll(UserAccount currentUser) {
        List<QueuedMessage> available = queueStore.drain(currentUser.getUsername());
        if (!available.isEmpty()) {
            return toViews(available);
        }

        InboxWaiter waiter = waiters.compute(currentUser.getUsername(), (key, existing) -> {
            if (existing != null && !existing.closed && !existing.future.isDone()) {
                return existing;
            }
            return new InboxWaiter();
        });

        try {
            List<QueuedMessage> arrived = waiter.future.get(timeout.toMillis(), TimeUnit.MILLISECONDS);
            return toViews(arrived);
        } catch (TimeoutException ex) {
            waiter.closed = true;
            List<QueuedMessage> lateArrival = waiter.future.getNow(null);
            if (lateArrival != null) {
                return toViews(lateArrival);
            }
            return List.of();
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            throw new ApiException(HttpStatus.INTERNAL_SERVER_ERROR, "INTERNAL_ERROR", "Polling interrupted");
        } catch (Exception ex) {
            throw new ApiException(HttpStatus.INTERNAL_SERVER_ERROR, "INTERNAL_ERROR", "Cannot wait for messages");
        } finally {
            waiters.remove(currentUser.getUsername(), waiter);
        }
    }

    private SendResult send(UserAccount sender, String receiverName, String text, MultipartFile file) {
        UserAccount receiver = userStore.findByUsername(receiverName)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "NOT_FOUND", "Receiver not found"));
        if (!receiver.getFriends().contains(sender.getUsername())) {
            return new SendResult(3);
        }

        QueuedMessage queued;
        if (file != null && !file.isEmpty()) {
            var fileRecord = fileService.save(sender, receiverName, file);
            queued = QueuedMessage.file(
                    java.time.Instant.now(),
                    sender.getUsername(),
                    receiverName,
                    fileRecord.getStoredFileName(),
                    fileRecord.getOriginalFileName(),
                    fileRecord.getContentType(),
                    fileRecord.getSize()
            );
        } else {
            queued = QueuedMessage.text(java.time.Instant.now(), sender.getUsername(), receiverName, text);
        }

        InboxWaiter waiter = waiters.get(receiverName);
        if (waiter != null && !waiter.closed && !waiter.future.isDone()) {
            waiter.future.complete(List.of(queued));
            waiters.remove(receiverName, waiter);
            return new SendResult(1);
        }

        queueStore.enqueue(receiverName, queued);
        return new SendResult(2);
    }

    private List<MessageView> toViews(List<QueuedMessage> messages) {
        List<MessageView> views = new ArrayList<>(messages.size());
        for (QueuedMessage message : messages) {
            if (message.getStoredFileName() != null) {
                views.add(new MessageView(message.getTime(), message.getSender(), fileService.publicLink(message.getStoredFileName())));
            } else {
                views.add(new MessageView(message.getTime(), message.getSender(), message.getText()));
            }
        }
        return views;
    }

    private static class InboxWaiter {
        private final CompletableFuture<List<QueuedMessage>> future = new CompletableFuture<>();
        private volatile boolean closed;
    }
}
