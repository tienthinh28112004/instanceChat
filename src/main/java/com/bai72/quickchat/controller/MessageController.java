package com.bai72.quickchat.controller;

import com.bai72.quickchat.dto.MessageView;
import com.bai72.quickchat.dto.SendResult;
import com.bai72.quickchat.dto.TextMessageRequest;
import com.bai72.quickchat.model.UserAccount;
import com.bai72.quickchat.service.MessageService;
import com.bai72.quickchat.web.AccessTokenInterceptor;
import java.util.List;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/messages")
public class MessageController {
    private final MessageService messageService;

    public MessageController(MessageService messageService) {
        this.messageService = messageService;
    }

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    public SendResult sendText(@RequestAttribute(AccessTokenInterceptor.CURRENT_USER) UserAccount currentUser,
                               @RequestBody TextMessageRequest request) {
        return messageService.sendText(currentUser, request);
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public SendResult sendMultipart(@RequestAttribute(AccessTokenInterceptor.CURRENT_USER) UserAccount currentUser,
                                    @RequestParam("username") String username,
                                    @RequestParam(value = "message", required = false) String message,
                                    @RequestParam(value = "file", required = false) MultipartFile file) {
        return messageService.sendMultipart(currentUser, username, message, file);
    }

    @GetMapping
    public List<MessageView> poll(@RequestAttribute(AccessTokenInterceptor.CURRENT_USER) UserAccount currentUser) {
        return messageService.poll(currentUser);
    }
}
