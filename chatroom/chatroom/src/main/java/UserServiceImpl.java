public class UserServiceImpl implements UserService {
    private UserDao userDao;
    @Override
    public User fineByName(String userName) {
        return userDao.getUserByUsername(userName);
    }

    @Override
    public void addUser(String userName, String password) {
        userDao.insertUser(userName,password);
    }
}
