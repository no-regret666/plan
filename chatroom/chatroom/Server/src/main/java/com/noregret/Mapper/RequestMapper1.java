package com.noregret.Mapper;

import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface RequestMapper1 {
    @Insert("insert into request1(`from`,`to`) values (#{fromUser},#{toUser})")
    void insertRequest(String fromUser, String toUser);

    @Select("select `from` from request1 where `to` = #{toUser}")
    List<String> selectRequest(String toUser);

    @Delete("delete from request1 where `from` = #{fromUser} and `to` = #{toUser}")
    void deleteRequest(String fromUser, String toUser);
}
