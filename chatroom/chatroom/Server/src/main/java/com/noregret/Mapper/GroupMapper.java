package com.noregret.Mapper;

import com.noregret.Pojo.Group;
import com.noregret.Pojo.Member;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface GroupMapper {
    @Insert("insert into `group`(group_name,member,role,status) values (#{groupName},#{member},#{role},0)")
    void insert(String groupName, String member, int role);

    @Select("select * from `group` where group_name = #{groupName} and role = 1")
    Group getGroup(String groupName);

    @Select("select group_name from `group` where member = #{member}")
    List<String> getGroups(String member);

    @Select("select member,role,status from `group` where group_name = #{groupName}")
    List<Member> getMembers(String groupName);

    @Select("select member from `group` where group_name = #{groupName}")
    List<String> getMemberNames(String groupName);

    @Delete("delete from `group` where group_name = #{groupName} and member = #{member}")
    void deleteMember(String groupName, String member);

    @Delete("delete from `group` where group_name = #{groupName}")
    void deleteGroup(String groupName);

    @Update("update `group` set role = #{role} where group_name = #{groupName} and member = #{member}")
    void modifyManager(String groupName, String member, int role);

    @Select("select role from `group` where group_name = #{groupName} and member = #{member}")
    int getRole(String groupName, String member);

    @Update("update `group` set `status` = #{status} where group_name = #{groupName} and member = #{member}")
    void modifyStatus(String groupName, String member, int status);

    @Select("select `status` from `group` where group_name = #{groupName} and member = #{member}")
    int getStatus(String groupName, String member);

    @Select("select count(*) from `group` where group_name = #{groupName} and member = #{member}")
    int getCount(String groupName, String member);

    @Delete("delete from `group` where group_name in (select group_name from `group` where member" +
            " = #{username} and role = 1) or member = #{username}")
    void deleteUser(String username);
}
