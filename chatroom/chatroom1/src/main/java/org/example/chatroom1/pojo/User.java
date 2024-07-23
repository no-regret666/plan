package org.example.chatroom1.pojo;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.util.Arrays;

@Data
public class User {
    private int id;
    private String username;
    private String password;
    private int[] telephone = new int[11];
}
