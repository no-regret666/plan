package com.noregret.Mapper;

import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface FriendMapper {
    @Insert("insert into `friend`(`username`,`friend_name`,`status`) values (#{username},#{friendName},0)")
    void insertFriendship(String username, String friendName);

    @Select("select `friend_name` from friend where `username` = #{username}")
    List<String> selectFriend(String username);

    @Delete("delete from `friend` where `username` = #{username} and `friend_name` = #{friendName}")
    void deleteFriend(String username, String friendName);

    @Select("select `id` from `friend` where `username` = #{username} and `friend_name` = #{friendName}")
    Integer selectFriend2(String username, String friendName);

    @Update("update friend set status = #{status} where username = #{username} and friend_name = #{friendName}")
    void modifyStatus(String username, String friendName,int status);

    @Select("select status from friend where username = #{username} and friend_name = #{friendName}")
    Integer selectStatus(String username, String friendName);

    @Delete("delete from friend where username = #{username} or friend_name = #{username}")
    void deleteUser(String username);
}

