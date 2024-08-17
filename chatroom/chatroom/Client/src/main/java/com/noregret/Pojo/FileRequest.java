package com.noregret.Pojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class FileRequest {
    private int id;
    private int fileID;
    private String from;
    private String to;
    private int status; //1:未接收 2:已接收
    private String filename;
}