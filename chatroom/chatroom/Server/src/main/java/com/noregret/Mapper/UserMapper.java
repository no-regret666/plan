package com.noregret.Mapper;

import com.noregret.Pojo.User;
import org.apache.ibatis.annotations.*;

@Mapper
public interface UserMapper {
    @Select("select * from user where username = #{username}")
    User getUser(String username);

    @Insert("insert into user(username,password) values (#{username},#{password})")
    void insertUser(String username, String password);

    @Delete("delete from user where username = #{username}")
    void deleteUser(String username);

    @Update("update user set password = #{password} where username = #{username}")
    void updatePassword(String username, String password);
}
