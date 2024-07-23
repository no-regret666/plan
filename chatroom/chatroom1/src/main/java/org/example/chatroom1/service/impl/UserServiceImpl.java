package org.example.chatroom1.service.impl;

import org.example.chatroom1.mapper.UserMapper;
import org.example.chatroom1.pojo.User;
import org.example.chatroom1.service.UserService;
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
