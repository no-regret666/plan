package org.example.chatroom.service.impl;

import org.example.chatroom.mapper.UserMapper;
import org.example.chatroom.pojo.User;
import org.example.chatroom.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class UserServiceImpl implements UserService {
    @Autowired
    private UserMapper userMapper;

    @Override
    public User findByUserName(String username) {
        User u = userMapper.findByUserName(username);
        return u;
    }

    @Override
    public void register(String username, String password) {
        userMapper.add(username,password);
    }
}
