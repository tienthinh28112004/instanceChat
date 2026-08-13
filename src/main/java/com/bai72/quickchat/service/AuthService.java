package com.bai72.quickchat.service;

import com.bai72.quickchat.config.AppProperties;
import com.bai72.quickchat.dto.AccessTokenResponse;
import com.bai72.quickchat.dto.GoogleLoginRequest;
import com.bai72.quickchat.dto.LoginRequest;
import com.bai72.quickchat.exception.ApiException;
import com.bai72.quickchat.model.UserAccount;
import com.bai72.quickchat.store.UserStore;
import at.favre.lib.crypto.bcrypt.BCrypt;
import java.time.Instant;
import java.util.Locale;
import java.util.UUID;
import java.util.regex.Pattern;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

@Service
public class AuthService {
    private static final Pattern USERNAME_SAFE = Pattern.compile("[^a-zA-Z0-9._-]");

    private final UserStore userStore;
    private final AppProperties properties;

    public AuthService(UserStore userStore, AppProperties properties) {
        this.userStore = userStore;
        this.properties = properties;
    }

    public AccessTokenResponse login(LoginRequest request) {
        UserAccount user = userStore.findByUsername(request.username())
                .orElseThrow(() -> new ApiException(HttpStatus.UNAUTHORIZED, "UNAUTHORIZED", "Invalid username or password"));
        boolean verified = BCrypt.verifyer().verify(request.password().toCharArray(), user.getPasswordHash()).verified;
        if (!verified) {
            throw new ApiException(HttpStatus.UNAUTHORIZED, "UNAUTHORIZED", "Invalid username or password");
        }
        issueToken(user);
        return new AccessTokenResponse(user.getToken());
    }

    public AccessTokenResponse googleLogin(GoogleLoginRequest request) {
        if (request.email() == null || request.email().isBlank()) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "BAD_REQUEST", "Email is required");
        }
        UserAccount user = userStore.findByGoogleEmail(request.email())
                .orElseGet(() -> userStore.createUser(uniqueUsernameFromEmail(request.email(), request.name()),
                        BCrypt.withDefaults().hashToString(12, ("google:" + request.email()).toCharArray()),
                        request.email()));
        if (user.getFriends().isEmpty() && request.name() != null && !request.name().isBlank()) {
            // Preserve first login metadata for auto-created accounts.
            userStore.save(user);
        }
        issueToken(user);
        return new AccessTokenResponse(user.getToken());
    }

    public UserAccount requireCurrentUser(String token) {
        return userStore.findByToken(token)
                .orElseThrow(() -> new ApiException(HttpStatus.UNAUTHORIZED, "UNAUTHORIZED", "Access token is missing or expired"));
    }

    public void issueToken(UserAccount user) {
        Instant now = Instant.now();
        user.setToken(UUID.randomUUID().toString().replace("-", ""));
        user.setTokenCreatedAt(now);
        user.setTokenExpiresAt(now.plus(properties.getTokenTtl()));
        userStore.save(user);
    }

    private String uniqueUsernameFromEmail(String email, String fallbackName) {
        String base = email.substring(0, email.indexOf('@'));
        if (fallbackName != null && !fallbackName.isBlank()) {
            base = fallbackName;
        }
        base = USERNAME_SAFE.matcher(base.toLowerCase(Locale.ROOT)).replaceAll("_");
        if (base.isBlank()) {
            base = "user";
        }
        String candidate = base;
        int index = 1;
        while (userStore.findByUsername(candidate).isPresent()) {
            candidate = base + index++;
        }
        return candidate;
    }
}
