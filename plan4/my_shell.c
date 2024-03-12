#include <stdio.h>
#include <stdlib.h>
#include <unistd.h>
#include <signal.h>
#include <string.h>
#include <sys/types.h>
#include <sys/wait.h>

#define MAXARGS 20
#define ARGLEN 100

char *readcmd(char *pathname);
void execute(char *arglist[]);

int main()
{
    char *arglist[MAXARGS + 1], *cmdline, *pathname = ".";
    while ((cmdline = readcmd(pathname)) != NULL)
    {
        arglist[0] = cmdline;
        for (int i = 0; i < MAXARGS; i++)
        {
            arglist[i] = strtok_r(arglist[i], " ", &arglist[i + 1]);
        }
        if (cmdline != NULL)
            execute(arglist);
        free(cmdline);
    }
    return 0;
}

char *readcmd(char *pathname)
{
    fprintf(stdout, "noregret@noregret %s $ ", pathname);
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