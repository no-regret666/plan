#include <stdio.h>
#include <stdlib.h>
#include <unistd.h>
#include <signal.h>
#include <string.h>
#include <sys/types.h>
#include <sys/wait.h>

#define MAXARGS 20
#define ARGLEN 100

char *readcmd(char *pathname);  // 读取命令行
char *my_getcwd(); // 获取当前工作目录
void execute(char *arglist[]);  // 执行命令
int cd(char *path, char *pathname);
void print_prompt(char *path);

char formerpath[256]; // 上一次工作目录

int main()
{
    char *arglist[MAXARGS + 1], *cmdline, pathname[256];
    strcpy(pathname,my_getcwd());
    while ((cmdline = readcmd(pathname)) != NULL)
    {
        arglist[0] = cmdline;
        for (int i = 0; i < MAXARGS; i++)
        {
            arglist[i] = strtok_r(arglist[i], " ", &arglist[i + 1]);
        }
        if (!strcmp(arglist[0], "cd"))
        {
            cd(arglist[1], pathname);
        }
        else
        {
            if (cmdline != NULL)
            {
                execute(arglist);
            }
        }
        strcpy(pathname,my_getcwd());
        free(cmdline);
    }
    return 0;
}

char *readcmd(char *pathname)
{
    print_prompt(pathname);
    char *buf;
    int bufsize = 0;
    int pos = 0;
    char c;
    while ((c = getc(stdin)) != '\n')
    {
        if (pos == 0)
            buf = (char *)malloc(1);
        else
        {
            buf = (char *)realloc(buf, bufsize + 2);
        }
        buf[pos++] = c;
        bufsize++;
    }
    if (pos == 0)
        return NULL;
    buf[pos] = '\0';
    return buf;
}

char *my_getcwd()
{
    char *buf = getcwd(NULL, 0);
    return buf;
}

void execute(char *arglist[])
{
    pid_t pid = fork();
    switch (pid)
    {
    case -1:
        perror("fork");
        exit(EXIT_FAILURE);

    // 子进程
    case 0:
        execvp(arglist[0], arglist);
        perror("execvp");
        exit(EXIT_FAILURE);

    // 父进程等待子进程退出
    case 1:
        int child_status;
        if ((wait(&child_status)) == -1)
            perror("wait");
    }
}

int cd(char *targetpath, char *pathname)
{
    if (!strcmp(targetpath, "-"))
        targetpath = formerpath;
    else
        strcpy(formerpath, pathname);

    if(!strncmp(targetpath,"~",1)){
        char *home = getenv("HOME");
        char path[256];
        strcpy(path,home);
        strcat(path,targetpath + 1);
        targetpath = path;
    }

    if (chdir(targetpath) == -1)
    {
        perror("chdir");
    }
}

void print_prompt(char *pathname)
{
    char *buf = pathname;
    char path[256];
    char *home = getenv("HOME");
    size_t len = strlen(home);
    if (!strncmp(buf, home, len))
    {
        strcpy(path, "~");
        strcat(path, buf + len);
    } // 将home目录改成~
    fprintf(stdout, "\e[40;33m %s \e[0m\e[44;30m %s \e[0m\e[30;37m %s \e[0m",
    "noregret@noregret-arch",path,"$");
}