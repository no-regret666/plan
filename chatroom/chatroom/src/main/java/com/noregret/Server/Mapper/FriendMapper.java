package com.noregret.Server.Mapper;

import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface FriendMapper {
    @Insert("insert into friendship(username,friend_name) values (#{username},#{friendName})")
    void insertFriendship(String username, String friendName);

    @Select("select user.username from user,friendship where user.username = friendship.username")
    List<String> selectFriend(String username);
}

