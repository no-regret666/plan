package com.noregret.Mapper;

import com.noregret.Pojo.Request;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface RequestMapper {
    @Insert("insert into request(`from`,`to`,`type`) values (#{from},#{to},#{type})")
    void insertRequest(String from, String to,int type);

    @Select("select `from` from request where `to` = #{to} and `type` = 1")
    List<String> selectRequest1(String to);

    @Delete("delete from request where `from` = #{from} and `to` = #{to} and `type` = #{type}")
    void deleteRequest(String from, String to,int type);

    @Select("select * from `request` where `to` in " +
            "(select group_name from `group` where member = #{username} and role in (1,2))")
    List<Request> selectRequest2(String username);

    @Delete("delete from request where `to` = #{groupName}")
    void breakGroup(String groupName);

    @Select("select count(*) from request where `from` = #{from} and `to` = #{to} and type = #{type}")
    int countRequest(String from, String to, int type);

    @Delete("delete from request where `from` = #{username} or `to` = #{username} or `to` in " +
            "(select group_name from `group` where member = #{username} and role = 1)")
    void deleteUser(String username);
}