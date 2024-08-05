package com.noregret.Mapper;

import com.noregret.Pojo.Message;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.sql.Timestamp;
import java.util.List;

@Mapper
public interface MessageMapper {
    @Insert("insert into message(`from`,`to`,content,time,status) " +
            "values (#{from},#{to},#{content},#{time},#{status})")
    void insert(String from, String to, String content, Timestamp time, String status);

    @Select("select * from message where `from` = #{name} or `to` = #{name}")
    List<Message> findByFromUser(String name);
}