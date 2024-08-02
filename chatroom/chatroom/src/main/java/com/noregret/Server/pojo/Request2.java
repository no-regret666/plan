package com.noregret.Server.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Request2 {
    private int id;
    private String groupName;
    private String fromUser;
}
