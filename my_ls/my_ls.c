#include <stdio.h>
#include "my_ls.h"
void ls(char *dirname){
    DIR * dir_ptr;
    struct dirent *direntp;
    if((dir_ptr = opendir(dirname)) == NULL){
        fprintf(stderr,"无法打开%s\n",dirname);
        exit(EXIT_FAILURE);
    }
    else{
        while((direntp = readdir(dir_ptr)) != NULL){
            printf("%s\n",direntp->d_name);
        }
        closedir(dir_ptr);
    }
}
void ls_a(){

}
int main(int argc,char **argv){
    if(argc == 1){
        ls(".");
    }
}