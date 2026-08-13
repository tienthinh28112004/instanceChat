package com.bai72.quickchat.controller;

import com.bai72.quickchat.dto.FriendRequest;
import com.bai72.quickchat.model.UserAccount;
import com.bai72.quickchat.service.FriendService;
import com.bai72.quickchat.web.AccessTokenInterceptor;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/friends")
public class FriendController {
    private final FriendService friendService;

    public FriendController(FriendService friendService) {
        this.friendService = friendService;
    }

    @GetMapping
    public List<String> list(@RequestAttribute(AccessTokenInterceptor.CURRENT_USER) UserAccount currentUser) {
        return friendService.listFriends(currentUser);
    }

    @PostMapping
    public void add(@RequestAttribute(AccessTokenInterceptor.CURRENT_USER) UserAccount currentUser,
                    @RequestBody FriendRequest request) {
        friendService.addFriend(currentUser, request);
    }
}
