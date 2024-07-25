import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Select;

public interface UserDao {
    @Select("select * from user where username = #{username}")
    User getUserByUsername(String username);

    @Insert("insert into user(username,password) values (#{username},#{password})")
    void insertUser(String username, String password);
}
