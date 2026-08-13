package com.bai72.quickchat.model;

import java.time.Instant;

public class QueuedMessage {
    private String time;
    private String sender;
    private String receiver;
    private String text;
    private String storedFileName;
    private String originalFileName;
    private String contentType;
    private long fileSize;

    public QueuedMessage() {
    }

    public QueuedMessage(String time, String sender, String receiver, String text, String storedFileName,
                         String originalFileName, String contentType, long fileSize) {
        this.time = time;
        this.sender = sender;
        this.receiver = receiver;
        this.text = text;
        this.storedFileName = storedFileName;
        this.originalFileName = originalFileName;
        this.contentType = contentType;
        this.fileSize = fileSize;
    }

    public static QueuedMessage text(Instant instant, String sender, String receiver, String text) {
        return new QueuedMessage(instant.toString(), sender, receiver, text, null, null, "text/plain", 0L);
    }

    public static QueuedMessage file(Instant instant, String sender, String receiver, String storedFileName,
                                     String originalFileName, String contentType, long fileSize) {
        return new QueuedMessage(instant.toString(), sender, receiver, null, storedFileName, originalFileName,
                contentType, fileSize);
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getSender() {
        return sender;
    }

    public void setSender(String sender) {
        this.sender = sender;
    }

    public String getReceiver() {
        return receiver;
    }

    public void setReceiver(String receiver) {
        this.receiver = receiver;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getStoredFileName() {
        return storedFileName;
    }

    public void setStoredFileName(String storedFileName) {
        this.storedFileName = storedFileName;
    }

    public String getOriginalFileName() {
        return originalFileName;
    }

    public void setOriginalFileName(String originalFileName) {
        this.originalFileName = originalFileName;
    }

    public String getContentType() {
        return contentType;
    }

    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

    public long getFileSize() {
        return fileSize;
    }

    public void setFileSize(long fileSize) {
        this.fileSize = fileSize;
    }
}
