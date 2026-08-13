package com.bai72.quickchat.config;

import at.favre.lib.crypto.bcrypt.BCrypt;
import com.bai72.quickchat.model.UserAccount;
import com.bai72.quickchat.store.UserStore;
import java.nio.file.Files;
import java.util.LinkedHashSet;
import java.util.List;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class AppDataInitializer implements CommandLineRunner {
    private final AppProperties properties;
    private final UserStore userStore;

    public AppDataInitializer(AppProperties properties, UserStore userStore) {
        this.properties = properties;
        this.userStore = userStore;
    }

    @Override
    public void run(String... args) throws Exception {
        Files.createDirectories(properties.getDataDir());
        Files.createDirectories(properties.getStorageDir());

        if (!userStore.allUsers().isEmpty()) {
            return;
        }

        String hash = BCrypt.withDefaults().hashToString(12, "Pass@123".toCharArray());
        UserAccount alice = new UserAccount("alice", hash, "alice@gmail.com", new LinkedHashSet<>(List.of("bob", "carol")));
        UserAccount bob = new UserAccount("bob", hash, "bob@gmail.com", new LinkedHashSet<>(List.of("alice")));
        UserAccount carol = new UserAccount("carol", hash, "carol@gmail.com", new LinkedHashSet<>(List.of("alice", "dave")));
        UserAccount dave = new UserAccount("dave", hash, "dave@gmail.com", new LinkedHashSet<>(List.of("carol", "erin")));
        UserAccount erin = new UserAccount("erin", hash, "erin@gmail.com", new LinkedHashSet<>(List.of("dave")));

        userStore.seedIfEmpty(List.of(alice, bob, carol, dave, erin));
    }
}
