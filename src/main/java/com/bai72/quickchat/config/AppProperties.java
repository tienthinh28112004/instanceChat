package com.bai72.quickchat.config;

import java.nio.file.Path;
import java.time.Duration;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app")
public class AppProperties {
    private Path dataDir;
    private Path storageDir;
    private Duration tokenTtl = Duration.ofHours(24);
    private Duration longPollTimeout = Duration.ofSeconds(10);

    public Path getDataDir() {
        return dataDir;
    }

    public void setDataDir(Path dataDir) {
        this.dataDir = dataDir;
    }

    public Path getStorageDir() {
        return storageDir;
    }

    public void setStorageDir(Path storageDir) {
        this.storageDir = storageDir;
    }

    public Duration getTokenTtl() {
        return tokenTtl;
    }

    public void setTokenTtl(Duration tokenTtl) {
        this.tokenTtl = tokenTtl;
    }

    public Duration getLongPollTimeout() {
        return longPollTimeout;
    }

    public void setLongPollTimeout(Duration longPollTimeout) {
        this.longPollTimeout = longPollTimeout;
    }
}
