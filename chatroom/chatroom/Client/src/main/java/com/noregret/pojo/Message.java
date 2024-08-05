package com.noregret.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.sql.Timestamp;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Message {
    private int id;
    private String from;
    private String to;
    private String content;
    private Timestamp time;
    private MessageStatus status;

    public enum MessageStatus {
        unread,
        read
    }
}
