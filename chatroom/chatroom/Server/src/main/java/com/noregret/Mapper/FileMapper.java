package com.noregret.Mapper;

import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.sql.Timestamp;

@Mapper
public interface FileMapper {
    @Insert("insert into `file`(`filename`,`time`) values (#{filename},#{time})")
    void insert(String filename, Timestamp time);

    @Select("select id from file where filename = #{filename} and time = #{time}")
    Integer selectID(String filename, Timestamp time);
}
