package com.bai72.quickchat.store;

import com.bai72.quickchat.config.AppProperties;
import com.bai72.quickchat.exception.ApiException;
import com.bai72.quickchat.model.UserAccount;
import com.fasterxml.jackson.core.type.TypeReference;
import java.nio.file.Path;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

@Component
public class UserStore {
    private final JsonStore jsonStore;
    private final Path filePath;
    private final Map<String, UserAccount> users = new ConcurrentHashMap<>();

    public UserStore(JsonStore jsonStore, AppProperties properties) {
        this.jsonStore = jsonStore;
        this.filePath = properties.getDataDir().resolve("users.json");
        load();
    }

    public synchronized void load() {
        List<UserAccount> loaded = jsonStore.read(filePath, new TypeReference<>() {
        }, new ArrayList<>());
        users.clear();
        for (UserAccount user : loaded) {
            if (user.getFriends() == null) {
                user.setFriends(new LinkedHashSet<>());
            }
            users.put(user.getUsername(), user);
        }
    }

    public synchronized void persist() {
        List<UserAccount> snapshot = users.values().stream()
                .sorted(Comparator.comparing(UserAccount::getUsername))
                .toList();
        jsonStore.write(filePath, snapshot);
    }

    public synchronized void seedIfEmpty(List<UserAccount> seedUsers) {
        if (!users.isEmpty()) {
            return;
        }
        for (UserAccount user : seedUsers) {
            users.put(user.getUsername(), user);
        }
        persist();
    }

    public Optional<UserAccount> findByUsername(String username) {
        return Optional.ofNullable(users.get(username));
    }

    public Optional<UserAccount> findByGoogleEmail(String email) {
        return users.values().stream()
                .filter(user -> email != null && email.equalsIgnoreCase(user.getGoogleEmail()))
                .findFirst();
    }

    public Optional<UserAccount> findByToken(String token) {
        if (token == null || token.isBlank()) {
            return Optional.empty();
        }
        Instant now = Instant.now();
        return users.values().stream()
                .filter(user -> token.equals(user.getToken()))
                .filter(user -> user.getTokenExpiresAt() != null && user.getTokenExpiresAt().isAfter(now))
                .findFirst();
    }

    public synchronized UserAccount save(UserAccount user) {
        users.put(user.getUsername(), user);
        persist();
        return user;
    }

    public synchronized void addFriendMutual(String first, String second) {
        UserAccount left = require(first);
        UserAccount right = require(second);
        left.getFriends().add(second);
        right.getFriends().add(first);
        persist();
    }

    public synchronized UserAccount createUser(String username, String passwordHash, String googleEmail) {
        if (users.containsKey(username)) {
            throw new ApiException(HttpStatus.CONFLICT, "CONFLICT", "Username already exists");
        }
        UserAccount user = new UserAccount(username, passwordHash, googleEmail, new LinkedHashSet<>());
        users.put(username, user);
        persist();
        return user;
    }

    public List<String> friendsOf(String username) {
        return new ArrayList<>(require(username).getFriends());
    }

    public boolean isFriend(String owner, String candidate) {
        return require(owner).getFriends().contains(candidate);
    }

    public UserAccount require(String username) {
        return findByUsername(username)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "NOT_FOUND", "User not found"));
    }

    public List<UserAccount> allUsers() {
        return users.values().stream()
                .sorted(Comparator.comparing(UserAccount::getUsername))
                .toList();
    }
}
