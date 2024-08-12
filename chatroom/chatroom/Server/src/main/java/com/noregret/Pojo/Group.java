package com.noregret.Pojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Group {
    private int id;
    private String groupName;
    private String member;
    private int role;
    private int status; //禁言状态 0:未被禁言 1:禁言中
}
