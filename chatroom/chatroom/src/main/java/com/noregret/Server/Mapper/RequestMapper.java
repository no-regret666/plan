package com.noregret.Server.Mapper;

import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface RequestMapper {
    @Insert("insert into request(from_user,to_user) values (#{fromUser},#{toUser})")
    void insertRequest(String fromUser, String toUser);

    @Select("select from_user from request where to_user = #{toUser}")
    List<String> selectRequest(String toUser);

    @Delete("delete from request where from_user = #{fromUser} and to_user = #{toUser}")
    void deleteRequest(String fromUser, String toUser);
}
