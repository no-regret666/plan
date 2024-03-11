#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <stdbool.h>
#include <ctype.h>
struct strbuf{
    int len; // 当前缓冲区（字符串）长度
    int alloc; // 当前缓冲区（字符串）容量
    char *buf; // 缓冲区（字符串）
};

// Part A
// 初始化 sb 结构体，容量为 alloc。
void strbuf_init(struct strbuf *sb, size_t alloc){
    sb->len = 0;
    sb->alloc = alloc;
    sb->buf = (char *)malloc(alloc * sizeof(char));
    if (sb->buf == NULL){
        fprintf(stderr, "分配内存失败\n");
        exit(EXIT_FAILURE);
    }
};

// 将字符串填充到 sb 中，长度为 len, 容量为 alloc。
void strbuf_attach(struct strbuf *sb, void *str, size_t len, size_t alloc){
    sb->len = len;
    sb->alloc = alloc;
    sb->buf = (char *)str;
};

// 释放 sb 结构体的内存。
void strbuf_release(struct strbuf *sb){
    free(sb->buf);
    sb->buf == NULL;
};

// 交换两个 strbuf。
void strbuf_swap(struct strbuf *a, struct strbuf *b){
    int temp_len = a->len;
    a->len = b->len;
    b->len = temp_len;
    int temp_alloc = a->alloc;
    a->alloc = b->alloc;
    b->alloc = temp_alloc;
    char *temp_buf = a->buf;
    a->buf = b->buf;
    b->buf = temp_buf;
};

// 将 sb 中的原始内存取出，并将 sz 设置为其 alloc 大小 。
char *strbuf_detach(struct strbuf *sb, size_t *sz){
    *sz = sb->alloc;
    return sb->buf;
};

// 比较两个 strbuf 的内存是否相同。
int strbuf_cmp(const struct strbuf *first, const struct strbuf *second){
    if (first->len == second->len)
    return 0;
    else
    return 1;
};

// 清空 sb。
void strbuf_reset(struct strbuf *sb){
    sb->len = 0;
    memset(sb->buf, '\0', sizeof(sb->buf));
};

// Part B
// 确保在 len 之后 strbuf 中至少有 extra 个字节的空闲空间可用。
void strbuf_grow(struct strbuf *sb, size_t extra){
    if (sb->alloc < sb->len + extra){
        sb->buf = (char *)realloc(sb->buf, sb->len + extra);
        if (sb->buf == NULL){
            fprintf(stderr, "重新分配内存失败\n");
            exit(EXIT_FAILURE);
        }
        sb->alloc = sb->len + extra;
    }
};

// 向 sb 追加长度为 len 的数据 data。
void strbuf_add(struct strbuf *sb, const void *data, size_t len){
    strbuf_grow(sb, len + 1);
    //法 1：
    //strncat(sb->buf,(char *)data,len-1);
    //法 2：
    //char *str = (char *)data;
    // for(int i = sb->len;i < sb->len + len;i++){
    //     for(int j = 0;j < len;j++)
    //     sb->buf[i] = str[j];
    // }
    //上述方法都是错误的
    memcpy(sb->buf + sb->len, data, len);//memcpy是一个很好用的函数
    sb->len += len;
    sb->buf[sb->len] = '\0';
};

// 向 sb 追加一个字符 c。
void strbuf_addch(struct strbuf *sb, int c){
    strbuf_grow(sb, 2);
    if (sb->buf == NULL){
        fprintf(stderr, "重新分配内存失败\n");
        exit(EXIT_FAILURE);
     }
    sb->buf[sb->len] = (char)c;
    sb->buf[sb->len + 1] = '\0';
    sb->len += 1;
};

// 向 sb 追加一个字符串 s。
void strbuf_addstr(struct strbuf *sb, const char *s){
    int len = strlen(s);
    strbuf_add(sb, (char *)s, len);
}

// 向一个 sb 追加另一个 strbuf的数据。
void strbuf_addbuf(struct strbuf *sb, const struct strbuf *sb2){
    sb->buf = (char *)realloc(sb->buf, sb->alloc + sb2->alloc);
    if (sb->buf == NULL){
        fprintf(stderr, "重新分配内存失败\n");
        exit(EXIT_FAILURE);
    }
    strbuf_addstr(sb, sb2->buf);
};

// 设置 sb 的长度 len。
void strbuf_setlen(struct strbuf *sb, size_t len){
    sb->len = len;
    sb->buf[len] = '\0';
};

// 计算 sb 目前仍可以向后追加的字符串长度。
size_t strbuf_avail(const struct strbuf *sb){
    return sb->alloc - sb->len - 1;
};

// 向 sb 内存坐标为 pos 位置插入长度为 len 的数据 data。
void strbuf_insert(struct strbuf *sb, size_t pos, const void *data, size_t len){
    // int last = sb->len - pos + 1;
    // char str[last];
    // int j = 0;
    strbuf_grow(sb,len + 1);
    // for(int i = pos;i <= sb->len + pos;i++){
    //      sb->buf[i + len] = str[j++]; 
    // }
    //memmove(sb->buf + pos + len, str, sb->len - pos);
    memmove(sb->buf + pos + len, sb->buf + pos, sb->len - pos);//memmove也是一个很好用的函数
    memcpy(sb->buf + pos,data,len);
    sb->len += len;
    sb->buf[sb->len] = '\0';
};

