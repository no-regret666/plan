package com.noregret.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Message {
    Integer id;
    Integer senderId;
    Integer receiverId;
    String message;
    Date time;
}
