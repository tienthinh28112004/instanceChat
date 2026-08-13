package com.bai72.quickchat.model;

import java.time.Instant;
import java.util.LinkedHashSet;
import java.util.Set;

public class UserAccount {
    private String username;
    private String passwordHash;
    private String googleEmail;
    private Set<String> friends = new LinkedHashSet<>();
    private String token;
    private Instant tokenCreatedAt;
    private Instant tokenExpiresAt;

    public UserAccount() {
    }

    public UserAccount(String username, String passwordHash, String googleEmail, Set<String> friends) {
        this.username = username;
        this.passwordHash = passwordHash;
        this.googleEmail = googleEmail;
        this.friends = friends;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }

    public String getGoogleEmail() {
        return googleEmail;
    }

    public void setGoogleEmail(String googleEmail) {
        this.googleEmail = googleEmail;
    }

    public Set<String> getFriends() {
        return friends;
    }

    public void setFriends(Set<String> friends) {
        this.friends = friends;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public Instant getTokenCreatedAt() {
        return tokenCreatedAt;
    }

    public void setTokenCreatedAt(Instant tokenCreatedAt) {
        this.tokenCreatedAt = tokenCreatedAt;
    }

    public Instant getTokenExpiresAt() {
        return tokenExpiresAt;
    }

    public void setTokenExpiresAt(Instant tokenExpiresAt) {
        this.tokenExpiresAt = tokenExpiresAt;
    }
}
