#include <stdio.h>
#include <stdlib.h>
#include <unistd.h>
#include <signal.h>
#include <string.h>
#include <sys/types.h>
#include <sys/wait.h>
#include <readline/readline.h>
#include <readline/history.h>
#include <pwd.h>
#include <time.h>
#include <fcntl.h>

#define MAXARGS 50
#define ARGLEN 100

void set_prompt(char *prompt);               // 获取命令提示符
void execute(int argc, char *argv[]);        // 执行命令
int cd(char *path, char *pathname);          // cd命令
void redirect_input(char *input);            // 输入重定向
void redirect_output(char *output, int add); // 输出重定向
void redirect(int argc, char *argv[]);       // 寻找特殊符号
void excute_pipe(char *cmdline);

char formerpath[256]; // 上一次工作目录

int main()
{
    signal(SIGINT, SIG_IGN);
    signal(SIGQUIT, SIG_IGN);
    signal(SIGTSTP, SIG_IGN);
    while (1)
    {
        char *argv[MAXARGS + 1], *cmdline, pathname[256], prompt[100];
        int argc = 0;
        getcwd(pathname, sizeof(pathname));
        set_prompt(prompt);
        if (!(cmdline = readline(prompt)))
            break;
        add_history(cmdline);

        argv[0] = cmdline;
        for (int i = 0; i < MAXARGS; i++)
            argv[i] = strtok_r(argv[i], " ", &argv[i + 1]);
        while (argv[argc])
            argc++;

        int pipe_num = 0;
        for (int i = 0; i < argc; i++)
        {
            if (strcmp(argv[i], "|"))
                pipe_num++;
        }
        if (argc == 0)
        {
            free(cmdline);
            continue;
        }
        if (!strcmp(argv[0], "exit"))
        {
            free(cmdline);
            break;
        }
        if (!strcmp(argv[0], "cd"))
        {
            if (argc == 1)
                argv[1] = "~";
            cd(argv[1], pathname);
        }
        else
        {
            if (pipe_num == 0)
                execute(argc, argv);
            else
                excute_pipe(cmdline);
        }
        free(cmdline);
    }
    return 0;
}

void set_prompt(char *prompt)
{
    // 获取主机名
    char hostname[100];
    if (gethostname(hostname, sizeof(hostname)))
    {
        perror("gethostname");
        exit(EXIT_FAILURE);
    }
    // 获取用户名
    struct passwd *user;
    char *username;
    user = getpwuid(getuid());
    username = user->pw_name;
    // 获取当前工作目录
    char cwd[100];
    char buf[100];
    getcwd(buf, sizeof(buf));
    char *home = getenv("HOME");
    size_t len = strlen(home);
    if (!strncmp(buf, home, len))
    {
        strcpy(cwd, "~");
        strcat(cwd, buf + len);
    } // 将home目录改成~
    // 获取时间
    time_t curtime;
    time(&curtime);
    char time[10];
    strncpy(time, ctime(&curtime) + 11, 8);

    sprintf(prompt, "\n\e[40;33m %s@%s \e[0m\e[44;30m %s \e[0m\e[47;30m %s \e[0m\n\e[32m %s \e[0m",
            username, hostname, cwd, time, "$");
}

void execute(int argc, char *argv[])
{
    if (!strcmp(argv[0], "ls"))
    {
        int index = 1;
        if (argv[1])
            index = 2;
        argv[index++] = (char *)"--color=auto";
    } // 给ls命令上色
    pid_t pid = fork();
    if (pid == -1)
    {
        perror("fork");
        exit(EXIT_FAILURE);
    }
    // 子进程
    if (pid == 0)
    {
        find(argc, argv);
        execvp(argv[0], argv);
        perror("execvp");
        exit(EXIT_FAILURE);
    }
    // 父进程等待子进程退出
    int status = 0;
    pid_t ret = waitpid(pid, &status, 0);
}

int cd(char *targetpath, char *pathname)
{
    if (!strcmp(targetpath, "-"))
        targetpath = formerpath;
    else
        strcpy(formerpath, pathname);

    if (!strncmp(targetpath, "~", 1))
    {
        char *home = getenv("HOME");
        char path[256];
        strcpy(path, home);
        strcat(path, targetpath + 1);
        targetpath = path;
    } // chdir无法识别~,需将~改为home路径

    if (chdir(targetpath) == -1)
        perror("chdir");
}

void redirect_input(char *input)
{
    int fd, newfd;
    fd = open(input, O_RDONLY);
    newfd = dup2(fd, 0);
    if (newfd != 0)
    {
        fprintf(stderr, "Could not open data as fd 0\n");
        exit(EXIT_FAILURE);
    }
}

void redirect_output(char *output, int add)
{
    int fd, newfd;
    if (!add)
        fd = open(output, O_WRONLY | O_CREAT | O_TRUNC, 0644);
    else
        fd = open(output, O_WRONLY | O_CREAT | O_APPEND, 0644);
    newfd = dup2(fd, 1);
    if (newfd != 1)
    {
        fprintf(stderr, "Could not open data as fd 1\n");
        exit(EXIT_FAILURE);
    }
}

void redirect(int argc, char *argv[])
{
    for (int i = 0; i < argc; i++)
    {
        if (!strcmp(argv[i], "<"))
        {
            redirect_input(argv[i + 1]);
            argv[i] = NULL;
            i++;
        }
        if (!strcmp(argv[i], ">"))
        {
            redirect_output(argv[i + 1], 0);
            argv[i] = NULL;
            i++;
        }
        if (!strcmp(argv[i], ">>"))
        {
            redirect_output(argv[i + 1], 1);
            argv[i] = NULL;
            i++;
        }
    }
}

void excute_pipe(char *cmdline)
{
    // 按 | 分割命令
    int cmd_num = 0;
    char *cmd[MAXARGS];
    char *token = strtok(cmdline, "|");
    while (token)
    {
        cmd[cmd_num++] = token;
        token = strtok(NULL, "|");
    }

    int pipefd[2];
    if (pipe(pipefd) == -1)
    {
        perror("pipe");
        exit(EXIT_FAILURE);
    }
    for (int i = 0; i < cmd_num; i++)
    {
        pid_t pid = fork();
        if (pid == -1)
        {
            perror("fork");
            exit(EXIT_FAILURE);
        }
        else if (pid == 0)
        {
            if (i > 0)
            {
                close(pipefd[0]);
                if (dup2(pipefd[1], 1) == -1)
                {
                    fprintf(stderr, "Could not redirect stdout");
                    exit(EXIT_FAILURE);
                }
                close(pipefd[1]);
            }

            if (i < cmd_num - 1)
            {
                int new_pipefd[2];
                if (pipe(pipefd) == -1)
                {
                    perror("pipe");
                    exit(EXIT_FAILURE);
                }
                close(pipefd[1]);
                if(dup2(pipefd[0],0) == -1){
                    fprintf(stderr, "Could not redirect stdin");
                    exit(EXIT_FAILURE);
                }
                close(pipefd[0]);
                pipefd[0] = new_pipefd[0];
                pipefd[1] = new_pipefd[1];

                //执行当前命令
            }
        }
    }
}