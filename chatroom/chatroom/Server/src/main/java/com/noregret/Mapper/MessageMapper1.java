package com.noregret.Mapper;

import com.noregret.Pojo.Message;
import org.apache.ibatis.annotations.*;

import java.sql.Timestamp;
import java.util.List;

@Mapper
public interface MessageMapper1 {
    @Insert("insert into message1(`from`,`to`,content,time,status) " +
            "values (#{from},#{to},#{content},#{time},#{status})")
    void insert(String from, String to, String content, Timestamp time, String status);

    @Select("select * from message1 where (`from` = #{name1} and `to` = #{name2})" +
            " or (`from` = #{name2} and `to` = #{name1})")
    List<Message> privateChat(String name1,String name2);

    @Update("update message1 set status = 'read' where status = 'unread' and `to` = #{username} and `from` = #{friendName}")
    void update(String username,String friendName);

    @Select("select `from` from message1 where `to` = #{to} and status = 'unread'")
    List<String> getFriends(String to);

    @Select("select count(*) from message1 where `from` = #{from} and `to` = #{to}")
    Integer count(String from, String to);

    @Delete("delete from message1 where `from` = #{from} and `to` = #{to}")
    void delete(String from, String to);

    @Delete("delete from message1 where `from` = #{username} or `to` = #{username}")
    void deleteUser(String username);
}