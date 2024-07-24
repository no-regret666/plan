package org.example.chatroom.controller;

import jakarta.validation.constraints.Pattern;
import org.example.chatroom.pojo.Result;
import org.example.chatroom.pojo.User;
import org.example.chatroom.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/user")
@Validated
public class UserController {
    @Autowired
    private UserService userService;

    @PostMapping("/login.html")
    public Result login(String username, String password) {
        User u = userService.findByUserName(username);
        if(u == null){
            return Result.error("该用户不存在");
        }
        else{
            if(u.getPassword().equals(password)){
                return Result.success();
            }
            else{
                return Result.error("密码错误");
            }
        }
    }

    @PostMapping("register.html")
    public Result findUser(String username){
        Map<String,Object> map = new HashMap<>();
        User u = userService.findByUserName(username);
        if(u == null){
            map.put("userExist",false);
            map.put("msg","用户名可用");
            return Result.success(map);
        }
        else{
            map.put("userExist",true);
            map.put("msg","此用户名太受欢迎，请更换一个");
            return Result.error(map);
        }
    }
}
