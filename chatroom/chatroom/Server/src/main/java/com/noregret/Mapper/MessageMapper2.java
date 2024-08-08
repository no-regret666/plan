package com.noregret.Mapper;

import com.noregret.Pojo.Message;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.sql.Timestamp;
import java.util.List;

@Mapper
public interface MessageMapper2 {
    @Insert("insert into message2(`from`,`to`,content,time,status) " +
            "values (#{from},#{to},#{content},#{time},#{status})")
    void insert(String from, String to, String content, Timestamp time, String status);

    @Select("select * from message2 where `to` =  #{to}")
    List<Message> groupChat(String to);

    @Delete("delete from message2 where `to` = #{to}")
    void breakGroup(String to);
}
