package com.noregret.Mapper;

import com.noregret.pojo.User;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface UserMapper {
    @Select("select * from user where username = #{username}")
    User getUserByUsername(String username);

    @Insert("insert into user(username,password) values (#{username},#{password})")
    void insertUser(String username, String password);

    @Delete("delete from user where username = #{username}")
    void deleteUser(String username);
}
