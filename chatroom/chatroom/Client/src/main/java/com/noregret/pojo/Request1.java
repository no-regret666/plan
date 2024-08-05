package com.noregret.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Request1 {
    private int id;
    private String fromUser;
    private String toUser;
}
