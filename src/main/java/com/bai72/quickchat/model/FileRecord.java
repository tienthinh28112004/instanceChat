package com.bai72.quickchat.model;

import java.time.Instant;

public class FileRecord {
    private String storedFileName;
    private String originalFileName;
    private String sender;
    private String receiver;
    private String contentType;
    private long size;
    private Instant uploadedAt;

    public FileRecord() {
    }

    public FileRecord(String storedFileName, String originalFileName, String sender, String receiver,
                      String contentType, long size, Instant uploadedAt) {
        this.storedFileName = storedFileName;
        this.originalFileName = originalFileName;
        this.sender = sender;
        this.receiver = receiver;
        this.contentType = contentType;
        this.size = size;
        this.uploadedAt = uploadedAt;
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

    public String getContentType() {
        return contentType;
    }

    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

    public long getSize() {
        return size;
    }

    public void setSize(long size) {
        this.size = size;
    }

    public Instant getUploadedAt() {
        return uploadedAt;
    }

    public void setUploadedAt(Instant uploadedAt) {
        this.uploadedAt = uploadedAt;
    }
}
