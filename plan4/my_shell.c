#include <stdio.h>
#include <stdlib.h>
#include <unistd.h>
#include <signal.h>
#include <string.h>

#define MAXARGS 20
#define ARGLEN 100



int main(){
    //解析命令行参数
    char *arglist[MAXARGS + 1],cmdline[ARGLEN];
    fgets(cmdline,ARGLEN,stdin);
    int len = strlen(cmdline);
    cmdline[len - 1] = '\0';
    arglist[0] = cmdline;
    for(int i = 0;i < MAXARGS;i++){
        arglist[i] = strtok_r(arglist[i]," ",&arglist[i + 1]);
    }
    for(int i = 0;i < 5;i++){
        printf("arglist[%d] = %s\n",i,arglist[i]);
    }

    
}
