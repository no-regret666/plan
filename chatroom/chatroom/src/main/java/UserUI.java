import org.springframework.beans.factory.annotation.Autowired;

import java.util.Scanner;

public class UserUI {
    @Autowired
    private UserService userService;

    public void menu() {
        System.out.println("-----------------------------------------");
        System.out.println("              欢迎进入聊天室                ");
        System.out.println("-----------------------------------------");
        System.out.println("           1.登录       2.注册             ");
        System.out.println("          3.找回密码     4.注销             ");
        System.out.println("                 5.退出                    ");
        System.out.println("------------------------------------------");
        Scanner sc = new Scanner(System.in);
        int key = sc.nextInt();
        switch (key) {
            case 1:
                if (login()) {
                    System.out.println("登录成功！");
                } else {
                    System.out.println("登录失败！");
                }
                break;

        }
    }

    public boolean login() {
        boolean flag = false;
        Scanner sc = new Scanner(System.in);
        System.out.println("请输入用户名:");
        String username = sc.nextLine();
        System.out.println("请输入密码");
        String password = sc.nextLine();
        User user = userService.fineByName(username);
        if (user == null) {
            System.out.println("该用户不存在！");
        } else {
            if (user.getPassword().equals(password)) {
                System.out.println("登录成功！");
                flag = true;
            } else {
                System.out.println("密码错误！");
            }
        }
        return flag;
    }
    public void register(){
        Scanner sc = new Scanner(System.in);
        System.out.println("请输入用户名:");
        String username = sc.nextLine();
        User user = userService.fineByName(username);
        while(user != null){
            System.out.println("该用户名已存在！");
            user = userService.fineByName(sc.nextLine());
        }
        System.out.println("请输入密码:");
    }
}
