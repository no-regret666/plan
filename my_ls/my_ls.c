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

typedef struct
{
    char *filename;
    struct stat info;
} Fileinfo;

void do_ls(char *dirname);
void mode_to_letters(mode_t num, char *mode);
void print_fileinfo(Fileinfo fileinfo);
void print_filename(char *filename, mode_t filemode);
int compare(const void *a, const void *b);
int compare_t(const void *a, const void *b);
void ls_R(char *dirname);

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
            printf("%s:\n", argv[i]);
            do_ls(argv[i]);
        }
    }
    if (flag == 0)
        do_ls(".");

    return 0;
}

void do_ls(char *dirname)
{
    Fileinfo fileinfo[4096];
    int file_cnt = 0;
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
            if (!has_a && *(cur_dirent->d_name) == '.')
                continue;
            fileinfo[file_cnt++].filename = cur_dirent->d_name;
        }
    }

    // 存储信息
    for (int i = 0; i < file_cnt; i++)
    {
        char pathname[256];
        strcpy(pathname, dirname);
        strcat(pathname, "/");
        strcat(pathname, fileinfo[i].filename);
        if (stat(pathname, &fileinfo[i].info) == -1)
        {
            perror("获取信息失败\n");
            exit(EXIT_FAILURE);
        }
    }

    // 排序
    if (has_t)
        qsort(fileinfo, file_cnt, sizeof(Fileinfo), compare_t);
    else
        qsort(fileinfo, file_cnt, sizeof(Fileinfo), compare);
    if (has_r)
    {
        int left = 0, right = file_cnt - 1;
        while (left < right)
        {
            Fileinfo temp = fileinfo[left];
            fileinfo[left++] = fileinfo[right];
            fileinfo[right--] = temp;
        }
    }

    // 打印信息
    for (int i = 0; i < file_cnt; i++)
    {
        print_fileinfo(fileinfo[i]);
    }
    if (has_R)
    {
        for (int i = 0; i < file_cnt; i++)
        {
            if (S_ISDIR(fileinfo[i].info.st_mode))
            {
                char pathname[256];
                strcpy(pathname, dirname);
                strcat(pathname, "/");
                strcat(pathname, fileinfo[i].filename);
                printf("\n%s:\n", pathname);
                do_ls(pathname);
            }
        }
    }
    closedir(dir_ptr);
}

int compare(const void *a, const void *b)
{
    Fileinfo *_a = (Fileinfo *)a;
    Fileinfo *_b = (Fileinfo *)b;
    return strcmp(_a->filename, _b->filename);
}

int compare_t(const void *a, const void *b)
{
    Fileinfo *_a = (Fileinfo *)a;
    Fileinfo *_b = (Fileinfo *)b;
    return _a->info.st_mtime < _b->info.st_mtime;
}

void mode_to_letters(mode_t num, char *mode) // 将权限转换为字符串
{
    strcpy(mode, "----------");
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

void print_fileinfo(const Fileinfo fileinfo)
{
    if (has_i)
        printf("%-8lu ", fileinfo.info.st_ino);
    if (has_s)
        printf("%-8ld ", (long)fileinfo.info.st_size);
    if (has_l)
    {
        char mode[11];
        mode_to_letters(fileinfo.info.st_mode, mode);
        printf("%s ", mode);

        printf("%-2d ", (int)fileinfo.info.st_nlink); // 打印链接数

        struct passwd *user;
        user = getpwuid(fileinfo.info.st_uid);
        printf("%s ", user->pw_name); // 打印用户名

        struct group *gp;
        gp = getgrgid(fileinfo.info.st_gid);
        printf("%s ", gp->gr_name); // 打印组名

        printf("%-10ld ", fileinfo.info.st_size);             // 打印文件大小
        printf("%.12s ", ctime(&fileinfo.info.st_mtime) + 4); // 打印时间
    }

    print_filename(fileinfo.filename, fileinfo.info.st_mode);
    printf("\n");
}

void print_filename(char *filename, mode_t filemode) // 染色文件名
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
        if (filemode & S_IXUSR || filemode & S_IXGRP || filemode & S_IXOTH)
            printf("\033[01;32m%s\033[0m", filename);
        else
            printf("%s", filename);
    }
    else
        printf("%s", filename);
}

// void ls_R(char *dirname)
// {
// }