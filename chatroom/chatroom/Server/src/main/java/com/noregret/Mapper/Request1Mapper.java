package com.noregret.Mapper;

import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface Request1Mapper {
    @Insert("insert into request1(from_user,to_user) values (#{fromUser},#{toUser})")
    void insertRequest(String fromUser, String toUser);

    @Select("select from_user from request1 where to_user = #{toUser}")
    List<String> selectRequest(String toUser);

    @Delete("delete from request1 where from_user = #{fromUser} and to_user = #{toUser}")
    void deleteRequest(String fromUser, String toUser);
}
