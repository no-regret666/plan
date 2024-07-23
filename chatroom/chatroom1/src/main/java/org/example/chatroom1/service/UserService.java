package org.example.chatroom1.service;

import org.example.chatroom1.pojo.User;

public interface UserService {
    //根据用户名查询用户
    User findByUserName(String username);

    void register(String username, String password);
}
