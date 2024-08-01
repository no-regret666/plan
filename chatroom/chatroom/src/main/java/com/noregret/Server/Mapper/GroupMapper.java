package com.noregret.Server.Mapper;

import com.noregret.Server.pojo.Group;
import com.noregret.Server.pojo.Member;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface GroupMapper {
    @Insert("insert into `group`(group_name,member,role) values (#{groupName},#{member},#{role})")
    void insert(String groupName, String member, int role);

    @Select("select * from `group` where group_name = #{groupName}")
    Group getGroup(String groupName);

    @Select("select id from `group` where group_name = #{groupName} and member = #{member}")
    Integer getId(String groupName, String member);

    @Select("select group_name from `group` where member = #{member}")
    List<String> getGroups(String member);

    @Select("select member,role from `group` where group_name = #{groupName}")
    List<Member> getMembers(String groupName);
}
