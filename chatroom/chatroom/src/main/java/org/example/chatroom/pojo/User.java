package org.example.chatroom.pojo;

import lombok.Data;

@Data
public class User {
    private int id;
    private String username;
    private String password;
    private int[] telephone = new int[11];
}
