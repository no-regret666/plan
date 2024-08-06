package com.noregret.Mapper;

import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface RequestMapper2 {
    @Select("select `from` from request2 where `to` = #{groupName}")
    List<String> selectRequest(String groupName);

    @Delete("delete from request2 where `from` = #{fromUser} and `to` = #{groupName}")
    void deleteRequest(String fromUser, String groupName);

    @Insert("insert into request2(`to`, `from`) values (#{groupName},#{fromUser})")
    void insertRequest(String fromUser, String groupName);
}
