package domain;

import java.util.Arrays;

public class User {
    private int id;
    private String username;
    private String password;
    private int[] telephone = new int[11];

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public int[] getTelephone() {
        return telephone;
    }

    public void setTelephone(int[] telephone) {
        this.telephone = telephone;
    }

    @Override
    public String toString() {
        return "domain.User{" +
                "id=" + id +
                ", username='" + username + '\'' +
                ", password='" + password + '\'' +
                ", telephone=" + Arrays.toString(telephone) +
                '}';
    }
}
