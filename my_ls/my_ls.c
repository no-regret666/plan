#include <stdio.h>
#include <stdlib.h>
#include <unistd.h>
#include <sys/types.h>
#include <dirent.h>
#include <string.h>
#include <sys/stat.h>
#include <time.h>
#include <pwd.h>
#include <grp.h>

void restored_name(struct dirent *cur_dirent);
void do_ls(char *dirname);
// void sort(char **filenames);
void mode_to_letters(mode_t num, char *mode);
void ls_l(struct stat sb);

// struct info_mold{
//     char pathname[256];
//     struct stat;
// }; //存储每个文件的信息

// 全局变量
char *filenames[4096];
int file_cnt = 0;

int has_a = 0;
int has_l = 0;
int has_R = 0;
int has_t = 0;
int has_r = 0;
int has_i = 0;
int has_s = 0; // 初始化参数

int main(int argc, char **argv)
{
    // 记录参数
    for (int i = 1; i < argc; i++)
    {
        if (argv[i][0] == '-')
        {
            for (int j = 1; j < strlen(argv[i]); j++)
            {
                switch (argv[i][j])
                {
                case 'a':
                    has_a = 1;
                    break;
                case 'l':
                    has_l = 1;
                    break;
                case 'R':
                    has_R = 1;
                    break;
                case 't':
                    has_t = 1;
                    break;
                case 'r':
                    has_r = 1;
                    break;
                case 'i':
                    has_i = 1;
                    break;
                case 's':
                    has_s = 1;
                    break;
                default:
                    perror("ls:不适用的选项\n请尝试执行 \"ls --help\" 来获取更多信息");
                }
            }
        }
    }

    // 遍历目录名，针对每个目录做ls操作
    int flag = 0;
    for (int i = 1; i < argc; i++)
    {
        if (argv[i][0] != '-')
        {
            flag = 1;
            do_ls(argv[i]);
        }
    }
    if (flag == 0)
        do_ls(".");

    return 0;
}

void restored_name(struct dirent *cur_dirent)
{
    if (!has_a && *(cur_dirent->d_name) == '.')
        return;
    filenames[file_cnt++] = cur_dirent->d_name;
}

void do_ls(char *dirname)
{
    DIR *dir_ptr;
    struct dirent *cur_dirent;
    if ((dir_ptr = opendir(dirname)) == NULL)
    {
        fprintf(stderr, "无法打开%s\n", dirname);
        exit(EXIT_FAILURE);
    }
    else
    {
        while ((cur_dirent = readdir(dir_ptr)) != NULL)
        {
            restored_name(cur_dirent);
        }
        // sort(filenames);
        closedir(dir_ptr);
    }

    // 存储信息，根据参数打印信息
    for (int i = 0; i < file_cnt; i++)
    {
        char pathname[256];
        strcpy(pathname, dirname);
        strcat(pathname, "/");
        strcat(pathname, filenames[i]);
        struct stat info;
        if (stat(pathname, &info) == -1)
        {
            perror("获取信息失败\n");
            exit(EXIT_FAILURE);
        }

        if (has_i)
            printf("%8lu ", info.st_ino);
        if (has_s)
            printf("%ld ", (long)info.st_size);
        if (has_l)
            ls_l(info);

        color_print(filenames[i], info.st_mode);
        printf("\n");
    }
}

// void sort(char **filenames)
// {
//     char temp[256];
//     for (int i = 0; i < file_cnt - 1; i++)
//     {
//         for (int j = 0; j < file_cnt - 1 - i; j++)
//         {-i
//             if (strcmp(filenames[j], filenames[j + 1]) > 0)
//             {
//                 strcpy(temp, filenames[j]);
//                 strcpy(filenames[j], filenames[j + 1]);
//                 strcpy(filenames[j + 1], temp);
//             }
//         }
//     }
// }

void mode_to_letters(mode_t num, char *mode) // 将权限转换为字符串
{
    // 判断文件类型
    switch (num & __S_IFMT)
    {
    case __S_IFREG: /* Regular file.  */
        mode[0] = '-';
        break;
    case __S_IFDIR: /* Directory.  */
        mode[0] = 'd';
        break;
    case __S_IFCHR: /* Character device.  */
        mode[0] = 'c';
        break;
    case __S_IFBLK: /* Block device.  */
        mode[0] = 'b';
        break;
    case __S_IFIFO: /* FIFO.  */
        mode[0] = 'p';
        break;
    case __S_IFSOCK: /* Socket.  */
        mode[0] = 's';
        break;
    case __S_IFLNK: /* Symbolic link.  */
        mode[0] = 'l';
        break;
    }

    // 权限-i
    if (num & S_IRUSR)
        mode[1] = 'r';
    if (num & S_IWUSR)
        mode[2] = 'w';
    if (num & S_IXUSR)
        mode[3] = 'x';
    if (num & S_IRGRP)
        mode[4] = 'r';
    if (num & S_IWGRP)
        mode[5] = 'w';
    if (num & S_IXGRP)
        mode[6] = 'x';
    if (num & S_IROTH)
        mode[7] = 'r';
    if (num & S_IWOTH)
        mode[8] = 'w';
    if (num & S_IXOTH)
        mode[9] = 'x';

    mode[10] = '\0';
}

void ls_l(struct stat sb)
{
    char *mode = (char *)malloc(11);
    if (mode == NULL)
    {
        perror("内存分配失败\n");
        exit(EXIT_FAILURE);
    }
    mode_to_letters(sb.st_mode, mode);
    printf("%s ", mode);
    free(mode);

    printf("%d ", (int)sb.st_nlink); // 打印链接数

    struct passwd *user;
    user = getpwuid(sb.st_uid);
    printf("%s ", user->pw_name); // 打印用户名

    struct group *gp;
    gp = getgrgid(sb.st_gid);
    printf("%s ", gp->gr_name); // 打印组名

    printf("%ld ", sb.st_size); // 打印文件大小

    struct tm *t;
    t = ctime((const long int *)sb.st_mtime);
    printf("%2d月 %2d %02d:%02d ", t->tm_mon, t->tm_mday, t->tm_hour, t->tm_min); // 打印时间
}

void color_print(char *filename, mode_t filemode) //染色文件名
{
    if (S_ISDIR(filemode))
        printf("\033[01;34m%s\033[0m", filename);
    else if (S_ISCHR(filemode))
        printf("\033[40;33m%s\033[0m", filename);
    else if (S_ISBLK(filemode))
        printf("\033[40;33m%s\033[0m", filename);
    else if (S_ISLNK(filemode))
        printf("\033[01;36m%s\033[0m", filename);
    else if (S_ISREG(filemode))
    {
        if(filemode & S_IXUSR || filemode & S_IXGRP || filemode & S_IXOTH)
            printf("\033[01;32m%s\033[0m",filename);
        else
            printf("%s", filename);
    }
    else
        printf("%s", filename);
}