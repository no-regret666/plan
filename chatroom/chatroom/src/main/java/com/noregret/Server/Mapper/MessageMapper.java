package com.noregret.Server.Mapper;

import com.noregret.Server.pojo.Message;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.sql.Timestamp;
import java.util.List;

@Mapper
public interface MessageMapper {
    @Insert("insert into message(from_user,to_user,content,time,status) " +
            "values (#{fromUser},#{toUser},#{content},#{time},#{status})")
    void insert(String fromUser, String toUser, String content, Timestamp time, String status);

    @Select("select * from message where from_user = #{fromUser} or to_user = #{fromUser}")
    List<Message> findByFromUser(String fromUser);
}
