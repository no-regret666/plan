package com.noregret.Server.Mapper;

import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface Request2Mapper {
    @Select("select from_user from request2 where group_name = #{groupName}")
    List<String> selectRequest(String groupName);

    @Delete("delete from request2 where from_user = #{fromUser} and group_name = #{groupName}")
    void deleteRequest(String fromUser, String groupName);

    @Insert("insert into request2(group_name, from_user) values (#{groupName},#{fromUser})")
    void insertRequest(String fromUser, String groupName);
}
