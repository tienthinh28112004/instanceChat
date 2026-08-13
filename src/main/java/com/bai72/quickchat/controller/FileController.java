package com.bai72.quickchat.controller;

import com.bai72.quickchat.model.UserAccount;
import com.bai72.quickchat.service.FileService;
import com.bai72.quickchat.web.AccessTokenInterceptor;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/files")
public class FileController {
    private final FileService fileService;

    public FileController(FileService fileService) {
        this.fileService = fileService;
    }

    @GetMapping("/{name}")
    public ResponseEntity<Resource> download(@RequestAttribute(AccessTokenInterceptor.CURRENT_USER) UserAccount currentUser,
                                             @PathVariable("name") String fileName) {
        return fileService.download(currentUser.getUsername(), fileName);
    }
}
