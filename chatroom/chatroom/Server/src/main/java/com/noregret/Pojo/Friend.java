package com.noregret.Pojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Friend {
    private int id;
    private String username;
    private String friendName;
    private int status; //屏蔽状态 0:未屏蔽 1:屏蔽
}
