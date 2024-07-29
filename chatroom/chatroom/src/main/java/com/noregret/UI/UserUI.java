package com.noregret.UI;

import com.noregret.Mapper.FriendMapper;
import com.noregret.Mapper.UserMapper;
import com.noregret.pojo.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Scanner;

@Component
public class UserUI {
    @Autowired
    private FriendMapper friendMapper;
    @Autowired
    private UserMapper userMapper;

    public void menu(String username) {
        System.out.println("-------------------------------------");
        System.out.println("这里是" + username + "的个人主页!");
        System.out.println("-------------------------------------");
        System.out.println("a.好友列表");
        System.out.println("b.添加好友");
        System.out.println("c.注销");
        Scanner sc = new Scanner(System.in);
        char c = sc.next().charAt(0);
        switch (c) {
            case 'a':
                showFriend(username);
                break;
            case 'b':
                addFriend();
                break;

        }
    }

    public void showFriend(String username) {
        System.out.println(friendMapper.selectFriend(username));
    }

    public void addFriend() {
        Scanner sc = new Scanner(System.in);
        System.out.println("请输入你要添加好友的用户名:");
        String username = sc.nextLine();
        User friend = userMapper.getUser(username);
        if (friend == null) {
            System.out.println("该用户不存在!");
        }
        else{

        }
    }
}
