package org.example.chatroom.service;

import org.example.chatroom.pojo.User;

public interface UserService {
    //根据用户名查询用户
    User findByUserName(String username);

    void register(String username, String password);
}
