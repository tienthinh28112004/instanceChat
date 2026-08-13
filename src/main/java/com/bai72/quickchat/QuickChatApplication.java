package com.bai72.quickchat;

import com.bai72.quickchat.config.AppProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties(AppProperties.class)
public class QuickChatApplication {
    public static void main(String[] args) {
        SpringApplication.run(QuickChatApplication.class, args);
    }
}
