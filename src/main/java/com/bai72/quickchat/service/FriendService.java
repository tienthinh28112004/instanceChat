package com.bai72.quickchat.service;

import com.bai72.quickchat.dto.FriendRequest;
import com.bai72.quickchat.exception.ApiException;
import com.bai72.quickchat.model.UserAccount;
import com.bai72.quickchat.store.UserStore;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

@Service
public class FriendService {
    private final UserStore userStore;

    public FriendService(UserStore userStore) {
        this.userStore = userStore;
    }

    public List<String> listFriends(UserAccount currentUser) {
        return userStore.friendsOf(currentUser.getUsername());
    }

    public void addFriend(UserAccount currentUser, FriendRequest request) {
        if (request.username() == null || request.username().isBlank()) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "BAD_REQUEST", "Friend username is required");
        }
        if (currentUser.getUsername().equals(request.username())) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "BAD_REQUEST", "Cannot add yourself");
        }
        userStore.require(request.username());
        userStore.addFriendMutual(currentUser.getUsername(), request.username());
    }
}
