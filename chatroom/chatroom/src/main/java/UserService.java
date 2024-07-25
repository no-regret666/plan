public interface UserService {
    //根据用户名寻找用户
    User fineByName(String userName);

    //添加用户
    void addUser(String userName, String password);
}
