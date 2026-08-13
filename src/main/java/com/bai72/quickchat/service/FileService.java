package com.bai72.quickchat.service;

import com.bai72.quickchat.config.AppProperties;
import com.bai72.quickchat.exception.ApiException;
import com.bai72.quickchat.model.FileRecord;
import com.bai72.quickchat.model.UserAccount;
import com.bai72.quickchat.store.FileStore;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.Locale;
import java.util.UUID;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
public class FileService {
    private final AppProperties properties;
    private final FileStore fileStore;

    public FileService(AppProperties properties, FileStore fileStore) {
        this.properties = properties;
        this.fileStore = fileStore;
    }

    public FileRecord save(UserAccount sender, String receiver, MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "BAD_REQUEST", "File is required");
        }
        try {
            Files.createDirectories(properties.getStorageDir());
            String originalName = sanitize(file.getOriginalFilename());
            String storedName = UUID.randomUUID().toString().replace("-", "") + "_" + originalName;
            Path target = properties.getStorageDir().resolve(storedName);
            file.transferTo(target);
            FileRecord record = new FileRecord(
                    storedName,
                    originalName,
                    sender.getUsername(),
                    receiver,
                    file.getContentType() != null ? file.getContentType() : MediaType.APPLICATION_OCTET_STREAM_VALUE,
                    file.getSize(),
                    Instant.now()
            );
            fileStore.save(record);
            return record;
        } catch (IOException ex) {
            throw new ApiException(HttpStatus.INTERNAL_SERVER_ERROR, "INTERNAL_ERROR", "Cannot store uploaded file");
        }
    }

    public ResponseEntity<Resource> download(String requester, String storedFileName) {
        FileRecord record = fileStore.find(storedFileName)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "NOT_FOUND", "File not found"));
        if (!requester.equals(record.getSender()) && !requester.equals(record.getReceiver())) {
            throw new ApiException(HttpStatus.FORBIDDEN, "FORBIDDEN", "You do not have permission to access this file");
        }
        try {
            Path path = properties.getStorageDir().resolve(storedFileName);
            if (!Files.exists(path)) {
                throw new ApiException(HttpStatus.NOT_FOUND, "NOT_FOUND", "File not found");
            }
            Resource resource = new UrlResource(path.toUri());
            HttpHeaders headers = new HttpHeaders();
            headers.setContentDisposition(ContentDisposition.attachment().filename(record.getOriginalFileName()).build());
            return ResponseEntity.ok()
                    .headers(headers)
                    .contentType(MediaType.parseMediaType(record.getContentType()))
                    .body(resource);
        } catch (IOException ex) {
            throw new ApiException(HttpStatus.INTERNAL_SERVER_ERROR, "INTERNAL_ERROR", "Cannot read file");
        }
    }

    public String publicLink(String storedFileName) {
        return "/files/" + storedFileName;
    }

    private String sanitize(String originalName) {
        String name = originalName == null || originalName.isBlank() ? "file.bin" : originalName;
        return name.toLowerCase(Locale.ROOT).replaceAll("[^a-z0-9._-]", "_");
    }
}
