package controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class UserController {
    @RequestMapping("/chatroom/register1.html")
    public String register1(String username, String password){
        System.out.println(username + ":" + password);
        return "success";
    }
}
