package com.noregret.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class theFriend {
    String name;
    Integer status; //是否在线，1:在线，0:不在线
}
