package org.example.chatroom.mapper;

import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.example.chatroom.pojo.User;

@Mapper
public interface UserMapper {
    //根据用户名查询用户
    @Select("select * from user where username=#{username}")
    User findByUserName(String username);

    //添加用户
    @Insert("insert into user(username, password)" + "values(#{username},#{password})")
    void add(String username, String password);
}
