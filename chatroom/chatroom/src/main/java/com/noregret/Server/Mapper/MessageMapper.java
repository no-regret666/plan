package com.noregret.Server.Mapper;

import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;

import java.sql.Timestamp;

@Mapper
public interface MessageMapper {
    @Insert("insert into message(from_user,to_user,content,time,status) " +
            "values (#{fromUser},#{toUser},#{content},#{time},#{status})")
    void insert(String fromUser, String toUser, String content, Timestamp time, String status);
}
