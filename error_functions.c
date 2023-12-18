#include <stdarg.h>
#include "error_functions.h"
#include "tlpi_hdr.h"
#include "ename.c.inc"

#ifdef __GNUC__
__attribute__ ((__noreturn__))
#endif
static void
terminate(Boolean useExit3)
{
    char *s;
    //如果定义了 EF_DUMPCORE 环境变量并且是非空字符串，则传储核心；否则，根据 useExit 的值，调用 exit(2) 或 exit(3)
    s = getenv("EF_DUMPCORE"); //getenv：搜索参数指向的环境字符串，并返回相关的值给字符串 s

    if(s != NULL && *s != '\0')
        abort(); //abort：终止程序运行，直接从调用的地方跳出
    else if(useExit3)
        exit(EXIT_FAILURE);
    else
        _exit(EXIT_SUCCESS);
}
static void
outputError(Boolean useErr,int err,Boolean flushStdout,const char *format,va_list ap)
{
#define BUF_SIZE 500
    char buf[BUF_SIZE],userMsg[BUF_SIZE],errText[BUF_SIZE];

    vsnprintf(userMsg,BUF_SIZE,format,ap);

    if(useErr)
        snprintf(errText,BUF_SIZE,"[%s %s]",
                (err > 0 && err <= MAX_ENAME) ? 
                ename[err] : "?UNKNOWN?",strerror(err));
    else
        snprintf(errText,BUF_SIZE,":");

    snprintf(buf,BUF_SIZE,"ERROR%s %s\n",errText,userMsg);

    if(flushStdout)
        fflush(stdout); //刷新stdout的输出缓冲区，清除任何挂起的stdout
    fputs(buf,stderr);
    fflush(stderr); //如果stderr不是行缓冲的话
}

void
errMsg(const char *format,...)
{
    va_list argList;
    int savedErrno;

    savedErrno = errno;

    va_start(argList,format);
    outputError(TRUE,errno,TRUE,format,argList);
    va_end(argList);

    errno = savedErrno;
} // 向标准错误终端输出消息,格式为：“错误类型 + 错误信息 + 用户自定义消息 + 换行”

void
errExit(const char *format,...)
{
    va_list argList;

    va_start(argList,format);
    outputError(TRUE,errno,TRUE,format,argList);
    va_end(argList);

    terminate(TRUE);
} //向标准错误终端输出消息并调用exit()函数或abort()函数（如果环境变量EF_DUMPCORE设置为非空
  //则会调用该函数生成核转储文件供调试用）终止程序，格式同上
void
err_exit(const char *format,...)
{
    va_list argList;

    va_start(argList,format);
    outputError(TRUE,errno,FALSE,format,argList);
    va_end(argList);

    terminate(FALSE);
} //除了调用_exit()函数代替exit()函数和输出错误消息时不刷新标准输入输出缓存（stdio buffers）,其余同
  //errExit()函数。其主要用于当一个进程创建的一个子进程出错需要终止时，得避免子进程刷新从父进程那继承过来的stdio缓存。

void
errExitEN(int errnum,const char *format,...)
{
    va_list argList;

    va_start(argList,format);
    outputError(TRUE,errnum,TRUE,format,argList);
    va_end(argList);

    terminate(TRUE);
} // 主要用于执行POSIX标准的程序出错处理，因为它们的返回值代表了errno

void
fatal(const char *format,...)
{
    va_list argList;

    va_start(argList,format);
    outputError(FALSE,0,TRUE,format,argList);
    va_end(argList);

    terminate(TRUE);
} //它可以用来诊断一般性错误，包括不设置errno值使得库函数运行错误。其余同errExit()函数

void
usageErr(const char *format, ...)
{
    va_list argList;

    fflush(stdout);

    fprintf(stderr, "Usage: ");
    va_start(argList, format);
    vfprintf(stderr, format, argList);
    va_end(argList);

    fflush(stderr);
    exit(EXIT_FAILURE);
} //一般用来诊断命令行命令输入错误（参数使用），格式为“Usage: + 格式化的用户自定义字符串”，而后调用exit()函数终止程序
void
cmdLineErr(const char *format, ...)
{
    va_list argList;

    fflush(stdout);

    fprintf(stderr, "Command-line usage error: ");
    va_start(argList, format);
    vfprintf(stderr, format, argList);
    va_end(argList);

    fflush(stderr);
    exit(EXIT_FAILURE);
} //基本同上，其专指命令行错误，它的输出消息的格式为“Command-line usage error: + 格式化的用户自定义字符串”

