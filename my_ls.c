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
#include <stdbool.h>

typedef struct
{
    char *filename;
    struct stat info;
} Fileinfo;

// 函数声明
void do_ls(char *dirname);
void mode_to_letters(mode_t num, char *mode);
void print_fileinfo(Fileinfo fileinfo);
void print_filename(char *filename, mode_t filemode);
int compare(const void *a, const void *b);
int compare_t(const void *a, const void *b);

bool has_a = false;
bool has_l = false;
bool has_R = false;
bool has_t = false;
bool has_r = false;
bool has_i = false;
bool has_s = false;

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
                    has_a = true;
                    break;
                case 'l':
                    has_l = true;
                    break;
                case 'R':
                    has_R = true;
                    break;
                case 't':
                    has_t = true;
                    break;
                case 'r':
                    has_r = true;
                    break;
                case 'i':
                    has_i = true;
                    break;
                case 's':
                    has_s = true;
                    break;
                default:
                    printf("ls:不适用的选项\n请尝试执行 \"ls --help\" 来获取更多信息\n");
                    return 0;
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
    // 动态分配内存
    Fileinfo *fileinfo = malloc(sizeof(Fileinfo) * 10000);
    if (fileinfo == NULL)
    {
        perror("内存分配失败");
        exit(EXIT_FAILURE);
    }

    int file_cnt = 0;
    DIR *dir_ptr;
    struct dirent *cur_dirent;
    if ((dir_ptr = opendir(dirname)) == NULL)
    {
        perror("打开文件夹失败");
        exit(EXIT_FAILURE);
    }
    else
    {
        while ((cur_dirent = readdir(dir_ptr)) != NULL)
        {
            if (!has_a && cur_dirent->d_name[0] == '.')
                continue;
            fileinfo[file_cnt++].filename = strdup(cur_dirent->d_name); // 使用 strdup 分配内存
        }
    }

    // 存储信息
    for (int i = 0; i < file_cnt; i++)
    {
        char pathname[1000];
        snprintf(pathname, sizeof(pathname), "%s/%s", dirname, fileinfo[i].filename); // 使用 snprintf 避免缓冲区溢出
        if (lstat(pathname, &fileinfo[i].info) == -1)
        {
            perror("获取信息失败");
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
                if (strcmp(fileinfo[i].filename, ".") != 0 && strcmp(fileinfo[i].filename, "..") != 0)
                {
                    char pathname[1000];
                    snprintf(pathname, sizeof(pathname), "%s/%s", dirname, fileinfo[i].filename);
                    printf("\n%s:\n", pathname);
                    do_ls(pathname);
                }
            }
        }
    }
    closedir(dir_ptr);

    // 释放内存
    for (int i = 0; i < file_cnt; ++i)
        free(fileinfo[i].filename);
    free(fileinfo);
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
    return _a->info.st_mtime - _b->info.st_mtime;
}

void mode_to_letters(mode_t num, char *mode)
{
    strcpy(mode, "----------");
    switch (num & __S_IFMT)
    {
    case __S_IFREG:
        mode[0] = '-';
        break;
    case __S_IFDIR:
        mode[0] = 'd';
        break;
    case __S_IFCHR:
        mode[0] = 'c';
        break;
    case __S_IFBLK:
        mode[0] = 'b';
        break;
    case __S_IFIFO:
        mode[0] = 'p';
        break;
    case __S_IFSOCK:
        mode[0] = 's';
        break;
    case __S_IFLNK:
        mode[0] = 'l';
        break;
    }
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

        struct passwd *user = getpwuid(fileinfo.info.st_uid);
        printf("%s ", user->pw_name); // 打印用户名

        struct group *gp = getgrgid(fileinfo.info.st_gid);
        printf("%s ", gp->gr_name); // 打印组名

        printf("%-10ld ", fileinfo.info.st_size); // 打印文件大小

        printf("%.12s ", ctime(&fileinfo.info.st_mtime) + 4); // 打印时间
    }
    print_filename(fileinfo.filename, fileinfo.info.st_mode);
    printf("\n");
}

void print_filename(char *filename, mode_t filemode)
{
    if (S_ISDIR(filemode))
        printf("\033[01;34m%s\033[0m", filename);
    else if (S_ISCHR(filemode))
        printf("\033[40;33m%s\033[0m", filename);
    else if (S_ISBLK(filemode))
        printf("\033[40;33m%s\033[0m", filename);
    else if (S_ISLNK(filemode))
        printf("\033[30;42m%s\033[0m", filename);
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
