package com.noregret.Service;

import com.noregret.Mapper.UserMapper;
import com.noregret.pojo.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

@Service
public class UserService {
    @Autowired
    private UserMapper userMapper;

    public User getUserByUsername(String username) {
        return userMapper.getUserByUsername(username);
    }

    public void insertUser(String username, String password) {
        userMapper.insertUser(username, password);
    }

    public void deleteUser(String username) {userMapper.deleteUser(username);}
}