package com.noregret.Server.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.sql.Timestamp;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Message {
    private int id;
    private String fromUser;
    private String toUser;
    private String content;
    private Timestamp time;
    private MessageStatus status;

    public enum MessageStatus {
        UNREAD,
        READ
    }
}
