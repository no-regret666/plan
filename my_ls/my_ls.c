#include <stdio.h>
#include "my_ls.h"

//全局变量
char *filenames[4096];
int file_cnt = 0;

int has_a = 0;
int has_l = 0;
int has_R = 0;
int has_t = 0;
int has_r = 0;
int has_i = 0;
int has_s = 0; //记录参数

int main(int argc,char **argv){
    int i = 0;
    if(argc == 1){
        ls_a(".");
    }
    else{
        for(i = 1;argv[i][0] != '-';i++){
            if(argv[i][1] == 'a'){
                has_a = 1;
                continue;
            }
            else if(argv[i][1] == 'l'){
                has_l = 1;
                continue;
            }
            else if(argv[i][1] == 'R'){
                has_R = 1;
                continue;
            }
            else if(argv[i][1] == 't'){
                has_t = 1;
                continue;
            }
            else if(argv[i][1] == 'r'){
                has_r = 1;
                continue;
            }
            else if(argv[i][1] == 'i'){
                has_i = 1;
                continue;
            }
            else if(argv[i][1] == 's'){
                has_s = 1;
                continue;
            }
        }
    }
    for(;i < argc;i++){
        ls_a(argv[i]);
    }
    return 0;
}

void restored_name(struct dirent *cur_dirent){
    if(!has_a && *(cur_dirent->d_name) == '.')
    return;
    filenames[file_cnt++] = cur_dirent->d_name;
}

void do_ls(char *dirname){
    DIR * dir_ptr;
    struct dirent *cur_dirent;
    if((dir_ptr = opendir(dirname)) == NULL){
        fprintf(stderr,"无法打开%s\n",dirname);
        exit(EXIT_FAILURE);
    }
    else{
        while((cur_dirent = readdir(dir_ptr)) != NULL){
            restored_name(cur_dirent);
        }
        sort(filenames);
        closedir(dir_ptr);
    }
}

void sort(char **filenames){
    char temp[256];
    for(int i = 0;i < file_cnt - 1;i++){
        for(int j = 0;j < file_cnt - 1 - i;j++){
            if(strcmp(filenames[j],filenames[j + 1]) > 0){
                strcpy(temp,filenames[j]);
                strcpy(filenames[j],filenames[j + 1]);
                strcpy(filenames[j + 1],temp);
            }
        }
    }
}

void ls_a(char *dirname){
    do_ls(dirname);
    for(int i = 0;i < file_cnt;i++){
        printf("%s\n",filenames[i]);
    }
}