package com.noregret;

import java.io.Console;
import java.io.IOException;

public class test {
    public static String getPassword(){
        Console console = System.console();
        char[] password = console.readPassword();
        return new String(password);
    }

    public static void main(String[] args) throws IOException{
        System.out.println("请输入密码：");
        String password = getPassword();
        System.out.println("输入的密码为" + password);
    }
}
