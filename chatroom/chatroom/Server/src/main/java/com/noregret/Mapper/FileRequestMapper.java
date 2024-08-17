package com.noregret.Mapper;

import com.noregret.Pojo.FileRequest;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

@Mapper
public interface FileRequestMapper {
    @Insert("insert into fileRequest(fileID,`from`,`to`,status,filename) values (#{fileID},#{from},#{to},#{status},#{filename})")
    void insert(int fileID, String from, String to, int status,String filename);

    @Select("select * from fileRequest where `to` = #{username} and status = 2")
    List<FileRequest> findByUsername(String username);

    @Update("update fileRequest set status = 1 where status = 2")
    void updateStatus(int fileID);
}