// Part C
// 去除 sb 缓冲区右端的所有 空格，tab, '\t'。
void strbuf_rtrim(struct strbuf *sb){
    int i = sb->len - 1;
    while(isblank(sb->buf[i])){
        sb->len--;
        sb->buf[i] = '\0';
        i--;
    }
};

// 去除 sb 缓冲区左端的所有 空格，tab, '\t'。
void reverse(char* s){
    int len = strlen(s);
    int i = 0;
    char c;
    while (i <= len / 2 - 1)
    {
        c = *(s + i);
        *(s + i) = *(s + len - 1 - i);
        *(s + len - 1 - i) = c;
        i++;
    }
}
void strbuf_ltrim(struct strbuf *sb){
    //法1：翻转字符串，然后调用strbuf_rtrim,再翻转字符串
    //reverse(sb->buf);
    //strbuf_rtrim(sb);
    //reverse(sb->buf);
    //法2：直接移除左端元素
    int i = 0;
    while(isblank(sb->buf[i])){
        memmove(sb->buf,sb->buf + 1,sb->len - 1);
        sb->len--;
    }
};

// 删除 sb 缓冲区从 pos 坐标长度为 len 的内容。
void strbuf_remove(struct strbuf *sb, size_t pos, size_t len){
    memmove(sb->buf + pos,sb->buf + pos + len,sb->len - pos - len);
    sb->len -= len;
};

// Part D
// sb 增长 hint ? hint : 8192 大小， 然后将文件描述符为 fd 的所有文件内容追加到 sb 中。
size_t strbuf_read(struct strbuf *sb, int fd, size_t hint){
    strbuf_grow(sb, (hint ? hint : 8192) + 1);
    FILE *fp = fdopen(fd,"r+");
    int ch;
    while((ch = fgetc(fp)) != EOF){
        strbuf_addch(sb,ch);
    }
    return 0;
};

// 将 将文件句柄为 fp 的一行内容（抛弃换行符）读取到 sb 。
int strbuf_getline(struct strbuf *sb, FILE *fp){
    int ch;
    while((ch = fgetc(fp)) != EOF && ch != '\n'){
        strbuf_addch(sb,ch);
    }
    return 0;
};

// 无信用挑战练习
// 1.实现字符串切割（C 系字符串函数的一个痛点）。
// 将长度为 len 的字符串 str 根据切割字符 terminator 切成多个 strbuf,并从结果返回，max 可以用来限定
// 最大切割数量。返回 struct strbuf 的指针数组，数组的最后元素为 NULL
struct strbuf **strbuf_split_buf(const char *str, size_t len, int terminator, int max){
    char ch = (char)terminator;
    int cnt = 0; //实际切割数
    bool inbuf = false;
    int i = 0;
    while(i < len){
        if(((*(str + i)) != ch) && !inbuf){
            inbuf = true;
            cnt++;
        }
        if(((*(str + i)) == ch) && inbuf)
        inbuf = false;
        i++;
    } //计算实际切割数
    cnt = cnt < max ? cnt : max; //实际切割数与最大切割数量相比取较小值
    struct strbuf **result = (struct strbuf **)malloc((cnt + 1) * sizeof(struct strbuf*));
    if(result == NULL){
        fprintf(stderr, "分配内存失败\n");
        exit(EXIT_FAILURE);
    }
    char **s = (char **)malloc(cnt * sizeof(s));
    int *str_len = (int *)malloc(cnt * sizeof(int));
    int j = 0,k = 0;
    for(i = 0;i < cnt;i++){
        result[i] = (struct strbuf *)malloc(sizeof(struct strbuf));
        strbuf_init(result[i],len + 1);
    }
    for(i = 0;i < cnt;i++){
        str_len[i] = 0;
        *(s + i) = (char *)malloc((len + 1) * sizeof(char));
        k = 0;
        while(j < len && str[j] == ch)
        j++;
        while(j < len && str[j] != ch){
            *(*(s + i) + k) = str[j];
            j++;
            k++;
            str_len[i]++;
        }
        *(*(s + i) + k) = '\0';
        str_len[i]++;
        if(j == len)
        break;
        j++;
    }
    for(i = 0;i < cnt;i++){
        memcpy(result[i]->buf,*(s + i),str_len[i] - 1);
        result[i]->buf[str_len[i] - 1] = '\0';
        result[i]->len = str_len[i] - 1;
        free(*(s + i));
    }
    result[cnt] = NULL;
    free(s);
    free(str_len);
    return result;
};

// 2.实现判断一个 strbuf 是否以指定字符串开头的功能（C 系字符串函数的另一个痛点）
// target_str : 目标字符串，str : 前缀字符串，strnlen : target_str 长度 ，前缀相同返回 true 失败
// 返回 false
bool strbuf_begin_judge(char *target_str, const char *str, int strnlen){
    if(str == NULL)
    return true;
    else 
    return strncmp(target_str,str,strlen(str)) == 0;
};

// 3.获取字符串从坐标 [begin, end) 的所有内容（可以分成引用和拷贝两个模式） 。
// target_str : 目标字符串，begin : 开始下标，end 结束下标。len : target_buf的长度，参数不合法返回
//  NULL. 下标从0开始，[begin, end)区间。
char *strbuf_get_mid_buf(char *target_buf, int begin, int end, int len){
    if(target_buf == NULL)
    return NULL;
    //char result[len]; 起初是这样写的，但结果总是NULL
    char *result = (char *)malloc(len);
    memcpy(result,target_buf + begin,end - begin);
    result[end - begin] = '\0';
    return result;
};