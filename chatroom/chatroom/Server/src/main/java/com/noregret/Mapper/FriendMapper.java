package com.noregret.Mapper;

import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface FriendMapper {
    @Insert("insert into friend(username,friend_name) values (#{username},#{friendName})")
    void insertFriendship(String username, String friendName);

    @Select("select friend_name from friend where username = #{username}")
    List<String> selectFriend(String username);

    @Delete("delete from friend where username = #{username} and friend_name = #{friendName}")
    void deleteFriend(String username, String friendName);

    @Select("select id from friend where username = #{username} and friend_name = #{friendName}")
    Integer selectFriend2(String username, String friendName);
}

