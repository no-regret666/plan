package org.example.chatroom1.controller;

import jakarta.validation.constraints.Pattern;
import org.example.chatroom1.pojo.Result;
import org.example.chatroom1.pojo.User;
import org.example.chatroom1.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/user")
@Validated
public class UserController {
    @Autowired
    private UserService userService;

    @PostMapping("/register1")
    public Result register(@Pattern(regexp = "^\\S${1,10}") String username, @Pattern(regexp = "^\\S${5,16}") String password) {
        //查询用户
        User u = userService.findByUserName(username);
        if (u == null) {
            //没有占用
            //注册
            userService.register(username, password);
            return Result.success();
        } else {
            return Result.error("用户名已被占用");
        }
    }
}
